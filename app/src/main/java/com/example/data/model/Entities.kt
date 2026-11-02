package com.example.data.model

data class Mine(
    val id: String,
    val name: String,
    val subsidiary: String, // ECL, BCCL, CCL, WCL, SECL, MCL, NCL
    val mineType: MineType,
    val location: String,
    val state: String,
    val latitude: Double,
    val longitude: Double,
    val operationalStatus: OperationalStatus,
    val managerName: String,
    val managerContact: String,
    val registrationDate: String,
    val annualCapacityMTPA: Double,
    val notes: String
)

enum class MineType(val label: String) {
    OPENCAST("Opencast (OCP)"),
    UNDERGROUND("Underground (UG)"),
    MIXED("Mixed (OCP + UG)")
}

enum class OperationalStatus(val label: String) {
    ACTIVE("Operational"),
    MAINTENANCE("Maintenance"),
    UNDER_REVIEW("Under Review"),
    TEMPORARY_HALT("Temporary Halt")
}

data class ComplianceItem(
    val id: String,
    val mineId: String,
    val category: ComplianceCategory,
    val requirementTitle: String,
    val description: String,
    val applicableRegulation: String, // e.g. DGMS CMR 2017 Reg 104
    val responsibleDepartment: String,
    val responsibleOfficer: String,
    val applicableDate: String,
    val dueDate: String,
    val reviewFrequency: String, // Monthly, Quarterly, Annual
    val requiredEvidence: String,
    val status: ComplianceStatus,
    val lastReviewDate: String,
    val nextReviewDate: String,
    val evidenceDocumentUrl: String,
    val remarks: String,
    val isVerified: Boolean = false,
    val verifiedBy: String? = null
)

enum class ComplianceCategory(val label: String) {
    MINE_SAFETY("Mine Safety (DGMS)"),
    ENVIRONMENTAL("Environmental (MOEFCC/SPCB)"),
    LABOUR_WORKFORCE("Labour & Workforce"),
    OPERATIONAL("Operational Reporting"),
    STATUTORY_DOCS("Statutory Documentation"),
    OTHER("Configurable Standards")
}

enum class ComplianceStatus(val label: String) {
    COMPLIANT("Compliant"),
    PENDING("Pending"),
    UNDER_REVIEW("Under Review"),
    ACTION_REQUIRED("Action Required"),
    OVERDUE("Overdue"),
    NOT_APPLICABLE("Not Applicable")
}

data class InspectionChecklistItem(
    val id: String,
    val title: String,
    val isPassed: Boolean,
    val comments: String
)

data class Inspection(
    val id: String,
    val mineId: String,
    val category: InspectionCategory,
    val inspectorName: String,
    val scheduledDate: String,
    val actualDate: String?,
    val status: InspectionStatus,
    val checklist: List<InspectionChecklistItem>,
    val observations: String,
    val severity: IssueSeverity,
    val correctiveAction: String,
    val responsiblePerson: String,
    val targetCompletionDate: String,
    val reviewStatus: String,
    val evidenceNote: String = ""
)

enum class InspectionCategory(val label: String) {
    SAFETY("Safety Inspection"),
    ENVIRONMENTAL("Environmental Audit"),
    EQUIPMENT("Heavy Equipment & Machinery"),
    OPERATIONAL("Operational & Haulage"),
    COMPLIANCE("Statutory Compliance Review"),
    GENERAL_SITE("General Pit Inspection")
}

