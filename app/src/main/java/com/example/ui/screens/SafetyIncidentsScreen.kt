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
fun SafetyIncidentsScreen(
    repository: MineGuardRepository,
    onOpenReportSafety: () -> Unit
) {
    val incidents by repository.safetyIncidents.collectAsState()
    val mines by repository.mines.collectAsState()

    var incidentToUpdate by remember { mutableStateOf<SafetyIncident?>(null) }
    var nextStatus by remember { mutableStateOf(ActionStatus.IN_PROGRESS) }
    var statusNotes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CloudBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Emergency Disclaimer Banner
        Surface(
            color = CriticalRed.copy(alpha = 0.10f),
            shape = RoundedCornerShape(10.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CriticalRed.copy(alpha = 0.4f)))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = CriticalRed, modifier = Modifier.size(24.dp))
                Column {
                    Text(
                        text = "EMERGENCY RESPONSE PROTOCOL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CriticalRed
                    )
                    Text(
                        text = "Digital reporting is for audit & tracking. For active pit emergencies, immediately trigger the colliery audible alarm and mandatory evacuation SOP.",
                        fontSize = 11.sp,
                        color = MainText,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Safety Observations & Incidents",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MainText
                )
                Text(
                    text = "${incidents.size} Observations Logged • Immediate Actions & Escalation",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }

            Button(
                onClick = onOpenReportSafety,
                colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("report_hazard_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Report Hazard", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
            }
        }

        // Incident Cards
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(incidents) { inc ->
                val mineName = mines.find { it.id == inc.mineId }?.name ?: inc.mineId

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("incident_card_${inc.id.lowercase()}"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (inc.severity == IssueSeverity.CRITICAL) CriticalRed.copy(alpha = 0.5f) else BorderColor
                        )
                    )
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = inc.category.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MainText
                                )
                                Text(
                                    text = "$mineName • ${inc.location}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryText
                                )
                            }
                            SeverityChip(severity = inc.severity)
                        }

                        Text(
                            text = inc.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MainText,
                            lineHeight = 18.sp
                        )

                        // Immediate Action
                        Surface(
                            color = CloudBackground,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.FlashOn, contentDescription = null, tint = SafetyOrange, modifier = Modifier.size(16.dp))
                                Column {
                                    Text("Immediate Action Taken:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                    Text(inc.immediateActionTaken, fontSize = 11.sp, color = MainText)
                                }
                            }
                        }

                        // Status and Assigned Officer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Assigned Officer", fontSize = 11.sp, color = SecondaryText)
                                Text(inc.assignedOfficer, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MainText)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Corrective Action Status", fontSize = 11.sp, color = SecondaryText)
                                StatusBadge(
                                    text = inc.correctiveActionStatus.label,
                                    containerColor = when (inc.correctiveActionStatus) {
                                        ActionStatus.OPEN -> CriticalRed.copy(alpha = 0.15f)
                                        ActionStatus.IN_PROGRESS -> WarningAmber.copy(alpha = 0.15f)
                                        ActionStatus.RESOLVED -> Teal.copy(alpha = 0.15f)
                                        ActionStatus.VERIFIED_CLOSED -> SuccessGreen.copy(alpha = 0.15f)
                                    },
                                    contentColor = when (inc.correctiveActionStatus) {
                                        ActionStatus.OPEN -> CriticalRed
                                        ActionStatus.IN_PROGRESS -> WarningAmber
                                        ActionStatus.RESOLVED -> Teal
                                        ActionStatus.VERIFIED_CLOSED -> SuccessGreen
                                    }
                                )
                            }
                        }

                        if (inc.closureNotes.isNotBlank()) {
                            Text("Resolution History: ${inc.closureNotes}", fontSize = 11.sp, color = SecondaryText)
                        }

                        // Action button
                        if (inc.correctiveActionStatus != ActionStatus.VERIFIED_CLOSED) {
                            OutlinedButton(
                                onClick = {
                                    incidentToUpdate = inc
                                    nextStatus = when (inc.correctiveActionStatus) {
                                        ActionStatus.OPEN -> ActionStatus.IN_PROGRESS
                                        ActionStatus.IN_PROGRESS -> ActionStatus.RESOLVED
                                        ActionStatus.RESOLVED -> ActionStatus.VERIFIED_CLOSED
                                        else -> ActionStatus.VERIFIED_CLOSED
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepNavy),
                                modifier = Modifier.fillMaxWidth().testTag("update_hazard_${inc.id.lowercase()}")
                            ) {
                                Text("Update Status (${inc.correctiveActionStatus.label} → Next Stage)")
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Update Status Dialog
    incidentToUpdate?.let { inc ->
        AlertDialog(
            onDismissRequest = { incidentToUpdate = null },
            title = { Text("Update Corrective Action Status", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Current Observation: ${inc.description.take(60)}...", fontSize = 12.sp, color = SecondaryText)
                    Text("Select Target Status:", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(ActionStatus.IN_PROGRESS, ActionStatus.RESOLVED, ActionStatus.VERIFIED_CLOSED).forEach { st ->
                            FilterChip(
                                selected = nextStatus == st,
                                onClick = { nextStatus = st },
                                label = { Text(st.label, fontSize = 10.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = statusNotes,
                        onValueChange = { statusNotes = it },
                        label = { Text("Action Taken / Resolution Notes *") },
                        modifier = Modifier.fillMaxWidth().testTag("status_notes_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.updateIncidentStatus(inc.id, nextStatus, statusNotes)
                        incidentToUpdate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                ) {
                    Text("Update Status")
                }
            },
            dismissButton = {
                TextButton(onClick = { incidentToUpdate = null }) { Text("Cancel") }
            }
        )
    }
}
