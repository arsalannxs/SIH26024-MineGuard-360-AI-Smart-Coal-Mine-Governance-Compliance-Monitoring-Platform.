package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.MineGuardRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun WorkforceScreen(
    repository: MineGuardRepository
) {
    val workforceRecords by repository.workforceRecords.collectAsState()
    val mines by repository.mines.collectAsState()

    var showLogDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CloudBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Workforce & Shift Attendance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MainText
                )
                Text(
                    text = "${workforceRecords.size} Active Roster Entries • Shift Biometric & Breathalyzer Checks",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }

            Button(
                onClick = { showLogDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("log_attendance_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Log Entry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Summary cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val presentCount = workforceRecords.count { it.status == AttendanceStatus.PRESENT }
            val leaveCount = workforceRecords.count { it.status == AttendanceStatus.ON_LEAVE }

            MetricCard(
                title = "Total On Shift",
                value = "$presentCount",
                subtitle = "Passed Pre-shift BAC",
                icon = Icons.Default.Groups,
                iconTint = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "On Leave / Rest",
                value = "$leaveCount",
                subtitle = "Statutory Rest Day",
                icon = Icons.Default.EventBusy,
                iconTint = Slate,
                modifier = Modifier.weight(1f)
            )
        }

        // Records List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(workforceRecords) { wf ->
                val mineName = mines.find { it.id == wf.mineId }?.name ?: wf.mineId

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("workforce_card_${wf.id.lowercase()}"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderColor))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = wf.workerName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MainText
                                )
                                Text(
                                    text = "ID: ${wf.workerId} • $mineName",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryText
                                )
                            }
                            StatusBadge(
                                text = wf.status.label,
                                containerColor = if (wf.status == AttendanceStatus.PRESENT) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                                contentColor = if (wf.status == AttendanceStatus.PRESENT) SuccessGreen else WarningAmber
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Shift: ${wf.shift}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DeepNavy)
                            Text("Check-in: ${wf.checkInTime ?: "--"}", fontSize = 12.sp, color = SecondaryText)
                        }

                        Surface(
                            color = CloudBackground,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                Text(wf.supervisorRemarks, fontSize = 11.sp, color = MainText)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showLogDialog) {
        var name by remember { mutableStateOf("") }
        var empId by remember { mutableStateOf("EMP-9080") }
        var remarks by remember { mutableStateOf("Biometric verified. Zero BAC on alcohol breathalyzer.") }

        AlertDialog(
            onDismissRequest = { showLogDialog = false },
            title = { Text("Log Worker Shift Attendance", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Worker Full Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("worker_name_input")
                    )
                    OutlinedTextField(
                        value = empId,
                        onValueChange = { empId = it },
                        label = { Text("Worker Employee ID *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Supervisor Remarks / Safety Verification") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val rec = WorkforceRecord(
                                id = "WF-${System.currentTimeMillis() % 10000}",
                                workerId = empId,
                                workerName = name,
                                contractorId = "CONTR-2026-01",
                                mineId = mines.firstOrNull()?.id ?: "DEMO-MINE-01",
                                shift = "Shift A (06:00 - 14:00)",
                                attendanceDate = "2026-11-02",
                                status = AttendanceStatus.PRESENT,
                                checkInTime = "06:05 AM",
                                checkOutTime = null,
                                supervisorRemarks = remarks
                            )
                            repository.logAttendance(rec)
                            showLogDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                ) {
                    Text("Save Record")
                }
            },
            dismissButton = { TextButton(onClick = { showLogDialog = false }) { Text("Cancel") } }
        )
    }
}
