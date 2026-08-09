package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.StoreViewModel

@Composable
fun CustomerActivationScreen(
    viewModel: StoreViewModel,
    onActivated: () -> Unit,
    onOpenDeveloperPortal: () -> Unit
) {
    val context = LocalContext.current
    var inputLicenseKey by remember { mutableStateOf("") }
    var selectedTrialDays by remember { mutableStateOf(15) }
    var isSubmitting by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = License Key, 1 = Free Trial

    var showDevPasscodeDialog by remember { mutableStateOf(false) }
    var devPasscodeInput by remember { mutableStateOf("") }
    var logoTapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }

    val handleLogoTap = {
        val now = System.currentTimeMillis()
        if (now - lastTapTime > 2000) {
            logoTapCount = 1
        } else {
            logoTapCount++
        }
        lastTapTime = now

        if (logoTapCount >= 10) {
            logoTapCount = 0
            showDevPasscodeDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F2537),
                        Color(0xFF1E3A5F),
                        Color(0xFF0F172A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo Header (Hidden 10-tap developer trigger)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
                    .clickable { handleLogoTap() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "COMMERCIAL STORE POS",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Text(
                text = "Play Store Ready Multi-Store SaaS Solution",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Activation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "App Activation Required",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Activate your business software to continue using POS features.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mode Selection Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(4.dp)
                    ) {
                        Surface(
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedTab == 0) Color(0xFF0F2537) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Key,
                                    contentDescription = null,
                                    tint = if (selectedTab == 0) Color.White else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "1. License Key",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (selectedTab == 0) Color.White else Color.Gray
                                )
                            }
                        }

                        Surface(
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedTab == 1) Color(0xFF0F2537) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (selectedTab == 1) Color.White else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "2. Free Trial",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (selectedTab == 1) Color.White else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (selectedTab == 0) {
                        // Enter License Key Option
                        Text(
                            text = "Enter Commercial License Key:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = inputLicenseKey,
                            onValueChange = { inputLicenseKey = it },
                            placeholder = { Text("e.g. SAAS-POS-AN-2026-X9A2-7B4K", fontFamily = FontFamily.Monospace) },
                            leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF2563EB)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "License key provided by application developer after purchase.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (inputLicenseKey.isNotBlank()) {
                                    isSubmitting = true
                                    viewModel.activateLicenseWithKey(inputLicenseKey) { success, msg ->
                                        isSubmitting = false
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        if (success) {
                                            onActivated()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            enabled = inputLicenseKey.isNotBlank() && !isSubmitting,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isSubmitting) "Verifying License..." else "Activate License Key", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Start Free Trial Option
                        Text(
                            text = "Select Trial Duration:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val trialOptions = listOf(7, 15, 30)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            trialOptions.forEach { days ->
                                Surface(
                                    onClick = { selectedTrialDays = days },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (selectedTrialDays == days) 2.dp else 1.dp,
                                        color = if (selectedTrialDays == days) Color(0xFF10B981) else Color(0xFFCBD5E1)
                                    ),
                                    color = if (selectedTrialDays == days) Color(0xFFECFDF5) else Color.White
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "$days Days",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (selectedTrialDays == days) Color(0xFF047857) else Color(0xFF334155)
                                        )
                                        Text(
                                            "Free Trial",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Enjoy $selectedTrialDays days of full features. No credit card required.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                isSubmitting = true
                                viewModel.startFreeTrial(selectedTrialDays) { success, msg ->
                                    isSubmitting = false
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    if (success) {
                                        onActivated()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            enabled = !isSubmitting,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isSubmitting) "Starting Trial..." else "Start $selectedTrialDays-Day Free Trial", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

        }
    }

    // Developer Security Master Password Dialog
    if (showDevPasscodeDialog) {
        AlertDialog(
            onDismissRequest = { showDevPasscodeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeveloperMode, contentDescription = null, tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Developer Master Verification")
                }
            },
            text = {
                Column {
                    Text(
                        "Enter Developer Master Password to proceed.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = devPasscodeInput,
                        onValueChange = { devPasscodeInput = it },
                        label = { Text("Developer Master Password") },
                        placeholder = { Text("Enter Master Passcode (e.g. 9999)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (devPasscodeInput.trim() == "9999" || devPasscodeInput.trim() == "AK-9999" || devPasscodeInput.trim().uppercase() == "DEV-ADMIN") {
                            showDevPasscodeDialog = false
                            devPasscodeInput = ""
                            viewModel.loginAsMasterDeveloper()
                            onOpenDeveloperPortal()
                        } else {
                            Toast.makeText(context, "Access Denied: Invalid Master Password!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Verify Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDevPasscodeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
