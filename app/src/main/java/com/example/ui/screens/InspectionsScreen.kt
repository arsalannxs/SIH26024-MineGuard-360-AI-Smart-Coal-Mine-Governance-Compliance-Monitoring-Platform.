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
fun InspectionsScreen(
    repository: MineGuardRepository,
    onOpenScheduleInspection: () -> Unit
) {
    val inspections by repository.inspections.collectAsState()
    val mines by repository.mines.collectAsState()

    var inspectionToComplete by remember { mutableStateOf<Inspection?>(null) }
    var completeFindings by remember { mutableStateOf("") }
    var completeAction by remember { mutableStateOf("") }
    var completeSeverity by remember { mutableStateOf(IssueSeverity.MEDIUM) }

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
                    text = "Mine Inspection Management",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MainText
                )
                Text(
                    text = "${inspections.size} Inspections • Statutory Checklists & Action Tracking",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }

            Button(
                onClick = onOpenScheduleInspection,
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("schedule_inspection_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Schedule", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Inspections List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(inspections) { item ->
                val mineName = mines.find { it.id == item.mineId }?.name ?: item.mineId

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inspection_card_${item.id.lowercase()}"),
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.category.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MainText
                                )
                                Text(
                                    text = "$mineName • Inspector: ${item.inspectorName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryText
                                )
                            }
                            SeverityChip(severity = item.severity)
                        }

                        // Dates and Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Scheduled: ${item.scheduledDate}", fontSize = 12.sp, color = SecondaryText)
                            StatusBadge(
                                text = item.status.label,
                                containerColor = if (item.status == InspectionStatus.COMPLETED) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                                contentColor = if (item.status == InspectionStatus.COMPLETED) SuccessGreen else WarningAmber
                            )
                        }

                        // Observations
                        if (item.observations.isNotBlank()) {
                            Surface(
                                color = CloudBackground,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Observations & Site Findings:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                    Text(item.observations, fontSize = 12.sp, color = MainText)
                                }
                            }
                        }

                        // Checklist items preview
                        if (item.checklist.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Checklist Enforcement (${item.checklist.size} points):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecondaryText)
                                item.checklist.forEach { chk ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (chk.isPassed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                            contentDescription = null,
                                            tint = if (chk.isPassed) SuccessGreen else CriticalRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text("${chk.title} - ${chk.comments}", fontSize = 11.sp, color = MainText)
                                    }
                                }
                            }
                        }

                        // Corrective Action
                        if (item.correctiveAction.isNotBlank()) {
                            Surface(
                                color = LightTeal,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Build, contentDescription = null, tint = Teal, modifier = Modifier.size(14.dp))
                                    Column {
                                        Text("Corrective Action Mandate:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Teal)
                                        Text(item.correctiveAction, fontSize = 11.sp, color = DeepNavy)
                                    }
                                }
                            }
                        }

                        // Action button if not completed
                        if (item.status != InspectionStatus.COMPLETED) {
                            Button(
                                onClick = {
                                    inspectionToComplete = item
                                    completeFindings = item.observations
                                    completeAction = item.correctiveAction
                                    completeSeverity = item.severity
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                                modifier = Modifier.fillMaxWidth().testTag("complete_inspection_${item.id.lowercase()}")
                            ) {
                                Text("Record Field Findings & Complete")
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Complete Inspection Dialog
    inspectionToComplete?.let { ins ->
        AlertDialog(
            onDismissRequest = { inspectionToComplete = null },
            title = { Text("Complete Inspection Findings", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Inspector: ${ins.inspectorName} (${ins.category.label})", fontSize = 12.sp, color = SecondaryText)
                    OutlinedTextField(
                        value = completeFindings,
                        onValueChange = { completeFindings = it },
                        label = { Text("On-site Observations & Findings *") },
                        modifier = Modifier.fillMaxWidth().testTag("complete_findings_input")
                    )
                    OutlinedTextField(
                        value = completeAction,
                        onValueChange = { completeAction = it },
                        label = { Text("Assigned Corrective Action *") },
                        modifier = Modifier.fillMaxWidth().testTag("complete_action_input")
                    )
                    Text("Severity Level", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IssueSeverity.values().forEach { s ->
                            FilterChip(
                                selected = completeSeverity == s,
                                onClick = { completeSeverity = s },
                                label = { Text(s.label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.completeInspection(ins.id, completeFindings, completeAction, completeSeverity)
                        inspectionToComplete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                ) {
                    Text("Save & Close Inspection")
                }
            },
            dismissButton = {
                TextButton(onClick = { inspectionToComplete = null }) { Text("Cancel") }
            }
        )
    }
}
