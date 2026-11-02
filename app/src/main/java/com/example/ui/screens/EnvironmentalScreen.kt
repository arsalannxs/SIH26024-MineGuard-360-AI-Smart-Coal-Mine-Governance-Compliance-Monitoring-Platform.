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
fun EnvironmentalScreen(
    repository: MineGuardRepository,
    onOpenAddEnv: () -> Unit
) {
    val readings by repository.environmentalReadings.collectAsState()
    val mines by repository.mines.collectAsState()

    val totalReadings = readings.size
    val activeBreaches = readings.count { it.isBreach }

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
                    text = "Environmental Telemetry & SPCB Compliance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MainText
                )
                Text(
                    text = "$totalReadings Sensor Stations Monitored • $activeBreaches Threshold Breaches",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (activeBreaches > 0) CriticalRed else SecondaryText
                )
            }

            Button(
                onClick = onOpenAddEnv,
                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_env_reading_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Reading", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Summary Gauge Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val pm10Reading = readings.firstOrNull { it.measurementType == EnvironmentalMetric.PM10 }
            val waterReading = readings.firstOrNull { it.measurementType == EnvironmentalMetric.WATER_PH }
            val noiseReading = readings.firstOrNull { it.measurementType == EnvironmentalMetric.NOISE_LEVEL }

            MetricCard(
                title = "PM10 Air Quality",
                value = "${pm10Reading?.value ?: "--"} µg",
                subtitle = "Safe: ≤ 100 µg/m³",
                icon = Icons.Default.Air,
                iconTint = if (pm10Reading?.isBreach == true) CriticalRed else Teal,
                cardBorderColor = if (pm10Reading?.isBreach == true) CriticalRed.copy(alpha = 0.5f) else BorderColor,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Effluent pH",
                value = "${waterReading?.value ?: "--"} pH",
                subtitle = "Safe: 6.5 – 8.5",
                icon = Icons.Default.WaterDrop,
                iconTint = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Ambient Noise",
                value = "${noiseReading?.value ?: "--"} dB",
                subtitle = "Safe: ≤ 75 dB(A)",
                icon = Icons.Default.VolumeUp,
                iconTint = if (noiseReading?.isBreach == true) SafetyOrange else Teal,
                cardBorderColor = if (noiseReading?.isBreach == true) SafetyOrange.copy(alpha = 0.5f) else BorderColor,
                modifier = Modifier.weight(1f)
            )
        }

        // Readings List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(readings) { r ->
                val mineName = mines.find { it.id == r.mineId }?.name ?: r.mineId

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("env_card_${r.id.lowercase()}"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (r.isBreach) CriticalRed.copy(alpha = 0.5f) else BorderColor
                        )
                    )
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
                                    text = r.measurementType.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MainText
                                )
                                Text(
                                    text = "$mineName • ${r.measurementLocation}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryText
                                )
                            }
                            StatusBadge(
                                text = if (r.isBreach) "LIMIT BREACH" else "COMPLIANT",
                                containerColor = if (r.isBreach) CriticalRed.copy(alpha = 0.15f) else SuccessGreen.copy(alpha = 0.15f),
                                contentColor = if (r.isBreach) CriticalRed else SuccessGreen
                            )
                        }

                        // Reading Value Box
                        Surface(
                            color = if (r.isBreach) CriticalRed.copy(alpha = 0.08f) else LightTeal,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Observed Telemetry", fontSize = 11.sp, color = SecondaryText)
                                    Text(
                                        "${r.value} ${r.unit}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (r.isBreach) CriticalRed else DeepNavy
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Statutory Safe Limit", fontSize = 11.sp, color = SecondaryText)
                                    Text(r.measurementType.safeLimit, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MainText)
                                }
                            }
                        }

                        Text("Instrument: ${r.instrumentOrSource}", fontSize = 11.sp, color = SecondaryText)
                        Text("Remarks: ${r.remarks}", fontSize = 11.sp, color = MainText)
                        Text("Timestamp: ${r.measurementDate} • Officer: ${r.responsiblePerson}", fontSize = 10.sp, color = SecondaryText)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
