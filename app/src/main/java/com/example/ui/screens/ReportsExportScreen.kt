package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.MineGuardRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ReportsExportScreen(
    repository: MineGuardRepository
) {
    val mines by repository.mines.collectAsState()
    val compliance by repository.complianceItems.collectAsState()
    val incidents by repository.safetyIncidents.collectAsState()
    val envReadings by repository.environmentalReadings.collectAsState()
    val contractors by repository.contractors.collectAsState()

    var selectedReportType by remember { mutableStateOf("Compliance") }
    var exportPreviewText by remember { mutableStateOf<String?>(null) }
    var copyStatusMessage by remember { mutableStateOf<String?>(null) }

    val reportTypes = listOf("Compliance", "Safety", "Inspections", "Environmental", "Contractors", "AuditLog")

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
                    text = "Reports & Statutory Exports",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MainText
                )
                Text(
                    text = "Generate printable executive reports and export verified CSV datasets",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }
        }

        // Report Type Selector
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            reportTypes.forEach { rType ->
                FilterChip(
                    selected = selectedReportType == rType,
                    onClick = {
                        selectedReportType = rType
                        exportPreviewText = null
                    },
                    label = { Text(rType, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LightTeal,
                        selectedLabelColor = DeepNavy
                    ),
                    modifier = Modifier.testTag("report_type_${rType.lowercase()}")
                )
            }
        }

        // Report Preview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    Column {
                        Text(
                            text = "Ministry of Coal / CIL Statutory Report",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafetyOrange
                        )
                        Text(
                            text = "$selectedReportType Audit Dossier",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                    }
                    StatusBadge(text = "SIH26024 Verified", containerColor = LightTeal, contentColor = Teal)
                }

                Text(
                    text = "Reporting Period: October – November 2026 • Scope: All Monitored Collieries",
                    fontSize = 11.sp,
                    color = SecondaryText
                )

                Divider(color = BorderColor.copy(alpha = 0.5f))

                // Summary stats depending on report type
                when (selectedReportType) {
                    "Compliance" -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Requirements: ${compliance.size}", fontSize = 12.sp, color = MainText)
                            Text("Compliant: ${compliance.count { it.status == ComplianceStatus.COMPLIANT }}", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                            Text("Overdue: ${compliance.count { it.status == ComplianceStatus.OVERDUE }}", fontSize = 12.sp, color = CriticalRed, fontWeight = FontWeight.Bold)
                        }
                    }
                    "Safety" -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Hazards: ${incidents.size}", fontSize = 12.sp, color = MainText)
                            Text("Critical: ${incidents.count { it.severity == IssueSeverity.CRITICAL }}", fontSize = 12.sp, color = CriticalRed, fontWeight = FontWeight.Bold)
                            Text("Closed: ${incidents.count { it.correctiveActionStatus == ActionStatus.VERIFIED_CLOSED }}", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                    "Environmental" -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sensor Readings: ${envReadings.size}", fontSize = 12.sp, color = MainText)
                            Text("Limit Breaches: ${envReadings.count { it.isBreach }}", fontSize = 12.sp, color = CriticalRed, fontWeight = FontWeight.Bold)
                            Text("Compliant Stations: ${envReadings.count { !it.isBreach }}", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {
                        Text("Ready for structured data generation and downstream CSV export.", fontSize = 12.sp, color = MainText)
                    }
                }

                // Export buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val csvData = repository.generateCsvExport(selectedReportType)
                            exportPreviewText = csvData
                            copyStatusMessage = "CSV Generated Successfully (${csvData.lines().size - 1} rows)!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                        modifier = Modifier.weight(1f).testTag("generate_csv_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV File")
                    }
                }

                copyStatusMessage?.let { msg ->
                    Text(text = msg, fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Live CSV Preview Box
        exportPreviewText?.let { csv ->
            Text("Generated CSV Data Output:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MainText)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2832)),
                shape = RoundedCornerShape(10.dp)
            ) {
                LazyColumn(modifier = Modifier.padding(12.dp)) {
                    item {
                        Text(
                            text = csv,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF8CE8C3),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        if (exportPreviewText == null) {
            Surface(
                color = CloudBackground,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Statutory Compliance Notice:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                    Text(
                        "Reports generated from this platform must be counter-signed by the Colliery General Manager before submission to the Directorate General of Mines Safety (DGMS) or State Pollution Control Board (SPCB).",
                        fontSize = 11.sp,
                        color = SecondaryText,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
