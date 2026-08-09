package com.example.data.api.config

/**
 * Centralized API configuration for the Developer API Connection Layer.
 * Allows easy modification of base URL, versioning, and connection timeouts from a single location.
 */
object ApiConfig {
    /**
     * Default base URL placeholder for the future Developer Server.
     * Must be HTTPS for production security.
     */
    const val DEFAULT_BASE_URL: String = "https://api.example.com/v1/"

    /**
     * Current API Version tag
     */
    const val API_VERSION: String = "v1"

    /**
     * Network Timeouts in Seconds
     */
    const val CONNECT_TIMEOUT_SECONDS: Long = 15L
    const val READ_TIMEOUT_SECONDS: Long = 15L
    const val WRITE_TIMEOUT_SECONDS: Long = 15L

    /**
     * Maximum retry attempts for transient network failures
     */
    const val MAX_RETRY_ATTEMPTS: Int = 2

    /**
     * Dynamic Base URL override if set programmatically by system configuration
     */
    @Volatile
    var dynamicBaseUrl: String? = null

    /**
     * Returns the active Base URL
     */
    fun getBaseUrl(): String {
        val url = dynamicBaseUrl ?: DEFAULT_BASE_URL
        return if (url.endsWith("/")) url else "$url/"
    }
}
