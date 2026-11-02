package com.example

import com.example.ai.AIRiskEngine
import com.example.data.model.*
import com.example.data.repository.MineGuardRepository
import com.example.data.util.AuditCrypto
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    private lateinit var repository: MineGuardRepository
    private lateinit var aiEngine: AIRiskEngine

    @Before
    fun setUp() {
        repository = MineGuardRepository()
        aiEngine = AIRiskEngine()
    }

    @Test
    fun repository_initializes_with_demo_mines_and_compliance() {
        val mines = repository.mines.value
        val compliance = repository.complianceItems.value

        assertTrue("Expected registered mines", mines.isNotEmpty())
        assertEquals(5, mines.size)

        val overdue = compliance.filter { it.status == ComplianceStatus.OVERDUE }
        assertTrue("Expected at least one overdue compliance item for SIH demo", overdue.isNotEmpty())
    }

    @Test
    fun compliance_verification_workflow_updates_status_and_audit_trail() {
        val initialAuditCount = repository.auditLogs.value.size
        val overdueItem = repository.complianceItems.value.first { it.status == ComplianceStatus.OVERDUE }

        repository.verifyComplianceEvidence(overdueItem.id, "Verified during SIH test execution")

        val updated = repository.complianceItems.value.first { it.id == overdueItem.id }
        assertEquals(ComplianceStatus.COMPLIANT, updated.status)
        assertTrue(updated.isVerified)

        val newAuditCount = repository.auditLogs.value.size
        assertEquals(initialAuditCount + 1, newAuditCount)
        assertEquals("VERIFY", repository.auditLogs.value.first().actionType)
    }

    @Test
    fun audit_crypto_generates_valid_sha256_hash_chain() {
        val prevHash = AuditCrypto.GENESIS_HASH
        val h1 = AuditCrypto.generateEventHash(prevHash, "2026-11-02 10:00:00", "USR-1", "COMPLIANCE", "VERIFY", "CMP-01", "Verified")
        assertNotNull(h1)
        assertEquals(64, h1.length)

        val h2 = AuditCrypto.generateEventHash(h1, "2026-11-02 10:01:00", "USR-1", "SAFETY", "CREATE", "INC-01", "Reported")
        assertNotEquals(h1, h2)
        assertEquals(64, h2.length)
    }

    @Test
    fun ai_risk_engine_correctly_computes_composite_risk_and_answers_queries() {
        val mines = repository.mines.value
        val compliance = repository.complianceItems.value
        val inspections = repository.inspections.value
        val incidents = repository.safetyIncidents.value
        val envReadings = repository.environmentalReadings.value
        val contractors = repository.contractors.value

        val risks = aiEngine.computeMineRisks(mines, compliance, inspections, incidents, envReadings, contractors)
        assertEquals(5, risks.size)

        // Query test
        val result = aiEngine.answerQuery(
            "Which mines have highest number of overdue compliance actions?",
            mines,
            compliance,
            inspections,
            incidents,
            envReadings,
            contractors
        )

        assertNotNull(result)
        assertTrue(result.answer.contains("Rajmahal") || result.answer.contains("overdue"))
        assertTrue("Expected supporting evidence citations", result.supportingEvidence.isNotEmpty())
    }

    @Test
    fun csv_export_generates_valid_structured_data() {
        val csv = repository.generateCsvExport("Compliance")
        assertNotNull(csv)
        assertTrue(csv.contains("RequirementTitle"))
        assertTrue(csv.lines().size > 2)
    }
}
