package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.*
import com.example.data.model.*
import com.example.data.repository.MineGuardRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun AIRiskScreen(
    repository: MineGuardRepository
) {
    val mines by repository.mines.collectAsState()
    val compliance by repository.complianceItems.collectAsState()
    val inspections by repository.inspections.collectAsState()
    val incidents by repository.safetyIncidents.collectAsState()
    val envReadings by repository.environmentalReadings.collectAsState()
    val contractors by repository.contractors.collectAsState()

    val aiEngine = remember { AIRiskEngine() }

    val mineRiskScores = remember(mines, compliance, inspections, incidents, envReadings, contractors) {
        aiEngine.computeMineRisks(mines, compliance, inspections, incidents, envReadings, contractors)
    }

    var currentQuery by remember { mutableStateOf("Which mines have highest number of overdue compliance actions?") }
    var activeQueryResult by remember { mutableStateOf<AIQueryResult?>(null) }
    var isThinking by remember { mutableStateOf(false) }

    // Preset prompts from SIH prompt section 15
    val promptPresets = listOf(
        "Which mines have highest number of overdue compliance actions?",
        "Show repeated safety observations during the selected period.",
        "Which corrective actions have exceeded their deadlines?",
        "Summarise environmental monitoring issues.",
        "Compare inspection findings across two mines.",
        "Which contractors have pending documentation?"
    )

    // Trigger initial query for instant demonstration
    LaunchedEffect(Unit) {
        activeQueryResult = aiEngine.answerQuery(
            currentQuery,
            mines,
            compliance,
            inspections,
            incidents,
            envReadings,
            contractors
        )
    }

    fun executeQuery(q: String) {
        currentQuery = q
        isThinking = true
        // Deterministic grounded resolution
        activeQueryResult = aiEngine.answerQuery(
            q,
            mines,
            compliance,
            inspections,
            incidents,
            envReadings,
            contractors
        )
        isThinking = false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CloudBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Risk Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("ai_risk_header_card"),
                colors = CardDefaults.cardColors(containerColor = DeepNavy),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Teal.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LightTeal, modifier = Modifier.size(14.dp))
                                Text("Decision-Support AI Intelligence", color = LightTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text("SIH26024 Engine", color = BorderColor, fontSize = 11.sp)
                    }

                    Text(
                        text = "AI Risk Intelligence & Natural Language Analytics",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Synthesizes statutory compliance, geotechnical radar, hazard frequency, and environmental telemetry into explainable risk indicators. Recommendations do not replace official regulatory decisions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BorderColor,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Natural Language Query Assistant
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderColor))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Ask Records Assistant (Natural Language)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MainText
                    )

                    // Prompt Chips
                    Text("Preset Analytical Questions:", fontSize = 11.sp, color = SecondaryText, fontWeight = FontWeight.Medium)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(promptPresets) { p ->
                            FilterChip(
                                selected = currentQuery == p,
                                onClick = { executeQuery(p) },
                                label = { Text(p, fontSize = 11.sp, maxLines = 1) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LightTeal,
                                    selectedLabelColor = DeepNavy
                                ),
                                modifier = Modifier.testTag("prompt_chip_${p.take(10).lowercase().trim()}")
                            )
                        }
                    }

                    // Query Input Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = currentQuery,
                            onValueChange = { currentQuery = it },
                            placeholder = { Text("Ask a question about colliery records...") },
                            modifier = Modifier.weight(1f).testTag("ai_query_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Teal,
                                unfocusedBorderColor = BorderColor
                            )
                        )

                        Button(
                            onClick = { executeQuery(currentQuery) },
                            colors = ButtonDefaults.buttonColors(containerColor = Teal),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            modifier = Modifier.testTag("ai_ask_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Query", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Active Query Result Card (Grounded in Evidence)
        activeQueryResult?.let { res ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("ai_query_result_card"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Teal.copy(alpha = 0.5f)))
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
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Insights, contentDescription = null, tint = Teal, modifier = Modifier.size(18.dp))
                                Text("Grounded AI Finding", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepNavy)
                            }
                            StatusBadge(text = res.confidence, containerColor = LightTeal, contentColor = Teal)
                        }

                        // Answer
                        Text(
                            text = res.answer,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MainText,
                            lineHeight = 20.sp
                        )

                        // Rationale & Method
                        Surface(
                            color = CloudBackground,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Calculation Methodology & Reporting Period:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                Text("${res.rationale} • Period: ${res.period}", fontSize = 11.sp, color = SecondaryText)
                            }
                        }

                        // Supporting Evidence citations
                        if (res.supportingEvidence.isNotEmpty()) {
                            Text("Direct Supporting Evidence (${res.supportingEvidence.size} records):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MainText)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                res.supportingEvidence.forEach { ev ->
                                    Surface(
                                        color = SurfaceWhite,
                                        shape = RoundedCornerShape(6.dp),
                                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderColor)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("[${ev.recordId}] ${ev.entityType} • ${ev.mineName}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                                Text(ev.details, fontSize = 11.sp, color = SecondaryText, maxLines = 1)
                                            }
                                            StatusBadge(
                                                text = ev.status,
                                                containerColor = if (ev.status.contains("OVERDUE") || ev.status.contains("EXCEEDED") || ev.status.contains("Critical")) CriticalRed.copy(alpha = 0.15f) else LightTeal,
                                                contentColor = if (ev.status.contains("OVERDUE") || ev.status.contains("EXCEEDED") || ev.status.contains("Critical")) CriticalRed else Teal
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Recommended Actions
                        if (res.recommendedActions.isNotEmpty()) {
                            Text("Suggested Follow-up Interventions:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MainText)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                res.recommendedActions.forEach { act ->
                                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("•", color = SafetyOrange, fontWeight = FontWeight.Bold)
                                        Text(act, fontSize = 12.sp, color = MainText)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Colliery Risk Indices (0-100)
        item {
            Text(
                text = "Colliery Composite Risk Indices (0–100 Scale)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MainText
            )
        }

        items(mineRiskScores) { score ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mine_risk_card_${score.mineId.lowercase()}"),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        when (score.riskLevel) {
                            RiskCategory.CRITICAL -> CriticalRed.copy(alpha = 0.6f)
                            RiskCategory.HIGH -> SafetyOrange.copy(alpha = 0.6f)
                            RiskCategory.MODERATE -> WarningAmber.copy(alpha = 0.5f)
                            RiskCategory.LOW -> SuccessGreen.copy(alpha = 0.4f)
                        }
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
                                text = score.mineName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MainText
                            )
                            Text(
                                text = "${score.subsidiary} • Primary Driver: ${score.primaryRiskDriver}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryText
                            )
                        }

                        // Big Risk Score Badge
                        Surface(
                            color = when (score.riskLevel) {
                                RiskCategory.CRITICAL -> CriticalRed.copy(alpha = 0.15f)
                                RiskCategory.HIGH -> SafetyOrange.copy(alpha = 0.15f)
                                RiskCategory.MODERATE -> WarningAmber.copy(alpha = 0.15f)
                                RiskCategory.LOW -> SuccessGreen.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${score.compositeRiskIndex}/100",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (score.riskLevel) {
                                        RiskCategory.CRITICAL -> CriticalRed
                                        RiskCategory.HIGH -> SafetyOrange
                                        RiskCategory.MODERATE -> WarningAmber
                                        RiskCategory.LOW -> SuccessGreen
                                    }
                                )
                                Text(
                                    text = score.riskLevel.label.take(8),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (score.riskLevel) {
                                        RiskCategory.CRITICAL -> CriticalRed
                                        RiskCategory.HIGH -> SafetyOrange
                                        RiskCategory.MODERATE -> WarningAmber
                                        RiskCategory.LOW -> SuccessGreen
                                    }
                                )
                            }
                        }
                    }

                    // Penalty Breakdown Bar
                    LinearProgressIndicator(
                        progress = { score.compositeRiskIndex / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when (score.riskLevel) {
                            RiskCategory.CRITICAL -> CriticalRed
                            RiskCategory.HIGH -> SafetyOrange
                            RiskCategory.MODERATE -> WarningAmber
                            RiskCategory.LOW -> SuccessGreen
                        },
                        trackColor = BorderColor
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Overdue Compliance: ${score.overdueComplianceCount}", fontSize = 11.sp, color = SecondaryText)
                        Text("High Hazards: ${score.highSeverityIncidentCount}", fontSize = 11.sp, color = SecondaryText)
                        Text("Env Breaches: ${score.envBreachCount}", fontSize = 11.sp, color = SecondaryText)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
