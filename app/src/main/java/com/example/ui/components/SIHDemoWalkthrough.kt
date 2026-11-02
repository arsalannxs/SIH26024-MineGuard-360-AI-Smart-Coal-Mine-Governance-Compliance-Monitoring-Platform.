package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.ui.navigation.NavSection
import com.example.ui.theme.*

data class DemoStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val targetSection: NavSection,
    val highlightNote: String
)

val SIH_DEMO_STEPS = listOf(
    DemoStep(1, "Open MineGuard Dashboard", "View executive KPIs, active mines, overdue actions and quick action shortcuts.", NavSection.OVERVIEW, "Overview cards & high-level stats"),
    DemoStep(2, "Select Sample Mine", "Filter by 'Rajmahal Open Cast Project' to isolate single colliery data.", NavSection.MINE_MGMT, "Check Rajmahal Colliery details"),
    DemoStep(3, "Open Compliance Register", "Inspect statutory requirements categorized by DGMS and MOEFCC.", NavSection.COMPLIANCE, "Filter by Overdue or Action Required"),
    DemoStep(4, "Show Overdue Compliance Action", "Identify 'Continuous Dust Suppression along Haul Roads' (DGMS Reg 104) past due.", NavSection.COMPLIANCE, "Evidence document link & overdue status badge"),
    DemoStep(5, "Schedule an Inspection", "Plan an official DGMS pit bench slope stability inspection with checklist.", NavSection.INSPECTIONS, "Click '+ Schedule Inspection'"),
    DemoStep(6, "Submit Safety Observation", "Report high-severity boulder spillage hazard with immediate mitigation note.", NavSection.SAFETY, "Click '+ Report Hazard'"),
    DemoStep(7, "Environmental Monitoring", "Review ambient PM10 telemetry breach (138.5 µg/m³) and noise alerts.", NavSection.ENVIRONMENTAL, "Automatic breach notification badge"),
    DemoStep(8, "GIS Mine Map", "Visualize geo-tagged mines, telemetry stations, and field reporting pins.", NavSection.GIS_MAP, "Interactive colliery map with offline draft sync"),
    DemoStep(9, "AI Risk Intelligence", "Open natural-language pattern and risk correlation engine.", NavSection.AI_RISK, "Multi-factor composite risk index"),
    DemoStep(10, "Ask AI Query", "Query: 'Which mines have highest overdue compliance actions?'", NavSection.AI_RISK, "Click prompt chip for instant grounded query"),
    DemoStep(11, "View Supporting Evidence", "Verify deterministic citations linking CMP-2026-001 and DGMS findings.", NavSection.AI_RISK, "Transparent record evidence cards"),
    DemoStep(12, "Assign Corrective Action", "Track resolution workflow and assign responsible officer.", NavSection.SAFETY, "Update incident status to In Progress / Resolved"),
    DemoStep(13, "Generate Compliance Report", "Preview and export statutory audit summary with CSV download.", NavSection.REPORTS, "Click 'Export CSV' or 'Generate Printable Summary'"),
    DemoStep(14, "Open Digital Audit Trail", "Inspect tamper-evident SHA-256 hash-chain verifying every action.", NavSection.AUDIT_TRAIL, "Cryptographic hash verification badges")
)

@Composable
fun SIHDemoWalkthroughDialog(
    currentStepIndex: Int,
    onNavigateToStep: (NavSection, Int) -> Unit,
    onDismiss: () -> Unit,
    onResetData: () -> Unit
) {
    var stepIdx by remember { mutableStateOf(currentStepIndex.coerceIn(0, SIH_DEMO_STEPS.size - 1)) }
    val step = SIH_DEMO_STEPS[stepIdx]

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = SafetyOrange)
                    Text("SIH 3-Minute Guided Demo", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                Surface(
                    color = SafetyOrange.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Step ${step.stepNumber}/14",
                        color = SafetyOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = step.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DeepNavy
                )
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MainText
                )

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
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Teal, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Focus: ${step.highlightNote}",
                            color = Teal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { if (stepIdx > 0) stepIdx-- },
                        enabled = stepIdx > 0
                    ) {
                        Text("Previous")
                    }

                    TextButton(
                        onClick = { if (stepIdx < SIH_DEMO_STEPS.size - 1) stepIdx++ },
                        enabled = stepIdx < SIH_DEMO_STEPS.size - 1
                    ) {
                        Text("Next Step")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onNavigateToStep(step.targetSection, stepIdx)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                modifier = Modifier.testTag("jump_to_demo_step")
            ) {
                Text("Go to ${step.targetSection.title}")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
