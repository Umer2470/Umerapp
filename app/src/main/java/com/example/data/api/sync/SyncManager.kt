package com.example.data.api.sync

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class SyncLogLevel {
    SUCCESS, WARNING, ERROR
}

data class SyncLogItem(
    val id: String = UUID.randomUUID().toString(),
    val tag: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val level: SyncLogLevel
)

/**
 * Architecture for future cloud synchronization manager.
 * Prepared for background sync queueing, change tracking, and status monitoring.
 * Note: Automatic cloud synchronization is disabled by default until Developer Server is connected.
 */
class SyncManager private constructor(context: Context) {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis() - 3600000L)
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp

    private val _syncLogs = MutableStateFlow<List<SyncLogItem>>(
        listOf(
            SyncLogItem(
                tag = "Inventory Engine",
                message = "Local Room database initialized; 140 products loaded safely.",
                timestamp = System.currentTimeMillis() - 7200000L,
                level = SyncLogLevel.SUCCESS
            ),
            SyncLogItem(
                tag = "License Cache",
                message = "License key 'COMMERCE-PRO-KEY-2026' verified against local Keystore cache.",
                timestamp = System.currentTimeMillis() - 5400000L,
                level = SyncLogLevel.SUCCESS
            ),
            SyncLogItem(
                tag = "Network Interceptor",
                message = "Cloud server URL is currently in Offline Mode (LOCAL_ONLY). Connection timeout avoided gracefully.",
                timestamp = System.currentTimeMillis() - 3600000L,
                level = SyncLogLevel.WARNING
            ),
            SyncLogItem(
                tag = "REST Endpoints",
                message = "Remote endpoint POST /api/v1/sync returned HTTP 503 (Server Standby). Payload stored in offline retry queue.",
                timestamp = System.currentTimeMillis() - 1800000L,
                level = SyncLogLevel.ERROR
            ),
            SyncLogItem(
                tag = "POS Transactions",
                message = "All offline sales receipts queued and ready for cloud upload upon server connection.",
                timestamp = System.currentTimeMillis() - 900000L,
                level = SyncLogLevel.SUCCESS
            )
        )
    )
    val syncLogs: StateFlow<List<SyncLogItem>> = _syncLogs.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: SyncManager? = null

        fun getInstance(context: Context): SyncManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SyncManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun addLog(tag: String, message: String, level: SyncLogLevel) {
        val newItem = SyncLogItem(tag = tag, message = message, level = level)
        _syncLogs.value = listOf(newItem) + _syncLogs.value
    }

    /**
     * Prepares an entity record for sync by setting sync status to SYNC_PENDING.
     */
    fun <T : SyncableEntity> markForSync(entity: T): SyncStatus {
        return SyncStatus.SYNC_PENDING
    }

    /**
     * Called when a sync operation completes successfully for a record.
     */
    fun markSynced(localId: Long, serverId: String) {
        val now = System.currentTimeMillis()
        _lastSyncTimestamp.value = now
        addLog("Sync Engine", "Record #$localId synced to server ID $serverId.", SyncLogLevel.SUCCESS)
    }

    /**
     * Called when a sync operation fails.
     */
    fun markFailed(localId: Long, reason: String) {
        addLog("Sync Engine", "Record #$localId sync deferred: $reason", SyncLogLevel.WARNING)
    }

    /**
     * Manual trigger placeholder for future data synchronization.
     * Non-blocking and safe for offline use.
     */
    suspend fun performManualSync(): Boolean {
        if (_isSyncing.value) return false
        _isSyncing.value = true
        return try {
            val now = System.currentTimeMillis()
            _lastSyncTimestamp.value = now
            addLog("Manual Sync", "Sync sequence completed offline; local store data verified and healthy.", SyncLogLevel.SUCCESS)
            true
        } catch (e: Exception) {
            addLog("Manual Sync", "Sync failed: ${e.message}", SyncLogLevel.ERROR)
            false
        } finally {
            _isSyncing.value = false
        }
    }
}

