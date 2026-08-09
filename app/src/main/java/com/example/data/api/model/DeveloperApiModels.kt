package com.example.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Objects for the Developer API Connection Layer.
 */

@JsonClass(generateAdapter = true)
data class RegisterInstallationRequest(
    @Json(name = "installation_id") val installationId: String,
    @Json(name = "customer_id") val customerId: String,
    @Json(name = "store_id") val storeId: Long,
    @Json(name = "app_version") val appVersion: String,
    @Json(name = "platform") val platform: String = "Android",
    @Json(name = "registered_at") val registeredAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class RegisterInstallationResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "installation_id") val installationId: String,
    @Json(name = "message") val message: String = "Registration successful",
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "expires_in") val expiresIn: Long? = 3600L
)

@JsonClass(generateAdapter = true)
data class LicenseValidateRequest(
    @Json(name = "installation_id") val installationId: String,
    @Json(name = "customer_id") val customerId: String,
    @Json(name = "license_key") val licenseKey: String,
    @Json(name = "app_version") val appVersion: String
)

@JsonClass(generateAdapter = true)
data class LicenseValidateResponse(
    @Json(name = "status") val status: String, // "active", "suspended", "expired", "update_required"
    @Json(name = "valid_until") val validUntil: Long? = null,
    @Json(name = "plan_type") val planType: String? = "COMMERCIAL",
    @Json(name = "max_shops") val maxShops: Int? = 5,
    @Json(name = "max_users") val maxUsers: Int? = 10,
    @Json(name = "message") val message: String = "License validated successfully"
)

@JsonClass(generateAdapter = true)
data class LicenseHeartbeatRequest(
    @Json(name = "installation_id") val installationId: String,
    @Json(name = "customer_id") val customerId: String,
    @Json(name = "store_id") val storeId: Long,
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class LicenseHeartbeatResponse(
    @Json(name = "status") val status: String,
    @Json(name = "server_time") val serverTime: Long = System.currentTimeMillis(),
    @Json(name = "next_heartbeat_seconds") val nextHeartbeatInSeconds: Long = 86400L,
    @Json(name = "message") val message: String = "Heartbeat acknowledged"
)

@JsonClass(generateAdapter = true)
data class AppVersionCheckRequest(
    @Json(name = "app_version") val appVersion: String,
    @Json(name = "installation_id") val installationId: String,
    @Json(name = "platform") val platform: String = "Android"
)

@JsonClass(generateAdapter = true)
data class AppVersionCheckResponse(
    @Json(name = "latest_version") val latestVersion: String,
    @Json(name = "is_update_required") val isUpdateRequired: Boolean = false,
    @Json(name = "download_url") val downloadUrl: String? = null,
    @Json(name = "release_notes") val releaseNotes: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiErrorResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "message") val message: String,
    @Json(name = "details") val details: String? = null
)

/**
 * Generic result wrapper for API operations preserving Offline-First resilience.
 */
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(
        val code: Int = -1,
        val message: String,
        val isNetworkError: Boolean = false
    ) : ApiResult<Nothing>()
    object Offline : ApiResult<Nothing>()
}
