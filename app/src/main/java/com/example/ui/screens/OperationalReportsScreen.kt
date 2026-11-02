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
fun OperationalReportsScreen(
    repository: MineGuardRepository
) {
    val operationalReports by repository.operationalReports.collectAsState()
    val mines by repository.mines.collectAsState()

    var showAddReportDialog by remember { mutableStateOf(false) }

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
                    text = "Operational Metrics & Coal Extraction",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MainText
                )
                Text(
                    text = "Production, Overburden Removal & Machinery Telemetry",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }

            Button(
                onClick = { showAddReportDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_operational_metric_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Record", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Summary Metric Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard(
                title = "Raw Coal (Oct)",
                value = "1.45 MT",
                subtitle = "Silo Rake Telemetry",
                icon = Icons.Default.Terrain,
                iconTint = DeepNavy,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "OB Removed",
                value = "4.20 M BCM",
                subtitle = "Laser Drone Pit Scan",
                icon = Icons.Default.Layers,
                iconTint = Teal,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "HEMM Uptime",
                value = "86.4%",
                subtitle = "SAP PM Benchmark",
                icon = Icons.Default.PrecisionManufacturing,
                iconTint = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        // Reports List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(operationalReports) { rep ->
                val mineName = mines.find { it.id == rep.mineId }?.name ?: rep.mineId

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("op_report_${rep.id.lowercase()}"),
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
                                    text = rep.metricName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MainText
                                )
                                Text(
                                    text = "$mineName • Period: ${rep.reportingPeriod}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryText
                                )
                            }
                            StatusBadge(
                                text = rep.verificationStatus.label,
                                containerColor = if (rep.verificationStatus == ReportVerificationStatus.VERIFIED) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                                contentColor = if (rep.verificationStatus == ReportVerificationStatus.VERIFIED) SuccessGreen else WarningAmber
                            )
                        }

                        // Value highlight
                        Surface(
                            color = LightTeal,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Recorded Figure:", fontSize = 12.sp, color = SecondaryText)
                                Text(
                                    "${"%,.1f".format(rep.value)} ${rep.unit}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                            }
                        }

                        Text("Telemetry Source: ${rep.source}", fontSize = 11.sp, color = SecondaryText)
                        Text("Submitted by: ${rep.reportingOfficer} on ${rep.dateSubmitted}", fontSize = 10.sp, color = SecondaryText)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showAddReportDialog) {
        var metricName by remember { mutableStateOf("Daily Coal Production") }
        var metricVal by remember { mutableStateOf("45000.0") }
        var unit by remember { mutableStateOf("Tonnes") }

        AlertDialog(
            onDismissRequest = { showAddReportDialog = false },
            title = { Text("Submit Operational Metric", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = metricName,
                        onValueChange = { metricName = it },
                        label = { Text("Metric Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("op_metric_name")
                    )
                    OutlinedTextField(
                        value = metricVal,
                        onValueChange = { metricVal = it },
                        label = { Text("Metric Value *") },
                        modifier = Modifier.fillMaxWidth().testTag("op_metric_val")
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit of Measurement") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rep = OperationalReport(
                            id = "OPS-${System.currentTimeMillis() % 10000}",
                            mineId = mines.firstOrNull()?.id ?: "DEMO-MINE-01",
                            reportingPeriod = "November 2026",
                            metricName = metricName,
                            value = metricVal.toDoubleOrNull() ?: 1000.0,
                            unit = unit,
                            source = "Manual Entry with Weigher Telemetry",
                            reportingOfficer = "Chief Mining Engineer",
                            verificationStatus = ReportVerificationStatus.VERIFIED,
                            dateSubmitted = "2026-11-02"
                        )
                        repository.addOperationalReport(rep)
                        showAddReportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                ) {
                    Text("Submit Metric")
                }
            },
            dismissButton = { TextButton(onClick = { showAddReportDialog = false }) { Text("Cancel") } }
        )
    }
}
