package com.example.data.api.repository

import android.content.Context
import com.example.data.api.license.LicenseStateCache
import com.example.data.api.model.ApiResult
import com.example.data.api.model.AppVersionCheckRequest
import com.example.data.api.model.AppVersionCheckResponse
import com.example.data.api.model.LicenseHeartbeatRequest
import com.example.data.api.model.LicenseHeartbeatResponse
import com.example.data.api.model.LicenseValidateRequest
import com.example.data.api.model.LicenseValidateResponse
import com.example.data.api.model.RegisterInstallationRequest
import com.example.data.api.model.RegisterInstallationResponse
import com.example.data.api.network.ApiClient
import com.example.data.api.network.OfflineNetworkException
import com.example.data.api.security.SecureIdentityManager
import com.example.data.api.security.SecureTokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Repository layer managing Developer API communications, response parsing,
 * token caching, and license state preservation with guaranteed Offline-First safety.
 */
class DeveloperApiRepository(context: Context) {

    private val apiClient = ApiClient.getInstance(context)
    private val identityManager = SecureIdentityManager.getInstance(context)
    private val tokenManager = SecureTokenManager.getInstance(context)
    private val licenseStateCache = LicenseStateCache.getInstance(context)

    /**
     * Registers app installation with the future Developer Server.
     */
    suspend fun registerInstallation(): ApiResult<RegisterInstallationResponse> {
        return withContext(Dispatchers.IO) {
            safeApiCall {
                val req = RegisterInstallationRequest(
                    installationId = identityManager.getInstallationId(),
                    customerId = identityManager.getCustomerId(),
                    storeId = identityManager.getStoreId(),
                    appVersion = identityManager.getAppVersion()
                )
                val res = apiClient.apiService.registerInstallation(req)
                res
            }.also { result ->
                if (result is ApiResult.Success) {
                    val data = result.data
                    if (data.accessToken != null) {
                        tokenManager.saveTokens(
                            accessToken = data.accessToken,
                            refreshToken = data.refreshToken ?: "",
                            expiresInSeconds = data.expiresIn ?: 3600L
                        )
                    }
                }
            }
        }
    }

    /**
     * Validates commercial license key with the Developer Server.
     */
    suspend fun validateLicense(licenseKey: String): ApiResult<LicenseValidateResponse> {
        return withContext(Dispatchers.IO) {
            safeApiCall {
                val req = LicenseValidateRequest(
                    installationId = identityManager.getInstallationId(),
                    customerId = identityManager.getCustomerId(),
                    licenseKey = licenseKey,
                    appVersion = identityManager.getAppVersion()
                )
                apiClient.apiService.validateLicense(req)
            }.also { result ->
                if (result is ApiResult.Success) {
                    val data = result.data
                    licenseStateCache.updateCachedLicenseState(
                        status = data.status,
                        message = data.message,
                        planType = data.planType ?: "COMMERCIAL",
                        maxShops = data.maxShops ?: 5,
                        maxUsers = data.maxUsers ?: 10
                    )
                }
            }
        }
    }

    /**
     * Sends periodic license heartbeat to the Developer Server.
     */
    suspend fun sendHeartbeat(): ApiResult<LicenseHeartbeatResponse> {
        return withContext(Dispatchers.IO) {
            safeApiCall {
                val req = LicenseHeartbeatRequest(
                    installationId = identityManager.getInstallationId(),
                    customerId = identityManager.getCustomerId(),
                    storeId = identityManager.getStoreId()
                )
                apiClient.apiService.sendHeartbeat(req)
            }.also { result ->
                if (result is ApiResult.Success) {
                    val data = result.data
                    licenseStateCache.updateCachedLicenseState(
                        status = data.status,
                        message = data.message
                    )
                }
            }
        }
    }

    /**
     * Checks for app version updates from Developer Server.
     */
    suspend fun checkAppVersion(): ApiResult<AppVersionCheckResponse> {
        return withContext(Dispatchers.IO) {
            safeApiCall {
                val req = AppVersionCheckRequest(
                    appVersion = identityManager.getAppVersion(),
                    installationId = identityManager.getInstallationId()
                )
                apiClient.apiService.checkAppVersion(req)
            }
        }
    }

    /**
     * Safely executes an API call catching network and HTTP errors without crashing.
     */
    private suspend fun <T> safeApiCall(call: suspend () -> Response<T>): ApiResult<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error(code = response.code(), message = "Response body is empty")
                }
            } else {
                ApiResult.Error(
                    code = response.code(),
                    message = response.errorBody()?.string() ?: "Server returned error code ${response.code()}"
                )
            }
        } catch (e: OfflineNetworkException) {
            ApiResult.Offline
        } catch (e: Exception) {
            ApiResult.Error(
                code = -1,
                message = e.message ?: "Network or connection error occurred",
                isNetworkError = true
            )
        }
    }

    /**
     * Returns the Installation ID.
     */
    fun getInstallationId(): String = identityManager.getInstallationId()

    /**
     * Returns current cached license status.
     */
    fun getCachedLicenseStatus(): String = licenseStateCache.getCachedLicenseStatus()
}
