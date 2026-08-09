package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AttendanceRecord
import com.example.data.entity.UserAccount
import com.example.ui.viewmodel.StoreViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: StoreViewModel,
    onNavigateBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allAttendance by viewModel.allAttendanceRecords.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Today's Live Attendance, 1: Staff Roster, 2: Monthly History
    var searchQuery by remember { mutableStateOf("") }
    var showMarkDialog by remember { mutableStateOf<UserAccount?>(null) }
    var selectedStatus by remember { mutableStateOf("PRESENT") }
    var noteInput by remember { mutableStateOf("") }

    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-DD", Locale.getDefault()).format(Date()) }
    val todayDisplayDate = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date()) }

    // Filter staff list
    val filteredUsers = remember(allUsers, searchQuery) {
        if (searchQuery.isBlank()) allUsers
        else allUsers.filter { it.name.contains(searchQuery, ignoreCase = true) || it.username.contains(searchQuery, ignoreCase = true) }
    }

    // Today's attendance records map
    val todayRecordsMap = remember(allAttendance, todayDateStr) {
        allAttendance.filter { it.dateStr == todayDateStr }.associateBy { it.userId }
    }

    // Stats calculations
    val totalStaffCount = allUsers.size
    val presentCount = todayRecordsMap.values.count { it.status == "PRESENT" || it.status == "LATE" }
    val absentCount = todayRecordsMap.values.count { it.status == "ABSENT" }
    val leaveCount = todayRecordsMap.values.count { it.status == "LEAVE" }
    val unmarkedCount = (totalStaffCount - (presentCount + absentCount + leaveCount)).coerceAtLeast(0)

    val currentMyRecord = todayRecordsMap[currentUser?.id ?: -1L]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Employee Attendance & Shift Tracker",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = todayDisplayDate,
                            fontSize = 12.sp,
                            color = GoldLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showToast("Attendance sheet updated") }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = GoldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // MY QUICK CHECK-IN BAR
            currentUser?.let { user ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = NavyDark,
                    border = BorderStroke(1.dp, GoldAccent)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(GoldAccent.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Badge, contentDescription = null, tint = GoldLight)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = user.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Role: ${user.role.replace("_", " ")}",
                                        fontSize = 11.sp,
                                        color = GoldLight
                                    )
                                }
                            }

                            val myStatus = currentMyRecord?.status ?: "NOT_CHECKED_IN"
                            val statusBg = when (myStatus) {
                                "PRESENT", "LATE" -> Color(0xFF16A34A)
                                "ABSENT" -> Color(0xFFDC2626)
                                "LEAVE" -> Color(0xFFD97706)
                                else -> Color(0xFF64748B)
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = statusBg
                            ) {
                                Text(
                                    text = if (myStatus == "NOT_CHECKED_IN") "Not Checked In" else myStatus,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.checkInEmployee(user.id, user.name, user.role)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Check In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    viewModel.checkOutEmployee(user.id)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Check Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (currentMyRecord?.breakStartTime == null || currentMyRecord.breakEndTime != null) {
                                        viewModel.startBreakEmployee(user.id)
                                    } else {
                                        viewModel.endBreakEmployee(user.id)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, GoldAccent),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight)
                            ) {
                                Icon(Icons.Default.FreeBreakfast, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentMyRecord?.breakStartTime != null && currentMyRecord.breakEndTime == null) "End Break" else "Break",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // SUMMARY CARDS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AttendanceStatCard("Present", presentCount.toString(), Color(0xFF16A34A), Modifier.weight(1f))
                AttendanceStatCard("Absent", absentCount.toString(), Color(0xFFDC2626), Modifier.weight(1f))
                AttendanceStatCard("Leave", leaveCount.toString(), Color(0xFFD97706), Modifier.weight(1f))
                AttendanceStatCard("Unmarked", unmarkedCount.toString(), Color(0xFF64748B), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TAB SELECTOR
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = NavyDark
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Today's Sheet", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("All Staff Roster", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Logs & Reports", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            // SEARCH BAR
            if (selectedTab == 0 || selectedTab == 1) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    placeholder = { Text("Search staff by name or username...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            // CONTENT BODY
            when (selectedTab) {
                0 -> {
                    // TODAY'S ATTENDANCE LIST
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(filteredUsers, key = { it.id }) { user ->
                            val record = todayRecordsMap[user.id]
                            StaffAttendanceRow(
                                user = user,
                                record = record,
                                onMarkClick = {
                                    showMarkDialog = user
                                },
                                onQuickCheckIn = {
                                    viewModel.checkInEmployee(user.id, user.name, user.role)
                                },
                                onQuickCheckOut = {
                                    viewModel.checkOutEmployee(user.id)
                                }
                            )
                        }
                    }
                }
                1 -> {
                    // ALL STAFF ROSTER
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(filteredUsers, key = { it.id }) { user ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Role: ${user.role} • Username: ${user.username}", fontSize = 12.sp, color = Color.Gray)
                                        if (user.phone.isNotBlank()) {
                                            Text("Phone: ${user.phone}", fontSize = 11.sp, color = NavyLight)
                                        }
                                    }

                                    Button(
                                        onClick = { showMarkDialog = user },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NavyDark)
                                    ) {
                                        Text("Mark Status", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // HISTORY & LOGS
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, GoldAccent)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("ATTENDANCE HISTORY SUMMARY", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Total Records Stored: ${allAttendance.size}", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }

                        if (allAttendance.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No historical attendance records found.", color = Color.Gray)
                                }
                            }
                        } else {
                            items(allAttendance, key = { it.id }) { rec ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(rec.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(rec.dateStr, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GoldAccent)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Status: ${rec.status}", fontSize = 12.sp, color = if (rec.status == "PRESENT") Color(0xFF16A34A) else Color(0xFFDC2626))
                                            val inTimeStr = rec.checkInTime?.let { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it)) } ?: "--"
                                            val outTimeStr = rec.checkOutTime?.let { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it)) } ?: "--"
                                            Text("In: $inTimeStr | Out: $outTimeStr", fontSize = 11.sp, color = Color.Gray)
                                        }
                                        if (rec.notes.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("Note: ${rec.notes}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // MARK ATTENDANCE DIALOG
    showMarkDialog?.let { user ->
        AlertDialog(
            onDismissRequest = { showMarkDialog = null },
            title = {
                Text("Mark Attendance - ${user.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select attendance status for today:", fontSize = 13.sp, color = Color.Gray)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedStatus == "PRESENT",
                            onClick = { selectedStatus = "PRESENT" },
                            label = { Text("Present") }
                        )
                        FilterChip(
                            selected = selectedStatus == "ABSENT",
                            onClick = { selectedStatus = "ABSENT" },
                            label = { Text("Absent") }
                        )
                        FilterChip(
                            selected = selectedStatus == "LEAVE",
                            onClick = { selectedStatus = "LEAVE" },
                            label = { Text("Leave") }
                        )
                        FilterChip(
                            selected = selectedStatus == "LATE",
                            onClick = { selectedStatus = "LATE" },
                            label = { Text("Late") }
                        )
                    }

                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Optional Notes / Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markEmployeeAttendanceStatus(
                            userId = user.id,
                            userName = user.name,
                            userRole = user.role,
                            status = selectedStatus,
                            notes = noteInput
                        )
                        showMarkDialog = null
                        noteInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyDark)
                ) {
                    Text("Save Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMarkDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AttendanceStatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = color)
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = NavyDark)
        }
    }
}

@Composable
private fun StaffAttendanceRow(
    user: UserAccount,
    record: AttendanceRecord?,
    onMarkClick: () -> Unit,
    onQuickCheckIn: () -> Unit,
    onQuickCheckOut: () -> Unit
) {
    val status = record?.status ?: "NOT_MARKED"
    val (statusText, statusBg) = when (status) {
        "PRESENT" -> "Present" to Color(0xFF16A34A)
        "ABSENT" -> "Absent" to Color(0xFFDC2626)
        "LEAVE" -> "On Leave" to Color(0xFFD97706)
        "LATE" -> "Late Arrival" to Color(0xFF9333EA)
        else -> "Unmarked" to Color(0xFF64748B)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NavyDark.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(user.role.replace("_", " "), fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = statusBg.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusBg.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusBg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val inTimeStr = record?.checkInTime?.let { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it)) } ?: "Not Checked In"
            val outTimeStr = record?.checkOutTime?.let { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it)) } ?: "Active Shift"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Check-in: $inTimeStr  |  Check-out: $outTimeStr",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onQuickCheckIn,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Login, contentDescription = "Check In", tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onQuickCheckOut,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Check Out", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onMarkClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Status", tint = NavyDark, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
