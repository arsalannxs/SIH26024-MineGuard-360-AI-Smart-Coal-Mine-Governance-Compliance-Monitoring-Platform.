package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddComplianceDialog(
    mines: List<Mine>,
    onAdd: (ComplianceItem) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMineId by remember { mutableStateOf(mines.firstOrNull()?.id ?: "DEMO-MINE-01") }
    var title by remember { mutableStateOf("") }
    var regulation by remember { mutableStateOf("DGMS Coal Mines Regulations 2017") }
    var department by remember { mutableStateOf("Safety & Engineering") }
    var officer by remember { mutableStateOf("Rajesh Sharma") }
    var dueDate by remember { mutableStateOf("2026-11-20") }
    var category by remember { mutableStateOf(ComplianceCategory.MINE_SAFETY) }
    var evidence by remember { mutableStateOf("Photographic proof and telemetry flow log") }
    var remarks by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Statutory Compliance Record", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Requirement Title *") },
                    placeholder = { Text("e.g. Continuous Dust Suppression System") },
                    modifier = Modifier.fillMaxWidth().testTag("compliance_title_input")
                )

                OutlinedTextField(
                    value = regulation,
                    onValueChange = { regulation = it },
                    label = { Text("Applicable Regulation / Legal Reference *") },
                    modifier = Modifier.fillMaxWidth().testTag("compliance_reg_input")
                )

                // Category selector
                Text("Compliance Category", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(ComplianceCategory.MINE_SAFETY, ComplianceCategory.ENVIRONMENTAL, ComplianceCategory.LABOUR_WORKFORCE).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.name.substring(0, 4), fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = officer,
                    onValueChange = { officer = it },
                    label = { Text("Responsible Officer") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Statutory Due Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth().testTag("compliance_due_input")
                )

                OutlinedTextField(
                    value = evidence,
                    onValueChange = { evidence = it },
                    label = { Text("Required Evidence Type") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks & Operational Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val newItem = ComplianceItem(
                            id = "CMP-${System.currentTimeMillis() % 10000}",
                            mineId = selectedMineId,
                            category = category,
                            requirementTitle = title,
                            description = "Statutory mandate under $regulation",
                            applicableRegulation = regulation,
                            responsibleDepartment = department,
                            responsibleOfficer = officer,
                            applicableDate = "2026-11-01",
                            dueDate = dueDate,
                            reviewFrequency = "Monthly",
                            requiredEvidence = evidence,
                            status = ComplianceStatus.ACTION_REQUIRED,
                            lastReviewDate = "2026-11-01",
                            nextReviewDate = dueDate,
                            evidenceDocumentUrl = "https://sih.mineguard.internal/docs/new-item.pdf",
                            remarks = remarks
                        )
                        onAdd(newItem)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                modifier = Modifier.testTag("save_compliance_button")
            ) {
                Text("Create Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ReportSafetyDialog(
    mines: List<Mine>,
    onReport: (SafetyIncident) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMineId by remember { mutableStateOf(mines.firstOrNull()?.id ?: "DEMO-MINE-01") }
    var location by remember { mutableStateOf("Haul Road Section 2") }
    var category by remember { mutableStateOf(IncidentCategory.UNSAFE_CONDITION) }
    var severity by remember { mutableStateOf(IssueSeverity.HIGH) }
    var description by remember { mutableStateOf("") }
    var immediateAction by remember { mutableStateOf("Area cordoned and supervisor notified") }
    var reporterName by remember { mutableStateOf("Field Safety Officer") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = SafetyOrange)
                Text("Report Safety Observation / Hazard", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Disclaimer: Digital reporting supports audit & tracking. For active emergencies, immediately trigger the colliery audible alarm and emergency evacuation SOP.",
                    style = MaterialTheme.typography.labelSmall,
                    color = CriticalRed,
                    lineHeight = 14.sp
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Hazard / Incident Description *") },
                    placeholder = { Text("Describe observed condition, vehicle/machine ID or strata crack...") },
                    modifier = Modifier.fillMaxWidth().testTag("incident_desc_input")
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Colliery Location / Bench / Pit *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Severity Level", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IssueSeverity.values().forEach { sev ->
                        FilterChip(
                            selected = severity == sev,
                            onClick = { severity = sev },
                            label = { Text(sev.label, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = immediateAction,
                    onValueChange = { immediateAction = it },
                    label = { Text("Immediate Action Taken") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reporterName,
                    onValueChange = { reporterName = it },
                    label = { Text("Reporter Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank()) {
                        val newInc = SafetyIncident(
                            id = "INC-${System.currentTimeMillis() % 10000}",
                            mineId = selectedMineId,
                            location = location,
                            category = category,
                            description = description,
                            reportedDate = "2026-11-02",
                            reportedTime = "09:30 AM",
                            reporterName = reporterName,
                            severity = severity,
                            immediateActionTaken = immediateAction,
                            assignedOfficer = "Mine Safety Manager",
                            correctiveActionStatus = ActionStatus.OPEN,
                            closureNotes = "Auto-escalated for immediate inspection."
                        )
                        onReport(newInc)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (severity == IssueSeverity.CRITICAL) CriticalRed else SafetyOrange),
                modifier = Modifier.testTag("submit_incident_button")
            ) {
                Text("Log Observation")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ScheduleInspectionDialog(
    mines: List<Mine>,
    onSchedule: (Inspection) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMineId by remember { mutableStateOf(mines.firstOrNull()?.id ?: "DEMO-MINE-01") }
    var inspector by remember { mutableStateOf("DGMS Regional Inspector") }
    var scheduledDate by remember { mutableStateOf("2026-11-12") }
    var category by remember { mutableStateOf(InspectionCategory.SAFETY) }
    var observations by remember { mutableStateOf("Pre-winter pit bench strata and lighting statutory check") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Official Mine Inspection", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = inspector,
                    onValueChange = { inspector = it },
                    label = { Text("Designated Inspector Name & Title *") },
                    modifier = Modifier.fillMaxWidth().testTag("inspection_inspector_input")
                )

                OutlinedTextField(
                    value = scheduledDate,
                    onValueChange = { scheduledDate = it },
                    label = { Text("Scheduled Inspection Date (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Inspection Scope", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(InspectionCategory.SAFETY, InspectionCategory.ENVIRONMENTAL, InspectionCategory.EQUIPMENT).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.label.substring(0, 8), fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = observations,
                    onValueChange = { observations = it },
                    label = { Text("Scope / Target Directives") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inspector.isNotBlank()) {
                        val newInsp = Inspection(
                            id = "INSP-${System.currentTimeMillis() % 10000}",
                            mineId = selectedMineId,
                            category = category,
                            inspectorName = inspector,
                            scheduledDate = scheduledDate,
                            actualDate = null,
                            status = InspectionStatus.SCHEDULED,
                            checklist = listOf(
                                InspectionChecklistItem("CHK-01", "Statutory pit slope stability validation", true, "Pending on-site verification"),
                                InspectionChecklistItem("CHK-02", "Emergency communication & sirens tested", true, "Pending on-site verification"),
                                InspectionChecklistItem("CHK-03", "Dust suppression & air velocity compliant", true, "Pending on-site verification")
                            ),
                            observations = observations,
                            severity = IssueSeverity.MEDIUM,
                            correctiveAction = "Address all failed checklist findings within 72 hours",
                            responsiblePerson = "Er. A. K. Verma",
                            targetCompletionDate = scheduledDate,
                            reviewStatus = "Scheduled on CIL Portal"
                        )
                        onSchedule(newInsp)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                modifier = Modifier.testTag("confirm_schedule_inspection")
            ) {
                Text("Schedule Inspection")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddEnvironmentalReadingDialog(
    mines: List<Mine>,
    onAdd: (EnvironmentalReading) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMineId by remember { mutableStateOf(mines.firstOrNull()?.id ?: "DEMO-MINE-01") }
    var metric by remember { mutableStateOf(EnvironmentalMetric.PM10) }
    var valueStr by remember { mutableStateOf("105.0") }
    var location by remember { mutableStateOf("Boundary Monitoring Station #2") }
    var instrument by remember { mutableStateOf("Continuous Ambient BAM-1020 Monitor") }
    var officer by remember { mutableStateOf("Ananya Das (Env Officer)") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Environmental Telemetry", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Measurement Metric", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(EnvironmentalMetric.PM10, EnvironmentalMetric.PM25, EnvironmentalMetric.WATER_PH, EnvironmentalMetric.NOISE_LEVEL).forEach { m ->
                        FilterChip(
                            selected = metric == m,
                            onClick = { metric = m },
                            label = { Text(m.name, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = valueStr,
                    onValueChange = { valueStr = it },
                    label = { Text("Observed Value (${metric.defaultUnit}) *") },
                    supportingText = { Text("Safe statutory limit: ${metric.safeLimit}") },
                    modifier = Modifier.fillMaxWidth().testTag("env_val_input")
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Sampling Station / Location *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = instrument,
                    onValueChange = { instrument = it },
                    label = { Text("Instrument / Source Device") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val v = valueStr.toDoubleOrNull() ?: 0.0
                    val isBreach = when (metric) {
                        EnvironmentalMetric.PM10 -> v > 100.0
                        EnvironmentalMetric.PM25 -> v > 60.0
                        EnvironmentalMetric.SO2 -> v > 80.0
                        EnvironmentalMetric.NO2 -> v > 80.0
                        EnvironmentalMetric.WATER_PH -> v < 6.5 || v > 8.5
                        EnvironmentalMetric.WATER_TSS -> v > 100.0
                        EnvironmentalMetric.NOISE_LEVEL -> v > 75.0
                        else -> false
                    }
                    val item = EnvironmentalReading(
                        id = "ENV-${System.currentTimeMillis() % 10000}",
                        mineId = selectedMineId,
                        measurementType = metric,
                        value = v,
                        unit = metric.defaultUnit,
                        thresholdMin = if (metric == EnvironmentalMetric.WATER_PH) 6.5 else 0.0,
                        thresholdMax = when (metric) {
                            EnvironmentalMetric.PM10 -> 100.0
                            EnvironmentalMetric.PM25 -> 60.0
                            EnvironmentalMetric.WATER_PH -> 8.5
                            EnvironmentalMetric.NOISE_LEVEL -> 75.0
                            else -> 100.0
                        },
                        isBreach = isBreach,
                        measurementDate = "2026-11-02 09:45",
                        measurementLocation = location,
                        instrumentOrSource = instrument,
                        responsiblePerson = officer,
                        remarks = if (isBreach) "Exceeded statutory safe limit ${metric.safeLimit}" else "Compliant with CPCB limits"
                    )
                    onAdd(item)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                modifier = Modifier.testTag("save_env_reading")
            ) {
                Text("Save Reading")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
