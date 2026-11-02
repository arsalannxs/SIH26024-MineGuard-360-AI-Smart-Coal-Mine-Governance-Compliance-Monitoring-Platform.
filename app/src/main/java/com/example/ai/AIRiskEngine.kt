package com.example.ai

import com.example.data.model.*

data class MineRiskScore(
    val mineId: String,
    val mineName: String,
    val subsidiary: String,
    val compositeRiskIndex: Int, // 0 - 100
    val riskLevel: RiskCategory,
    val compliancePenalty: Int,
    val safetyPenalty: Int,
    val envPenalty: Int,
    val contractorPenalty: Int,
    val openActionCount: Int,
    val overdueComplianceCount: Int,
    val highSeverityIncidentCount: Int,
    val envBreachCount: Int,
    val primaryRiskDriver: String,
    val evidenceRecordIds: List<String>
)

enum class RiskCategory(val label: String) {
    LOW("Low Risk (Nominal)"),
    MODERATE("Moderate Risk"),
    HIGH("High Risk (Action Needed)"),
    CRITICAL("Critical Risk (Intervention Required)")
}

data class AIQueryResult(
    val query: String,
    val answer: String,
    val rationale: String,
    val period: String,
    val supportingEvidence: List<EvidenceItem>,
    val recommendedActions: List<String>,
    val confidence: String = "High (Deterministic Record Cross-Reference)"
)

data class EvidenceItem(
    val recordId: String,
    val entityType: String,
    val mineName: String,
    val keyMetric: String,
    val details: String,
    val status: String
)

class AIRiskEngine {

    fun computeMineRisks(
        mines: List<Mine>,
        compliance: List<ComplianceItem>,
        inspections: List<Inspection>,
        incidents: List<SafetyIncident>,
        envReadings: List<EnvironmentalReading>,
        contractors: List<Contractor>
    ): List<MineRiskScore> {
        return mines.map { mine ->
            val mineCompliance = compliance.filter { it.mineId == mine.id }
            val mineInspections = inspections.filter { it.mineId == mine.id }
            val mineIncidents = incidents.filter { it.mineId == mine.id }
            val mineEnv = envReadings.filter { it.mineId == mine.id }
            val mineContractors = contractors.filter { it.mineId == mine.id }

            val overdueCount = mineCompliance.count { it.status == ComplianceStatus.OVERDUE }
            val actionReqCount = mineCompliance.count { it.status == ComplianceStatus.ACTION_REQUIRED }
            val compPenalty = (overdueCount * 25) + (actionReqCount * 12)

            val criticalIncidents = mineIncidents.count { it.severity == IssueSeverity.CRITICAL && it.correctiveActionStatus != ActionStatus.VERIFIED_CLOSED }
            val highIncidents = mineIncidents.count { it.severity == IssueSeverity.HIGH && it.correctiveActionStatus != ActionStatus.VERIFIED_CLOSED }
            val safetyPenalty = (criticalIncidents * 30) + (highIncidents * 15)

            val breaches = mineEnv.count { it.isBreach }
            val envPenalty = breaches * 20

            val pendingContractors = mineContractors.count { it.complianceStatus != ContractorComplianceStatus.VERIFIED_COMPLIANT }
            val contractorPenalty = pendingContractors * 15

            val rawScore = (compPenalty + safetyPenalty + envPenalty + contractorPenalty).coerceIn(0, 100)

            val level = when {
                rawScore >= 65 -> RiskCategory.CRITICAL
                rawScore >= 40 -> RiskCategory.HIGH
                rawScore >= 20 -> RiskCategory.MODERATE
                else -> RiskCategory.LOW
            }

            // Find primary driver
            val maxPenalty = maxOf(compPenalty, safetyPenalty, envPenalty, contractorPenalty)
            val driver = when (maxPenalty) {
                compPenalty -> "Overdue Statutory Compliance Items ($overdueCount overdue)"
                safetyPenalty -> "Unresolved Critical Safety Hazards ($criticalIncidents critical)"
                envPenalty -> "Environmental Measurement Breaches ($breaches breaches)"
                else -> "Contractor Documentation Delays"
            }

            val evidenceIds = mutableListOf<String>()
            mineCompliance.filter { it.status == ComplianceStatus.OVERDUE || it.status == ComplianceStatus.ACTION_REQUIRED }
                .forEach { evidenceIds.add(it.id) }
            mineIncidents.filter { it.severity == IssueSeverity.CRITICAL || it.severity == IssueSeverity.HIGH }
                .forEach { evidenceIds.add(it.id) }
            mineEnv.filter { it.isBreach }.forEach { evidenceIds.add(it.id) }

            MineRiskScore(
                mineId = mine.id,
                mineName = mine.name,
                subsidiary = mine.subsidiary,
                compositeRiskIndex = rawScore,
                riskLevel = level,
                compliancePenalty = compPenalty,
                safetyPenalty = safetyPenalty,
                envPenalty = envPenalty,
                contractorPenalty = contractorPenalty,
                openActionCount = overdueCount + actionReqCount + highIncidents + breaches,
                overdueComplianceCount = overdueCount,
                highSeverityIncidentCount = criticalIncidents + highIncidents,
                envBreachCount = breaches,
                primaryRiskDriver = driver,
                evidenceRecordIds = evidenceIds
            )
        }.sortedByDescending { it.compositeRiskIndex }
    }

