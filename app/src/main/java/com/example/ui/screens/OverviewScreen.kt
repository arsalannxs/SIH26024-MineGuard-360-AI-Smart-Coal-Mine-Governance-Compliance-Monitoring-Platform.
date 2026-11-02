package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.MineGuardRepository
import com.example.ui.components.*
import com.example.ui.navigation.NavSection
import com.example.ui.theme.*

@Composable
fun OverviewScreen(
    repository: MineGuardRepository,
    onNavigate: (NavSection) -> Unit,
    onOpenAddCompliance: () -> Unit,
    onOpenScheduleInspection: () -> Unit,
    onOpenReportSafety: () -> Unit,
    onOpenAddEnv: () -> Unit,
    onOpenDemoWalkthrough: () -> Unit
) {
    val mines by repository.mines.collectAsState()
    val compliance by repository.complianceItems.collectAsState()
    val inspections by repository.inspections.collectAsState()
    val incidents by repository.safetyIncidents.collectAsState()
    val envReadings by repository.environmentalReadings.collectAsState()
    val contractors by repository.contractors.collectAsState()
    val alerts by repository.alerts.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    val totalMines = mines.size
    val activeCompliance = compliance.size
    val compliantCount = compliance.count { it.status == ComplianceStatus.COMPLIANT }
    val pendingCompliance = compliance.count { it.status == ComplianceStatus.PENDING || it.status == ComplianceStatus.ACTION_REQUIRED }
    val overdueCompliance = compliance.count { it.status == ComplianceStatus.OVERDUE }
    val openInspections = inspections.count { it.status != InspectionStatus.COMPLETED }
    val unresolvedHazards = incidents.count { it.correctiveActionStatus != ActionStatus.VERIFIED_CLOSED }
    val envAlerts = envReadings.count { it.isBreach }
    val activeContractors = contractors.size

    val complianceRate = if (activeCompliance > 0) ((compliantCount.toFloat() / activeCompliance) * 100).toInt() else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CloudBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("overview_hero_banner"),
                colors = CardDefaults.cardColors(containerColor = DeepNavy),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = SafetyOrange.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "SIH26024 • Ministry of Coal / CIL",
                                color = SafetyOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Button(
                            onClick = onOpenDemoWalkthrough,
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("start_demo_walkthrough_btn")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("3-Min Demo", color = DeepNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "One Platform. Every Mine. Complete Visibility.",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time statutory governance, DGMS safety inspection tracking, environmental telemetrics, and AI risk prediction across all coal mining subsidiaries.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BorderColor,
                        lineHeight = 16.sp
                    )

                    // Current User role indicator
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = LightTeal, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Logged in as ${currentUser.name} • ${currentUser.role.displayName}",
                            color = LightTeal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Quick Action Shortcuts
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Field Actions",
                    style = MaterialTheme.typography.titleSmall,
                    color = MainText,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        ActionChipButton(
                            label = "+ Add Compliance",
                            icon = Icons.Default.FactCheck,
                            color = DeepNavy,
                            onClick = onOpenAddCompliance,
                            tag = "quick_action_add_compliance"
                        )
                    }
                    item {
                        ActionChipButton(
                            label = "+ Schedule Inspection",
                            icon = Icons.Default.FindInPage,
                            color = Teal,
                            onClick = onOpenScheduleInspection,
                            tag = "quick_action_schedule_inspection"
                        )
                    }
                    item {
                        ActionChipButton(
                            label = "+ Report Hazard",
                            icon = Icons.Default.Warning,
                            color = SafetyOrange,
                            onClick = onOpenReportSafety,
                            tag = "quick_action_report_hazard"
                        )
                    }
                    item {
                        ActionChipButton(
                            label = "+ Add Telemetry",
                            icon = Icons.Default.Sensors,
                            color = SuccessGreen,
                            onClick = onOpenAddEnv,
                            tag = "quick_action_add_env"
                        )
                    }
                    item {
                        ActionChipButton(
                            label = "Ask AI Risk",
                            icon = Icons.Default.Psychology,
                            color = DeepNavy,
                            onClick = { onNavigate(NavSection.AI_RISK) },
                            tag = "quick_action_ai_risk"
                        )
                    }
                    item {
                        ActionChipButton(
                            label = "Export Reports",
                            icon = Icons.Default.FileDownload,
                            color = Slate,
                            onClick = { onNavigate(NavSection.REPORTS) },
                            tag = "quick_action_export_reports"
                        )
                    }
                }
            }
        }

        // Executive Governance Metric Cards (2-column responsive layout)
        item {
            Text(
                text = "Key Governance Indicators",
                style = MaterialTheme.typography.titleSmall,
                color = MainText,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Registered Mines",
                    value = "$totalMines",
                    subtitle = "Across 5 CIL Subsidiaries",
                    icon = Icons.Default.Terrain,
                    iconTint = DeepNavy,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(NavSection.MINE_MGMT) }
                )
                MetricCard(
                    title = "Compliance Rate",
                    value = "$complianceRate%",
                    subtitle = "$compliantCount of $activeCompliance Verified",
                    icon = Icons.Default.FactCheck,
                    iconTint = if (complianceRate >= 70) SuccessGreen else WarningAmber,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(NavSection.COMPLIANCE) }
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Overdue Actions",
                    value = "$overdueCompliance",
                    subtitle = "Statutory DGMS Mandates",
                    icon = Icons.Default.Error,
                    iconTint = CriticalRed,
                    cardBorderColor = if (overdueCompliance > 0) CriticalRed.copy(alpha = 0.4f) else BorderColor,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(NavSection.COMPLIANCE) }
                )
                MetricCard(
                    title = "Pending Actions",
                    value = "$pendingCompliance",
                    subtitle = "Under Review / Action Req",
                    icon = Icons.Default.Schedule,
                    iconTint = WarningAmber,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(NavSection.COMPLIANCE) }
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Open Hazards",
                    value = "$unresolvedHazards",
                    subtitle = "Requires Field Mitigation",
                    icon = Icons.Default.HealthAndSafety,
                    iconTint = SafetyOrange,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(NavSection.SAFETY) }
                )
                MetricCard(
                    title = "Env Breaches",
                    value = "$envAlerts",
                    subtitle = "Ambient PM10 & Noise",
                    icon = Icons.Default.Eco,
                    iconTint = if (envAlerts > 0) CriticalRed else SuccessGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(NavSection.ENVIRONMENTAL) }
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Open Inspections",
                    value = "$openInspections",
                    subtitle = "DGMS & Internal Audits",
                    icon = Icons.Default.FindInPage,
                    iconTint = Teal,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(NavSection.INSPECTIONS) }
                )
                MetricCard(
                    title = "Active Contractors",
                    value = "$activeContractors",
                    subtitle = "Mining & Earthmoving",
                    icon = Icons.Default.Engineering,
                    iconTint = Slate,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(NavSection.CONTRACTORS) }
                )
            }
        }

        // Compliance Progress Visual Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderColor))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Statutory Compliance Progress",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MainText
                        )
                        Text(
                            text = "$compliantCount / $activeCompliance Met",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Teal
                        )
                    }

                    LinearProgressIndicator(
                        progress = { complianceRate / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Teal,
                        trackColor = BorderColor
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• Compliant: $compliantCount", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                        Text("• Pending: $pendingCompliance", fontSize = 11.sp, color = WarningAmber, fontWeight = FontWeight.SemiBold)
                        Text("• Overdue: $overdueCompliance", fontSize = 11.sp, color = CriticalRed, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Active Alerts & Escalations Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "High Priority Alerts (${alerts.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MainText,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigate(NavSection.ALERTS) }) {
                    Text("View All Alerts", color = Teal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        items(alerts.take(3)) { alert ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(NavSection.ALERTS) },
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (alert.priority == IssueSeverity.CRITICAL) CriticalRed.copy(alpha = 0.5f) else BorderColor
                    )
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (alert.priority == IssueSeverity.CRITICAL) CriticalRed.copy(alpha = 0.15f) else SafetyOrange.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (alert.priority == IssueSeverity.CRITICAL) Icons.Default.Warning else Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (alert.priority == IssueSeverity.CRITICAL) CriticalRed else SafetyOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = alert.mineName,
                                fontSize = 11.sp,
                                color = SecondaryText,
                                fontWeight = FontWeight.Medium
                            )
                            SeverityChip(severity = alert.priority)
                        }
                        Text(
                            text = alert.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MainText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = alert.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Bottom space
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ActionChipButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag(tag),
        color = color.copy(alpha = 0.12f),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(color.copy(alpha = 0.4f)))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Text(text = label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
