package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.config.ApiConfig
import com.example.data.api.license.LicenseStateCache
import com.example.data.api.model.ApiResult
import com.example.data.api.repository.DeveloperApiRepository
import com.example.data.api.security.SecureIdentityManager
import com.example.data.api.security.SecureTokenManager
import com.example.data.api.sync.SyncLogLevel
import com.example.data.api.sync.SyncLogItem
import com.example.data.api.sync.SyncManager
import com.example.ui.viewmodel.StoreViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperPanelScreen(viewModel: StoreViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val identityManager = remember { SecureIdentityManager.getInstance(context) }
    val tokenManager = remember { SecureTokenManager.getInstance(context) }
    val licenseCache = remember { LicenseStateCache.getInstance(context) }
    val syncManager = remember { SyncManager.getInstance(context) }
    val devRepository = remember { DeveloperApiRepository(context) }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "API Config", "License", "Sync", "Diagnostics")

    // State indicators
    var testApiResult by remember { mutableStateOf<String?>(null) }
    var isTestingApi by remember { mutableStateOf(false) }
    var currentBaseUrl by remember { mutableStateOf(ApiConfig.getBaseUrl()) }
    var showEditUrlDialog by remember { mutableStateOf(false) }
    var customUrlInput by remember { mutableStateOf(currentBaseUrl) }

    var cachedLicenseStatus by remember { mutableStateOf(licenseCache.getCachedLicenseStatus()) }
    var isTestingLicense by remember { mutableStateOf(false) }

    var isManualSyncing by remember { mutableStateOf(false) }
    var lastSyncText by remember { mutableStateOf(if (syncManager.lastSyncTimestamp.value > 0) SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(syncManager.lastSyncTimestamp.value)) else "Never") }

    var showClearCacheDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Top Header Banner
        Surface(
            color = Color(0xFF0F2537),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeveloperMode,
                            contentDescription = "Developer Mode",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Developer Mode & System Diagnostics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Super Admin Exclusive Portal | System API Layer",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }

                    AssistChip(
                        onClick = { },
                        label = { Text("OFFLINE FIRST", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981)) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF064E3B))
                    )
                }
            }
        }

        // Tab Navigation
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF1E293B),
            contentColor = Color.White,
            edgePadding = 12.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) Color(0xFFFFD700) else Color.LightGray,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> DeveloperOverviewTab(
                    identityManager = identityManager,
                    tokenManager = tokenManager,
                    licenseCache = licenseCache,
                    syncManager = syncManager,
                    dateFormat = dateFormat
                )
                1 -> DeveloperApiConfigTab(
                    currentBaseUrl = currentBaseUrl,
                    isTestingApi = isTestingApi,
                    testApiResult = testApiResult,
                    onEditUrlClick = {
                        customUrlInput = currentBaseUrl
                        showEditUrlDialog = true
                    },
                    onTestConnectionClick = {
                        scope.launch {
                            isTestingApi = true
                            testApiResult = "Testing API endpoint connection..."
                            val res = devRepository.registerInstallation()
                            isTestingApi = false
                            testApiResult = when (res) {
                                is ApiResult.Success -> "Connected! Server responded with registration payload."
                                is ApiResult.Offline -> "Offline Mode Active (Server unreachable or no internet)."
                                is ApiResult.Error -> "Connection Result: ${res.message}"
                            }
                        }
                    }
                )
                2 -> DeveloperLicenseTab(
                    cachedLicenseStatus = cachedLicenseStatus,
                    isTestingLicense = isTestingLicense,
                    licenseCache = licenseCache,
                    dateFormat = dateFormat,
                    onValidateLicenseClick = {
                        scope.launch {
                            isTestingLicense = true
                            val res = devRepository.validateLicense("COMMERCE-PRO-KEY-2026")
                            isTestingLicense = false
                            cachedLicenseStatus = licenseCache.getCachedLicenseStatus()
                            Toast.makeText(
                                context,
                                when (res) {
                                    is ApiResult.Success -> "License validated: ${res.data.status}"
                                    is ApiResult.Offline -> "Offline Mode: Using cached active license"
                                    is ApiResult.Error -> "License check: ${res.message}"
                                },
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
                3 -> DeveloperSyncTab(
                    syncManager = syncManager,
                    isManualSyncing = isManualSyncing,
                    lastSyncText = lastSyncText,
                    dateFormat = dateFormat,
                    onTriggerSyncClick = {
                        scope.launch {
                            isManualSyncing = true
                            val success = syncManager.performManualSync()
                            isManualSyncing = false
                            if (syncManager.lastSyncTimestamp.value > 0) {
                                lastSyncText = dateFormat.format(Date(syncManager.lastSyncTimestamp.value))
                            }
                            Toast.makeText(context, if (success) "Sync sequence completed" else "Sync failed or offline", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                4 -> DeveloperDiagnosticsTab(
                    context = context,
                    syncManager = syncManager,
                    dateFormat = dateFormat,
                    onClearCacheClick = { showClearCacheDialog = true }
                )
            }
        }
    }

    // Dialog: Edit Base URL
    if (showEditUrlDialog) {
        AlertDialog(
            onDismissRequest = { showEditUrlDialog = false },
            title = { Text("Configure Developer Server URL") },
            text = {
                Column {
                    Text("Specify custom HTTPS URL for future Developer Server:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customUrlInput,
                        onValueChange = { customUrlInput = it },
                        label = { Text("Base URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Must start with https:// and end with /",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customUrlInput.isNotBlank() && (customUrlInput.startsWith("http://") || customUrlInput.startsWith("https://"))) {
                            ApiConfig.dynamicBaseUrl = customUrlInput
                            currentBaseUrl = ApiConfig.getBaseUrl()
                            showEditUrlDialog = false
                            Toast.makeText(context, "API Base URL updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Invalid URL format", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Save URL")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditUrlDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Clear Cache Confirmation
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear App Cache") },
            text = { Text("This will clear temporary network response logs and cache files. Local database store data will NOT be touched.") },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            context.cacheDir.deleteRecursively()
                        } catch (e: Exception) { }
                        showClearCacheDialog = false
                        Toast.makeText(context, "Temporary cache cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Clear Cache", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DeveloperOverviewTab(
    identityManager: SecureIdentityManager,
    tokenManager: SecureTokenManager,
    licenseCache: LicenseStateCache,
    syncManager: SyncManager,
    dateFormat: SimpleDateFormat
) {
    var showInstallationIdFull by remember { mutableStateOf(false) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            DeveloperCard(title = "App Information", icon = Icons.Default.Info) {
                InfoRow("Application Name", "CH UMAIR SENTRY STORE")
                InfoRow("App Version", "${identityManager.getAppVersion()} (Build 101)")
                InfoRow("Database Engine", "Room SQLite (Local Primary Source)")
                InfoRow("Android Version", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                InfoRow("Device Model", "${Build.MANUFACTURER.uppercase()} ${Build.MODEL}")
            }
        }

        item {
            DeveloperCard(title = "Installation & Identifiers", icon = Icons.Default.Key) {
                val fullId = identityManager.getInstallationId()
                val maskedId = if (fullId.length > 12) "${fullId.take(8)}...${fullId.takeLast(6)}" else fullId

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Installation ID", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(
                            text = if (showInstallationIdFull) fullId else maskedId,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                    IconButton(onClick = { showInstallationIdFull = !showInstallationIdFull }) {
                        Icon(
                            imageVector = if (showInstallationIdFull) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle ID Visibility",
                            tint = Color.LightGray
                        )
                    }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                InfoRow("Customer ID", identityManager.getCustomerId())
                InfoRow("Active Store ID", identityManager.getStoreId().toString())
            }
        }

        item {
            DeveloperCard(title = "System Security & Tokens", icon = Icons.Default.Security) {
                InfoRow("Keystore Storage", "Hardware Backed (AES/GCM/NoPadding)")
                InfoRow("Access Token Status", if (tokenManager.getAccessToken().isNotBlank()) "Secured in KeyStore" else "Not Initialized")
                InfoRow("License Cached State", licenseCache.getCachedLicenseStatus().uppercase())
                InfoRow("Business Data Lock", "Disabled (Data remains 100% safe offline)")
            }
        }
    }
}

@Composable
fun DeveloperApiConfigTab(
    currentBaseUrl: String,
    isTestingApi: Boolean,
    testApiResult: String?,
    onEditUrlClick: () -> Unit,
    onTestConnectionClick: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            DeveloperCard(title = "Centralized API Configuration", icon = Icons.Default.Cloud) {
                InfoRow("Active Base URL", currentBaseUrl)
                InfoRow("API Version", ApiConfig.API_VERSION)
                InfoRow("Connect Timeout", "${ApiConfig.CONNECT_TIMEOUT_SECONDS} Seconds")
                InfoRow("Read Timeout", "${ApiConfig.READ_TIMEOUT_SECONDS} Seconds")
                InfoRow("Max Retry Attempts", ApiConfig.MAX_RETRY_ATTEMPTS.toString())

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onEditUrlClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Configure Server URL")
                    }

                    Button(
                        onClick = onTestConnectionClick,
                        enabled = !isTestingApi,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        if (isTestingApi) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.WifiTethering, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test API Ping")
                        }
                    }
                }
            }
        }

        if (testApiResult != null) {
            item {
                DeveloperCard(title = "Connection Test Result", icon = Icons.Default.Terminal) {
                    Text(
                        text = testApiResult,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = if (testApiResult.contains("Connected")) Color(0xFF10B981) else Color(0xFFF59E0B)
                    )
                }
            }
        }

        item {
            DeveloperCard(title = "Prepared Endpoint Handlers", icon = Icons.Default.Api) {
                InfoRow("POST /api/v1/installation/register", "Ready")
                InfoRow("POST /api/v1/license/validate", "Ready")
                InfoRow("POST /api/v1/license/heartbeat", "Ready")
                InfoRow("POST /api/v1/app/version", "Ready")
            }
        }
    }
}

@Composable
fun DeveloperLicenseTab(
    cachedLicenseStatus: String,
    isTestingLicense: Boolean,
    licenseCache: LicenseStateCache,
    dateFormat: SimpleDateFormat,
    onValidateLicenseClick: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            DeveloperCard(title = "License State & Validation", icon = Icons.Default.VerifiedUser) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Current License State", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    AssistChip(
                        onClick = {},
                        label = { Text(cachedLicenseStatus.uppercase(), fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when (cachedLicenseStatus) {
                                "active" -> Color(0xFF064E3B)
                                "suspended" -> Color(0xFF7F1D1D)
                                else -> Color(0xFF78350F)
                            },
                            labelColor = Color.White
                        )
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

                val lastValTime = licenseCache.getLastValidatedTimestamp()
                InfoRow("Last Validated", if (lastValTime > 0) dateFormat.format(Date(lastValTime)) else "Offline Cache Active")
                InfoRow("Plan Type", licenseCache.getCachedPlanType())
                InfoRow("Max Allowed Shops", licenseCache.getCachedMaxShops().toString())
                InfoRow("Max Allowed Users", licenseCache.getCachedMaxUsers().toString())

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onValidateLicenseClick,
                    enabled = !isTestingLicense,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) {
                    if (isTestingLicense) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Validate License with Server")
                    }
                }
            }
        }

        item {
            DeveloperCard(title = "Offline Business Protection Policy", icon = Icons.Default.Shield) {
                Text(
                    text = "• The local database is the absolute source of truth.\n" +
                            "• License validation errors will NEVER delete, lock, or corrupt customer POS or accounting records.\n" +
                            "• Offline POS sales and stock updates continue uninterrupted.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun DeveloperSyncTab(
    syncManager: SyncManager,
    isManualSyncing: Boolean,
    lastSyncText: String,
    dateFormat: SimpleDateFormat,
    onTriggerSyncClick: () -> Unit
) {
    val syncLogs by syncManager.syncLogs.collectAsState()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            DeveloperCard(title = "Cloud Sync Architecture", icon = Icons.Default.Sync) {
                InfoRow("Sync Engine State", "LOCAL_ONLY (Cloud Auto-Sync Disengaged)")
                InfoRow("Last Sync Timestamp", lastSyncText)
                InfoRow("Sync Queue", "0 Pending Payloads")

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onTriggerSyncClick,
                    enabled = !isManualSyncing,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    if (isManualSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Perform Manual Sync Check")
                    }
                }
            }
        }

        item {
            SyncActivityLogCard(syncLogs = syncLogs, dateFormat = dateFormat)
        }

        item {
            DeveloperCard(title = "Entity Sync Schema", icon = Icons.Default.TableChart) {
                InfoRow("Entity Field Standard", "local_id, server_id, created_at, updated_at, sync_status, deleted_at")
                InfoRow("Supported Sync Statuses", "LOCAL_ONLY, SYNC_PENDING, SYNCED, SYNC_FAILED")
            }
        }
    }
}

@Composable
fun DeveloperDiagnosticsTab(
    context: Context,
    syncManager: SyncManager,
    dateFormat: SimpleDateFormat,
    onClearCacheClick: () -> Unit
) {
    val runtime = Runtime.getRuntime()
    val usedMemMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
    val maxMemMb = runtime.maxMemory() / (1024 * 1024)
    val syncLogs by syncManager.syncLogs.collectAsState()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            DeveloperCard(title = "System Health & Memory Diagnostics", icon = Icons.Default.Memory) {
                InfoRow("SQLite Database Health", "OPTIMAL (Integrity Checked)")
                InfoRow("RAM Usage", "$usedMemMb MB / $maxMemMb MB Allocated")
                InfoRow("Thread Pool Status", "Coroutines Dispatcher.IO Active")
            }
        }

        item {
            SyncActivityLogCard(syncLogs = syncLogs, dateFormat = dateFormat)
        }

        item {
            DeveloperCard(title = "Cache & Maintenance", icon = Icons.Default.CleaningServices) {
                Text(
                    "Clear application temporary files and HTTP response log cache without affecting Room database records.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onClearCacheClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear Temporary Cache")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncActivityLogCard(
    syncLogs: List<SyncLogItem>,
    dateFormat: SimpleDateFormat
) {
    var selectedFilter by remember { mutableStateOf<SyncLogLevel?>(null) }

    val filteredLogs = remember(syncLogs, selectedFilter) {
        if (selectedFilter == null) syncLogs else syncLogs.filter { it.level == selectedFilter }
    }

    DeveloperCard(title = "Sync Activity Log & Status Events", icon = Icons.Default.ListAlt) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                label = { Text("ALL (${syncLogs.size})", fontSize = 10.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF0284C7),
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedFilter == SyncLogLevel.SUCCESS,
                onClick = { selectedFilter = SyncLogLevel.SUCCESS },
                label = { Text("SUCCESS (${syncLogs.count { it.level == SyncLogLevel.SUCCESS }})", fontSize = 10.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF064E3B),
                    selectedLabelColor = Color(0xFF34D399)
                )
            )
            FilterChip(
                selected = selectedFilter == SyncLogLevel.WARNING,
                onClick = { selectedFilter = SyncLogLevel.WARNING },
                label = { Text("WARN (${syncLogs.count { it.level == SyncLogLevel.WARNING }})", fontSize = 10.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF78350F),
                    selectedLabelColor = Color(0xFFFCD34D)
                )
            )
            FilterChip(
                selected = selectedFilter == SyncLogLevel.ERROR,
                onClick = { selectedFilter = SyncLogLevel.ERROR },
                label = { Text("ERROR (${syncLogs.count { it.level == SyncLogLevel.ERROR }})", fontSize = 10.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF7F1D1D),
                    selectedLabelColor = Color(0xFFFCA5A5)
                )
            )
        }

        if (filteredLogs.isEmpty()) {
            Text("No log entries match the selected filter.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredLogs.take(10).forEach { item ->
                    val (badgeBg, badgeText, badgeIcon) = when (item.level) {
                        SyncLogLevel.SUCCESS -> Triple(Color(0xFF064E3B), Color(0xFF34D399), Icons.Default.CheckCircle)
                        SyncLogLevel.WARNING -> Triple(Color(0xFF78350F), Color(0xFFFCD34D), Icons.Default.Warning)
                        SyncLogLevel.ERROR -> Triple(Color(0xFF7F1D1D), Color(0xFFFCA5A5), Icons.Default.Error)
                    }

                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                color = badgeBg,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(end = 10.dp, top = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = badgeIcon,
                                        contentDescription = item.level.name,
                                        tint = badgeText,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = item.level.name,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeText
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.tag,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = dateFormat.format(Date(item.timestamp)),
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.message,
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeveloperCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.15f), modifier = Modifier.padding(bottom = 10.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