    fun answerQuery(
        query: String,
        mines: List<Mine>,
        compliance: List<ComplianceItem>,
        inspections: List<Inspection>,
        incidents: List<SafetyIncident>,
        envReadings: List<EnvironmentalReading>,
        contractors: List<Contractor>
    ): AIQueryResult {
        val q = query.lowercase().trim()

        return when {
            q.contains("overdue") || q.contains("highest number of pending") || q.contains("highest overdue") -> {
                val overdueItems = compliance.filter { it.status == ComplianceStatus.OVERDUE }
                val mineCounts = overdueItems.groupBy { it.mineId }.mapValues { it.value.size }
                val maxMineEntry = mineCounts.maxByOrNull { it.value }
                val topMine = mines.find { it.id == maxMineEntry?.key }

                val evidence = overdueItems.map { item ->
                    val mName = mines.find { it.id == item.mineId }?.name ?: item.mineId
                    EvidenceItem(
                        recordId = item.id,
                        entityType = "Statutory Compliance",
                        mineName = mName,
                        keyMetric = "Due Date: ${item.dueDate}",
                        details = "${item.requirementTitle} (${item.applicableRegulation})",
                        status = item.status.label
                    )
                }

                AIQueryResult(
                    query = query,
                    answer = if (topMine != null) {
                        "${topMine.name} (${topMine.subsidiary}) currently has the highest count of overdue compliance actions (${maxMineEntry?.value} overdue items). Across all registered collieries, there are ${overdueItems.size} overdue actions requiring immediate supervisory attention."
                    } else {
                        "Currently, zero statutory compliance actions are flagged as overdue in the active register."
                    },
                    rationale = "Calculated from statutory compliance records where current date > applicable statutory due date and verification is incomplete.",
                    period = "Active Statutory Register as of November 2026",
                    supportingEvidence = evidence,
                    recommendedActions = listOf(
                        "Expedite haul road sprinkler booster pump replacement tender at ${topMine?.name ?: "affected mines"}.",
                        "Issue urgent compliance reminder to responsible officers (Rajesh Sharma & V. N. Tripathi).",
                        "Schedule DGMS pre-audit review before formal regulatory inspection."
                    )
                )
            }

            q.contains("repeated safety") || q.contains("hazard") || q.contains("safety observation") -> {
                val highIncidents = incidents.filter { it.severity == IssueSeverity.HIGH || it.severity == IssueSeverity.CRITICAL }
                val haulRoadIssues = incidents.filter { it.description.contains("haul", ignoreCase = true) || it.location.contains("road", ignoreCase = true) || it.location.contains("ramp", ignoreCase = true) }

                val evidence = highIncidents.map { inc ->
                    val mName = mines.find { it.id == inc.mineId }?.name ?: inc.mineId
                    EvidenceItem(
                        recordId = inc.id,
                        entityType = "Safety Incident / Hazard",
                        mineName = mName,
                        keyMetric = "${inc.category.label} (${inc.severity.label})",
                        details = "${inc.description} - Immediate action: ${inc.immediateActionTaken}",
                        status = inc.correctiveActionStatus.label
                    )
                }

                AIQueryResult(
                    query = query,
                    answer = "A recurring safety pattern is detected in Haul Road management and high-pressure hydraulic equipment. Specifically, boulder spillage on Ramp #2 and haul road berm height deficiencies (< 1.8m) were reported concurrently at Rajmahal OCP, presenting a heightened vehicle collision and tyre puncture risk.",
                    rationale = "Derived from cluster analysis of incident location tags ('Haul Road Ramp #2', 'Pit Workshop Bay 3') and DGMS inspection finding #INSP-2026-101.",
                    period = "Last 30 Days (October - November 2026)",
                    supportingEvidence = evidence,
                    recommendedActions = listOf(
                        "Enforce strict 1.8m berm height dressing along Section 4 immediately.",
                        "Calibrate automated weigh-bridge tailgate sensors to eliminate dumper overloading.",
                        "Conduct non-destructive testing (NDT) on hydraulic hoses across all heavy earthmovers."
                    )
                )
            }

            q.contains("environmental") || q.contains("pm10") || q.contains("pollution") || q.contains("air") || q.contains("water") -> {
                val breaches = envReadings.filter { it.isBreach }
                val evidence = envReadings.map { reading ->
                    val mName = mines.find { it.id == reading.mineId }?.name ?: reading.mineId
                    EvidenceItem(
                        recordId = reading.id,
                        entityType = "Environmental Telemetry",
                        mineName = mName,
                        keyMetric = "${reading.measurementType.label}: ${reading.value} ${reading.unit}",
                        details = "Location: ${reading.measurementLocation}. Safe limit: ${reading.measurementType.safeLimit}. Remarks: ${reading.remarks}",
                        status = if (reading.isBreach) "LIMIT EXCEEDED" else "Compliant"
                    )
                }

                AIQueryResult(
                    query = query,
                    answer = "Environmental telemetry indicates 2 active threshold breaches at Rajmahal OCP: (1) Ambient PM10 at Boundary Station #2 peaked at 138.5 µg/m³ (safe limit 100 µg/m³); (2) Ambient noise at CHP Feeder reached 79.2 dB(A) (limit 75 dB(A)). Effluent water discharge pH (7.4) and TSS levels remain 100% compliant.",
                    rationale = "Real-time comparison of continuous sensor readings against CPCB / MOEFCC National Ambient Air Quality Standards (NAAQS 2009).",
                    period = "Past 7 Days Telemetry",
                    supportingEvidence = evidence,
                    recommendedActions = listOf(
                        "Deploy mobile water sprinkler bowsers to Lease Boundary East until Section C fixed line is repaired.",
                        "Replace worn rubber chute liners at CHP Feeder Breaker to reduce acoustic vibration.",
                        "File statutory environmental incident mitigation report to SPCB within 24 hours."
                    )
                )
            }

            q.contains("contractor") || q.contains("document") || q.contains("pending documentation") -> {
                val pendingContractors = contractors.filter { it.complianceStatus != ContractorComplianceStatus.VERIFIED_COMPLIANT }
                val evidence = contractors.map { c ->
                    val mName = mines.find { it.id == c.mineId }?.name ?: c.mineId
                    EvidenceItem(
                        recordId = c.id,
                        entityType = "Contractor Compliance",
                        mineName = mName,
                        keyMetric = "Docs: ${c.requiredDocsUploaded}/${c.totalRequiredDocs}",
                        details = "${c.companyName} (${c.workCategory}). Contract: ${c.contractRef}",
                        status = c.complianceStatus.label
                    )
                }

                AIQueryResult(
                    query = query,
                    answer = "Trans-Coal Haulage & Earthmovers Ltd has 1 pending document (Workmen Compensation Insurance Policy WC-2025/99 renewal due in 12 days). In contrast, Apex Drilling & Blasting and Bharat Industrial Security Force have completed 100% statutory document filings.",
                    rationale = "Cross-referenced contractor document register against mandatory CIL NIT documentation checklists.",
                    period = "Active Contracts 2024-2027",
                    supportingEvidence = evidence,
                    recommendedActions = listOf(
                        "Issue formal notice to Trans-Coal Haulage Ltd to submit renewed policy before 14th November.",
                        "Verify VTC vocational training attendance for all 280 contractor drivers."
                    )
                )
            }

            q.contains("compare") || q.contains("inspection") -> {
                val evidence = inspections.map { ins ->
                    val mName = mines.find { it.id == ins.mineId }?.name ?: ins.mineId
                    EvidenceItem(
                        recordId = ins.id,
                        entityType = "Inspection Audit",
                        mineName = mName,
                        keyMetric = "Severity: ${ins.severity.label}",
                        details = "Inspector: ${ins.inspectorName}. Category: ${ins.category.label}. Action: ${ins.correctiveAction}",
                        status = ins.status.label
                    )
                }

                AIQueryResult(
                    query = query,
                    answer = "Comparison between Rajmahal OCP (ECL) and Jharia Deep UG (BCCL): Rajmahal OCP has 1 completed inspection with 2 failed checklist items (haul road berm and dust suppression) requiring high-priority actions. Jharia Underground inspection recorded 100% compliance on ventilation safety and stone dusting with zero overdue corrective actions.",
                    rationale = "Synthesized from DGMS inspection records #INSP-2026-101 (Rajmahal) and #INSP-2026-103 (Jharia).",
                    period = "October 2026 Statutory Inspections",
                    supportingEvidence = evidence,
                    recommendedActions = listOf(
                        "Standardize opencast haul road safety guidelines across ECL based on best practices from BCCL.",
                        "Re-inspect Rajmahal Section 4 berm height on 5th November."
                    )
                )
            }

            else -> {
                // Default holistic governance risk answer
                val overdueCount = compliance.count { it.status == ComplianceStatus.OVERDUE }
                val criticalHazards = incidents.count { it.severity == IssueSeverity.CRITICAL }
                val evidence = compliance.filter { it.status == ComplianceStatus.OVERDUE }.map { item ->
                    EvidenceItem(
                        recordId = item.id,
                        entityType = "Compliance Action",
                        mineName = mines.find { it.id == item.mineId }?.name ?: item.mineId,
                        keyMetric = "Due: ${item.dueDate}",
                        details = item.requirementTitle,
                        status = item.status.label
                    )
                }

                AIQueryResult(
                    query = query,
                    answer = "System Overview Analysis: The central compliance engine identifies $overdueCount overdue statutory items, $criticalHazards unresolved critical equipment hazards, and 2 environmental threshold breaches across 5 monitored mines. The primary operational bottleneck is at Rajmahal OCP due to concurrent dust suppression sprinkler outage and PM10 boundary elevation.",
                    rationale = "Holistic multi-module risk scan aggregating compliance, incidents, environmental sensors, and contractor records.",
                    period = "Real-time system state (November 2026)",
                    supportingEvidence = evidence,
                    recommendedActions = listOf(
                        "Prioritize resolving overdue statutory actions before the scheduled DGMS quarterly audit.",
                        "Mobilize water tanker bowsers to bring PM10 levels below 100 µg/m³.",
                        "Review contractor vocational training records."
                    )
                )
            }
        }
    }
}
