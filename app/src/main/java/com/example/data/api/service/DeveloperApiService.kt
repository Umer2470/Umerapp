package com.example.data.api.service

import com.example.data.api.model.AppVersionCheckRequest
import com.example.data.api.model.AppVersionCheckResponse
import com.example.data.api.model.LicenseHeartbeatRequest
import com.example.data.api.model.LicenseHeartbeatResponse
import com.example.data.api.model.LicenseValidateRequest
import com.example.data.api.model.LicenseValidateResponse
import com.example.data.api.model.RegisterInstallationRequest
import com.example.data.api.model.RegisterInstallationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit interface defining future Developer Server API endpoints.
 */
interface DeveloperApiService {

    @POST("installation/register")
    suspend fun registerInstallation(
        @Body request: RegisterInstallationRequest
    ): Response<RegisterInstallationResponse>

    @POST("license/validate")
    suspend fun validateLicense(
        @Body request: LicenseValidateRequest
    ): Response<LicenseValidateResponse>

    @POST("license/heartbeat")
    suspend fun sendHeartbeat(
        @Body request: LicenseHeartbeatRequest
    ): Response<LicenseHeartbeatResponse>

    @POST("app/version")
    suspend fun checkAppVersion(
        @Body request: AppVersionCheckRequest
    ): Response<AppVersionCheckResponse>
}