enum class InspectionStatus(val label: String) {
    SCHEDULED("Scheduled"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    OVERDUE("Overdue Closure")
}

enum class IssueSeverity(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical")
}

data class SafetyIncident(
    val id: String,
    val mineId: String,
    val location: String,
    val category: IncidentCategory,
    val description: String,
    val reportedDate: String,
    val reportedTime: String,
    val reporterName: String,
    val severity: IssueSeverity,
    val immediateActionTaken: String,
    val assignedOfficer: String,
    val correctiveActionStatus: ActionStatus,
    val closureNotes: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isOfflineDraft: Boolean = false
)

enum class IncidentCategory(val label: String) {
    UNSAFE_CONDITION("Unsafe Working Condition"),
    NEAR_MISS("Near Miss"),
    EQUIPMENT_HAZARD("Equipment / Machinery Hazard"),
    WORKPLACE_HAZARD("Workplace Hazard"),
    SAFETY_VIOLATION("SOP Violation"),
    MINOR_ACCIDENT("Minor Injury"),
    MAJOR_ACCIDENT("Reportable Dangerous Occurrence")
}

enum class ActionStatus(val label: String) {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    VERIFIED_CLOSED("Verified Closed")
}

data class EnvironmentalReading(
    val id: String,
    val mineId: String,
    val measurementType: EnvironmentalMetric,
    val value: Double,
    val unit: String,
    val thresholdMin: Double? = null,
    val thresholdMax: Double? = null,
    val isBreach: Boolean,
    val measurementDate: String,
    val measurementLocation: String,
    val instrumentOrSource: String,
    val responsiblePerson: String,
    val remarks: String
)

enum class EnvironmentalMetric(val label: String, val defaultUnit: String, val safeLimit: String) {
    PM10("PM10 Particulate Matter", "µg/m³", "≤ 100 µg/m³"),
    PM25("PM2.5 Fine Particulates", "µg/m³", "≤ 60 µg/m³"),
    SO2("Sulphur Dioxide (SO2)", "µg/m³", "≤ 80 µg/m³"),
    NO2("Nitrogen Oxides (NOx)", "µg/m³", "≤ 80 µg/m³"),
    WATER_PH("Effluent Discharge pH", "pH", "6.5 – 8.5"),
    WATER_TSS("Total Suspended Solids", "mg/L", "≤ 100 mg/L"),
    NOISE_LEVEL("Ambient Noise (Day)", "dB(A)", "≤ 75 dB(A)"),
    WATER_USAGE("Mine De-watering & Use", "kL/day", "≤ 1200 kL"),
    LAND_RESTORATION("Reclaimed Plantation Area", "Hectares", "≥ Target")
}

data class Contractor(
    val id: String,
    val companyName: String,
    val contractRef: String,
    val mineId: String,
    val workCategory: String, // Overburden, Haulage, Security, Maintenance
    val startDate: String,
    val endDate: String,
    val managerName: String,
    val workforceCount: Int,
    val requiredDocsUploaded: Int,
    val totalRequiredDocs: Int,
    val complianceStatus: ContractorComplianceStatus,
    val performanceRating: Double // 1.0 to 5.0
)

enum class ContractorComplianceStatus(val label: String) {
    VERIFIED_COMPLIANT("Verified Compliant"),
    UNDER_AUDIT("Audit In Progress"),
    DOCS_PENDING("Documents Expiring/Pending"),
    NON_COMPLIANT("Non-Compliant / Flagged")
}

data class WorkforceRecord(
    val id: String,
    val workerId: String,
    val workerName: String,
    val contractorId: String?,
    val mineId: String,
    val shift: String, // Shift A (06:00-14:00), Shift B, Shift C
    val attendanceDate: String,
    val status: AttendanceStatus,
    val checkInTime: String?,
    val checkOutTime: String?,
    val supervisorRemarks: String
)

enum class AttendanceStatus(val label: String) {
    PRESENT("Present"),
    ABSENT("Absent"),
    ON_LEAVE("On Leave"),
    SHIFT_REASSIGNED("Shift Reassigned")
}

data class OperationalReport(
    val id: String,
    val mineId: String,
    val reportingPeriod: String, // e.g. "October 2026", "Week 44"
    val metricName: String, // Coal Production, Overburden Removed, Equipment Availability, Diesel Consumed
    val value: Double,
    val unit: String,
    val source: String,
    val reportingOfficer: String,
    val verificationStatus: ReportVerificationStatus,
    val dateSubmitted: String
)

enum class ReportVerificationStatus(val label: String) {
    VERIFIED("Verified by General Manager"),
    PENDING_VERIFICATION("Pending Verification"),
    FLAGGED_DISCREPANCY("Discrepancy Flagged")
}

data class AlertItem(
    val id: String,
    val alertType: AlertType,
    val mineId: String,
    val mineName: String,
    val title: String,
    val description: String,
    val priority: IssueSeverity,
    val status: AlertStatus,
    val createdAt: String,
    val dueDate: String?,
    val responsibleOfficer: String,
    val escalationLevel: Int = 1
)

enum class AlertType(val label: String) {
    COMPLIANCE_DEADLINE("Statutory Compliance Deadline"),
    OVERDUE_ACTION("Overdue Corrective Action"),
    CRITICAL_SAFETY("Critical Safety Observation"),
    ENVIRONMENTAL_BREACH("Environmental Limit Exceeded"),
    CONTRACTOR_DOC_EXPIRY("Contractor Statutory Expiry"),
    INSPECTION_OVERDUE("Scheduled Inspection Due")
}

enum class AlertStatus(val label: String) {
    NEW("New"),
    ACKNOWLEDGED("Acknowledged"),
    ESCALATED("Escalated to GM/DGMS"),
    RESOLVED("Resolved")
}

data class AuditLogEntry(
    val id: String,
    val timestamp: String,
    val userId: String,
    val userName: String,
    val userRole: String,
    val actionType: String, // CREATE, UPDATE, DELETE, APPROVE, VERIFY, ESCALATE, EXPORT
    val module: String,
    val entityId: String,
    val description: String,
    val previousHash: String,
    val currentHash: String // SHA-256 tamper-evident link
)
