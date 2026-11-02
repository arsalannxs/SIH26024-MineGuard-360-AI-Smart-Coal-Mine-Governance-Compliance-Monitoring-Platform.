package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.MineGuardRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    repository: MineGuardRepository,
    onOpenRoleSwitcher: () -> Unit,
    onOpenDemoWalkthrough: () -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val isOffline by repository.isOfflineMode.collectAsState()

    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var resetSuccessNotice by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CloudBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "System Governance & Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MainText
            )
            Text(
                text = "Role-based authorization, demonstration controls, and network mode",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText
            )
        }

        // Active Role Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("settings_active_role_card"),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderColor))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = DeepNavy)
                            Text("Active User & Role", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepNavy)
                        }
                        StatusBadge(text = currentUser.role.displayName, containerColor = LightTeal, contentColor = Teal)
                    }

                    Text("Officer: ${currentUser.name} • ${currentUser.subsidiary}", fontSize = 12.sp, color = MainText)
                    Text("Permissions: ${currentUser.role.description}", fontSize = 11.sp, color = SecondaryText)

                    Button(
                        onClick = onOpenRoleSwitcher,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                        modifier = Modifier.fillMaxWidth().testTag("switch_demo_role_btn")
                    ) {
                        Icon(Icons.Default.SwitchAccount, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Switch Demo Role (9 Roles Available)")
                    }
                }
            }
        }

        // 3-Minute SIH Demo Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SafetyOrange.copy(alpha = 0.5f)))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = SafetyOrange)
                        Text("Smart India Hackathon 3-Minute Demo", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepNavy)
                    }

                    Text(
                        text = "Follow the guided 14-step presentation walkthrough showcasing compliance overdue triage, inspection checklist, hazard reporting, environmental breaches, AI grounded query, and audit trail.",
                        fontSize = 12.sp,
                        color = SecondaryText,
                        lineHeight = 16.sp
                    )

                    Button(
                        onClick = onOpenDemoWalkthrough,
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                        modifier = Modifier.fillMaxWidth().testTag("launch_sih_walkthrough_btn")
                    ) {
                        Text("Launch Guided Walkthrough", color = DeepNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Offline Network Toggle Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderColor))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Offline Field Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MainText)
                        Text(
                            "Simulates underground or remote pit network severance with local draft queueing.",
                            fontSize = 11.sp,
                            color = SecondaryText
                        )
                    }
                    Switch(
                        checked = isOffline,
                        onCheckedChange = { repository.toggleOfflineMode() },
                        colors = SwitchDefaults.colors(checkedThumbColor = SafetyOrange, checkedTrackColor = SafetyOrange.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("offline_mode_toggle")
                    )
                }
            }
        }

        // Reset Demo Baseline Data Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderColor))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Demo Data Management", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MainText)
                    Text(
                        "Restore verified baseline records across all 5 collieries, inspections, and audit logs.",
                        fontSize = 11.sp,
                        color = SecondaryText
                    )

                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CriticalRed),
                        modifier = Modifier.fillMaxWidth().testTag("reset_demo_data_btn")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset System to Evaluation Baseline")
                    }

                    if (resetSuccessNotice) {
                        Text("✓ System successfully reset to baseline!", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Official SIH26024 Metadata & Attribution Card
        item {
            Surface(
                color = CloudBackground,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Smart India Hackathon (SIH26024)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                    Text("Ministry: Ministry of Coal • Department: Coal India Limited (CIL)", fontSize = 11.sp, color = SecondaryText)
                    Text("Category: Software • Theme: Smart Automation", fontSize = 11.sp, color = SecondaryText)
                    Text("Applet ID: 4b51a6e7-3819-4092-85fb-a63f1f3b3b6f", fontSize = 10.sp, color = Slate)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Notice: All colliery names, telemetry sensor figures and compliance records in this demonstration are illustrative models created for SIH26024 prototype evaluation.",
                        fontSize = 10.sp,
                        color = SecondaryText,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset Demonstration Data?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This will restore the standard SIH demonstration dataset, reload all baseline compliance items, and append a reset event to the audit trail.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.resetDemoData()
                        showResetConfirmDialog = false
                        resetSuccessNotice = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CriticalRed)
                ) {
                    Text("Reset Baseline")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}
