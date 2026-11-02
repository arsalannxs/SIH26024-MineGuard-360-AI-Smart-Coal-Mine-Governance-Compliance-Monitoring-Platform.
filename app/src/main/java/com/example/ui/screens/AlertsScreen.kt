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
fun AlertsScreen(
    repository: MineGuardRepository
) {
    val alerts by repository.alerts.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredAlerts = alerts.filter { alert ->
        when (selectedFilter) {
            "ALL" -> true
            "CRITICAL" -> alert.priority == IssueSeverity.CRITICAL
            "HIGH" -> alert.priority == IssueSeverity.HIGH
            "NEW" -> alert.status == AlertStatus.NEW
            "ESCALATED" -> alert.status == AlertStatus.ESCALATED
            else -> true
        }
    }

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
                    text = "Alerts & Escalation Engine",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MainText
                )
                Text(
                    text = "${alerts.size} Active System Alerts • Priority Triage & Escalations",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }
        }

        // Filter chips
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("ALL", "CRITICAL", "HIGH", "NEW", "ESCALATED").forEach { f ->
                FilterChip(
                    selected = selectedFilter == f,
                    onClick = { selectedFilter = f },
                    label = { Text(f, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (f == "CRITICAL") CriticalRed.copy(alpha = 0.15f) else LightTeal,
                        selectedLabelColor = if (f == "CRITICAL") CriticalRed else DeepNavy
                    ),
                    modifier = Modifier.testTag("filter_alert_${f.lowercase()}")
                )
            }
        }

        // Alerts List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredAlerts) { alert ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("alert_card_${alert.id.lowercase()}"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (alert.priority == IssueSeverity.CRITICAL) CriticalRed.copy(alpha = 0.5f) else BorderColor
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
                                    text = alert.alertType.label,
                                    fontSize = 12.sp,
                                    color = SecondaryText,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = alert.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MainText
                                )
                            }
                            SeverityChip(severity = alert.priority)
                        }

                        Text(
                            text = alert.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MainText,
                            lineHeight = 18.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Colliery: ${alert.mineName}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DeepNavy)
                            Text("Officer: ${alert.responsibleOfficer}", fontSize = 11.sp, color = SecondaryText)
                        }

                        Surface(
                            color = CloudBackground,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Status: ${alert.status.label}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                Text("Triggered: ${alert.createdAt}", fontSize = 10.sp, color = SecondaryText)
                            }
                        }

                        // Action buttons row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (alert.status == AlertStatus.NEW) {
                                OutlinedButton(
                                    onClick = { repository.acknowledgeAlert(alert.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepNavy)
                                ) {
                                    Text("Acknowledge", fontSize = 11.sp)
                                }
                            }

                            if (alert.status != AlertStatus.ESCALATED && alert.status != AlertStatus.RESOLVED) {
                                OutlinedButton(
                                    onClick = { repository.escalateAlert(alert.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SafetyOrange)
                                ) {
                                    Text("Escalate to GM", fontSize = 11.sp)
                                }
                            }

                            if (alert.status != AlertStatus.RESOLVED) {
                                Button(
                                    onClick = { repository.resolveAlert(alert.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                ) {
                                    Text("Resolve", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
