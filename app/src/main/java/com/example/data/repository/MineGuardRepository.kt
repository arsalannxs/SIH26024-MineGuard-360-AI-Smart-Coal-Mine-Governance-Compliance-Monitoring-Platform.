package com.example.data.repository

import com.example.data.model.*
import com.example.data.util.AuditCrypto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class MineGuardRepository {

    private val _currentUser = MutableStateFlow(
        CurrentUser(
            id = "USR-SIH-2026",
            name = "Er. A. K. Verma",
            role = UserRole.MINE_MANAGER,
            subsidiary = "ECL (Eastern Coalfields Ltd)",
            assignedMineId = "DEMO-MINE-01"
        )
    )
    val currentUser: StateFlow<CurrentUser> = _currentUser.asStateFlow()

    private val _mines = MutableStateFlow<List<Mine>>(emptyList())
    val mines: StateFlow<List<Mine>> = _mines.asStateFlow()

    private val _complianceItems = MutableStateFlow<List<ComplianceItem>>(emptyList())
    val complianceItems: StateFlow<List<ComplianceItem>> = _complianceItems.asStateFlow()

    private val _inspections = MutableStateFlow<List<Inspection>>(emptyList())
    val inspections: StateFlow<List<Inspection>> = _inspections.asStateFlow()

    private val _safetyIncidents = MutableStateFlow<List<SafetyIncident>>(emptyList())
    val safetyIncidents: StateFlow<List<SafetyIncident>> = _safetyIncidents.asStateFlow()

    private val _environmentalReadings = MutableStateFlow<List<EnvironmentalReading>>(emptyList())
    val environmentalReadings: StateFlow<List<EnvironmentalReading>> = _environmentalReadings.asStateFlow()

    private val _contractors = MutableStateFlow<List<Contractor>>(emptyList())
    val contractors: StateFlow<List<Contractor>> = _contractors.asStateFlow()

    private val _workforceRecords = MutableStateFlow<List<WorkforceRecord>>(emptyList())
    val workforceRecords: StateFlow<List<WorkforceRecord>> = _workforceRecords.asStateFlow()

    private val _operationalReports = MutableStateFlow<List<OperationalReport>>(emptyList())
    val operationalReports: StateFlow<List<OperationalReport>> = _operationalReports.asStateFlow()

    private val _alerts = MutableStateFlow<List<AlertItem>>(emptyList())
    val alerts: StateFlow<List<AlertItem>> = _alerts.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _selectedMineFilter = MutableStateFlow<String?>("ALL")
    val selectedMineFilter: StateFlow<String?> = _selectedMineFilter.asStateFlow()

    init {
        loadDemoData()
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    fun setCurrentRole(role: UserRole) {
        val old = _currentUser.value
        _currentUser.value = old.copy(role = role)
        appendAuditLog(
            actionType = "ROLE_SWITCH",
            module = "RBAC_SETTINGS",
            entityId = old.id,
            description = "User role switched to ${role.displayName} for demonstration."
        )
    }

    fun setSelectedMineFilter(mineId: String?) {
        _selectedMineFilter.value = mineId
    }

    fun toggleOfflineMode() {
        val next = !_isOfflineMode.value
        _isOfflineMode.value = next
        appendAuditLog(
            actionType = "NETWORK_TOGGLE",
            module = "SYSTEM",
            entityId = "CONN-STATE",
            description = "Network state toggled to ${if (next) "Offline Field Mode" else "Online Synced Mode"}."
        )
    }

    // --- AUDIT TRAIL CHAIN GENERATION ---
    @Synchronized
    private fun appendAuditLog(
        actionType: String,
        module: String,
        entityId: String,
        description: String
    ) {
        val currentList = _auditLogs.value
        val previousHash = if (currentList.isNotEmpty()) currentList.first().currentHash else AuditCrypto.GENESIS_HASH
        val timestamp = getCurrentTimestamp()
        val user = _currentUser.value

        val newHash = AuditCrypto.generateEventHash(
            previousHash = previousHash,
            timestamp = timestamp,
            userId = user.id,
            module = module,
            actionType = actionType,
            entityId = entityId,
            description = description
        )

        val entry = AuditLogEntry(
            id = "AUD-${System.currentTimeMillis()}-${(100..999).random()}",
            timestamp = timestamp,
            userId = user.id,
            userName = user.name,
            userRole = user.role.displayName,
            actionType = actionType,
            module = module,
            entityId = entityId,
            description = description,
            previousHash = previousHash,
            currentHash = newHash
        )
        // Store newest first for convenient display
        _auditLogs.value = listOf(entry) + currentList
    }

    // --- MINE OPERATIONS ---
    fun addMine(mine: Mine) {
        _mines.value = listOf(mine) + _mines.value
        appendAuditLog(
            actionType = "CREATE",
            module = "MINE_MGMT",
            entityId = mine.id,
            description = "Registered new mine colliery: ${mine.name} (${mine.subsidiary})"
        )
    }

    fun updateMine(updated: Mine) {
        _mines.value = _mines.value.map { if (it.id == updated.id) updated else it }
        appendAuditLog(
            actionType = "UPDATE",
            module = "MINE_MGMT",
            entityId = updated.id,
            description = "Updated colliery record details for ${updated.name}"
        )
    }

    fun deleteMine(mineId: String) {
        val target = _mines.value.find { it.id == mineId }
        _mines.value = _mines.value.filterNot { it.id == mineId }
        appendAuditLog(
            actionType = "DELETE",
            module = "MINE_MGMT",
            entityId = mineId,
            description = "De-registered colliery ${target?.name ?: mineId}"
        )
    }

    // --- COMPLIANCE OPERATIONS ---
    fun addComplianceItem(item: ComplianceItem) {
        _complianceItems.value = listOf(item) + _complianceItems.value
        appendAuditLog(
            actionType = "CREATE",
            module = "COMPLIANCE_TRACKER",
            entityId = item.id,
            description = "Logged statutory requirement: ${item.requirementTitle} under ${item.applicableRegulation}"
        )
        if (item.status == ComplianceStatus.ACTION_REQUIRED || item.status == ComplianceStatus.OVERDUE) {
            triggerAlert(
                alertType = AlertType.COMPLIANCE_DEADLINE,
                mineId = item.mineId,
                title = "Statutory Alert: ${item.requirementTitle}",
                description = "Requirement status is ${item.status.label}. Due: ${item.dueDate}",
                priority = if (item.status == ComplianceStatus.OVERDUE) IssueSeverity.CRITICAL else IssueSeverity.HIGH,
                dueDate = item.dueDate,
                officer = item.responsibleOfficer
            )
        }
    }

    fun updateComplianceItem(item: ComplianceItem) {
        _complianceItems.value = _complianceItems.value.map { if (it.id == item.id) item else it }
        appendAuditLog(
            actionType = "UPDATE",
            module = "COMPLIANCE_TRACKER",
            entityId = item.id,
            description = "Updated compliance item ${item.requirementTitle} to status ${item.status.label}"
        )
    }

    fun verifyComplianceEvidence(itemId: String, notes: String) {
        val user = _currentUser.value
        _complianceItems.value = _complianceItems.value.map {
            if (it.id == itemId) {
                it.copy(
                    isVerified = true,
                    verifiedBy = "${user.name} (${user.role.displayName})",
                    status = ComplianceStatus.COMPLIANT,
                    lastReviewDate = getCurrentTimestamp().substring(0, 10),
                    remarks = "${it.remarks} | Verified: $notes"
                )
            } else it
        }
        appendAuditLog(
            actionType = "VERIFY",
            module = "COMPLIANCE_TRACKER",
            entityId = itemId,
            description = "Officially verified statutory compliance evidence and marked as Compliant."
        )
    }

    // --- INSPECTION OPERATIONS ---
    fun scheduleInspection(inspection: Inspection) {
        _inspections.value = listOf(inspection) + _inspections.value
        appendAuditLog(
            actionType = "CREATE",
            module = "INSPECTIONS",
            entityId = inspection.id,
            description = "Scheduled ${inspection.category.label} by ${inspection.inspectorName} on ${inspection.scheduledDate}"
        )
        triggerAlert(
            alertType = AlertType.INSPECTION_OVERDUE,
            mineId = inspection.mineId,
            title = "Scheduled Inspection: ${inspection.category.label}",
            description = "Inspector: ${inspection.inspectorName}. Target Date: ${inspection.scheduledDate}",
            priority = IssueSeverity.MEDIUM,
            dueDate = inspection.scheduledDate,
            officer = inspection.inspectorName
        )
    }

    fun completeInspection(inspectionId: String, findings: String, correctiveAction: String, severity: IssueSeverity) {
        _inspections.value = _inspections.value.map {
            if (it.id == inspectionId) {
                it.copy(
                    status = InspectionStatus.COMPLETED,
                    actualDate = getCurrentTimestamp().substring(0, 10),
                    observations = findings,
                    correctiveAction = correctiveAction,
                    severity = severity,
                    reviewStatus = "Pending Verification by DGMS / Manager"
                )
            } else it
        }
        appendAuditLog(
            actionType = "COMPLETE",
            module = "INSPECTIONS",
            entityId = inspectionId,
            description = "Completed inspection findings with severity ${severity.label} and corrective action assigned."
        )
        if (severity == IssueSeverity.CRITICAL || severity == IssueSeverity.HIGH) {
            triggerAlert(
                alertType = AlertType.OVERDUE_ACTION,
                mineId = _inspections.value.find { it.id == inspectionId }?.mineId ?: "UNKNOWN",
                title = "Inspection Action: ${severity.label} Severity",
                description = "Corrective Action required: $correctiveAction",
                priority = severity,
                dueDate = getCurrentTimestamp().substring(0, 10),
                officer = _currentUser.value.name
            )
        }
    }

    // --- SAFETY & INCIDENTS ---
    fun reportSafetyIncident(incident: SafetyIncident) {
        _safetyIncidents.value = listOf(incident) + _safetyIncidents.value
        appendAuditLog(
            actionType = "CREATE",
            module = "SAFETY_INCIDENTS",
            entityId = incident.id,
            description = "Reported safety hazard: ${incident.category.label} (${incident.severity.label}) at ${incident.location}"
        )
        if (incident.severity == IssueSeverity.CRITICAL || incident.severity == IssueSeverity.HIGH) {
            triggerAlert(
                alertType = AlertType.CRITICAL_SAFETY,
                mineId = incident.mineId,
                title = "CRITICAL SAFETY: ${incident.category.label}",
                description = "${incident.description}. Immediate action: ${incident.immediateActionTaken}",
                priority = incident.severity,
                dueDate = incident.reportedDate,
                officer = incident.assignedOfficer
            )
        }
    }

    fun updateIncidentStatus(incidentId: String, status: ActionStatus, notes: String) {
        _safetyIncidents.value = _safetyIncidents.value.map {
            if (it.id == incidentId) {
                it.copy(
                    correctiveActionStatus = status,
                    closureNotes = if (notes.isNotBlank()) "${it.closureNotes}\n[${getCurrentTimestamp()}]: $notes".trim() else it.closureNotes
                )
            } else it
        }
        appendAuditLog(
            actionType = "UPDATE",
            module = "SAFETY_INCIDENTS",
            entityId = incidentId,
            description = "Updated incident status to ${status.label} with notes: $notes"
        )
    }

    // --- ENVIRONMENTAL READINGS ---
    fun addEnvironmentalReading(reading: EnvironmentalReading) {
        _environmentalReadings.value = listOf(reading) + _environmentalReadings.value
        appendAuditLog(
            actionType = "CREATE",
            module = "ENV_MONITORING",
            entityId = reading.id,
            description = "Recorded ${reading.measurementType.label}: ${reading.value} ${reading.unit} at ${reading.measurementLocation}"
        )
        if (reading.isBreach) {
            triggerAlert(
                alertType = AlertType.ENVIRONMENTAL_BREACH,
                mineId = reading.mineId,
                title = "ENVIRONMENT LIMIT EXCEEDED: ${reading.measurementType.label}",
                description = "Observed ${reading.value} ${reading.unit} (Safe limit: ${reading.measurementType.safeLimit}) at ${reading.measurementLocation}",
                priority = IssueSeverity.HIGH,
                dueDate = reading.measurementDate,
                officer = reading.responsiblePerson
            )
        }
    }

    // --- CONTRACTORS ---
    fun registerContractor(contractor: Contractor) {
        _contractors.value = listOf(contractor) + _contractors.value
        appendAuditLog(
            actionType = "CREATE",
            module = "CONTRACTOR_MGMT",
            entityId = contractor.id,
            description = "Registered mining contractor ${contractor.companyName} (${contractor.workCategory})"
        )
    }

    fun updateContractor(contractor: Contractor) {
        _contractors.value = _contractors.value.map { if (it.id == contractor.id) contractor else it }
        appendAuditLog(
            actionType = "UPDATE",
            module = "CONTRACTOR_MGMT",
            entityId = contractor.id,
            description = "Updated contractor status for ${contractor.companyName}"
        )
    }

    // --- WORKFORCE ATTENDANCE ---
    fun logAttendance(record: WorkforceRecord) {
        _workforceRecords.value = listOf(record) + _workforceRecords.value
        appendAuditLog(
            actionType = "CREATE",
            module = "WORKFORCE",
            entityId = record.id,
            description = "Logged attendance for ${record.workerName} (${record.shift}) as ${record.status.label}"
        )
    }

    // --- OPERATIONAL REPORTS ---
    fun addOperationalReport(report: OperationalReport) {
        _operationalReports.value = listOf(report) + _operationalReports.value
        appendAuditLog(
            actionType = "CREATE",
            module = "OPERATIONAL_REPORTS",
            entityId = report.id,
            description = "Submitted operational metric: ${report.metricName} = ${report.value} ${report.unit} for ${report.reportingPeriod}"
        )
    }

    // --- ALERTS ---
    private fun triggerAlert(
        alertType: AlertType,
        mineId: String,
        title: String,
        description: String,
        priority: IssueSeverity,
        dueDate: String?,
        officer: String
    ) {
        val mineName = _mines.value.find { it.id == mineId }?.name ?: "All Mines"
        val alert = AlertItem(
            id = "ALT-${System.currentTimeMillis()}-${(10..99).random()}",
            alertType = alertType,
            mineId = mineId,
            mineName = mineName,
            title = title,
            description = description,
            priority = priority,
            status = AlertStatus.NEW,
            createdAt = getCurrentTimestamp(),
            dueDate = dueDate,
            responsibleOfficer = officer
        )
        _alerts.value = listOf(alert) + _alerts.value
    }

    fun acknowledgeAlert(alertId: String) {
        _alerts.value = _alerts.value.map {
            if (it.id == alertId) it.copy(status = AlertStatus.ACKNOWLEDGED) else it
        }
        appendAuditLog(
            actionType = "UPDATE",
            module = "ALERTS",
            entityId = alertId,
            description = "Acknowledged alert notifications"
        )
    }

    fun escalateAlert(alertId: String) {
        _alerts.value = _alerts.value.map {
            if (it.id == alertId) it.copy(status = AlertStatus.ESCALATED, escalationLevel = it.escalationLevel + 1) else it
        }
        appendAuditLog(
            actionType = "ESCALATE",
            module = "ALERTS",
            entityId = alertId,
            description = "Escalated priority alert to General Manager and DGMS Area Director"
        )
    }

    fun resolveAlert(alertId: String) {
        _alerts.value = _alerts.value.map {
            if (it.id == alertId) it.copy(status = AlertStatus.RESOLVED) else it
        }
        appendAuditLog(
            actionType = "RESOLVE",
            module = "ALERTS",
            entityId = alertId,
            description = "Resolved alert and closed active escalation loop"
        )
    }

    fun syncOfflineDrafts() {
        var syncedCount = 0
        _safetyIncidents.value = _safetyIncidents.value.map {
            if (it.isOfflineDraft) {
                syncedCount++
                it.copy(isOfflineDraft = false)
            } else it
        }
        if (syncedCount > 0) {
            appendAuditLog(
                actionType = "SYNC",
                module = "OFFLINE_SYNC",
                entityId = "SYNC-$syncedCount",
                description = "Synchronized $syncedCount field observation drafts from local storage to central server."
            )
        }
    }

    // --- CSV EXPORT GENERATOR ---
    fun generateCsvExport(moduleName: String): String {
        appendAuditLog(
            actionType = "EXPORT",
            module = "REPORTS_EXPORT",
            entityId = moduleName,
            description = "Generated statutory CSV audit export for $moduleName"
        )
        val sb = StringBuilder()
        when (moduleName) {
            "Compliance" -> {
                sb.append("ID,MineID,Category,RequirementTitle,Regulation,DueDate,Status,Verified\n")
                _complianceItems.value.forEach {
                    sb.append("\"${it.id}\",\"${it.mineId}\",\"${it.category.label}\",\"${it.requirementTitle}\",\"${it.applicableRegulation}\",\"${it.dueDate}\",\"${it.status.label}\",\"${it.isVerified}\"\n")
                }
            }
            "Inspections" -> {
                sb.append("ID,MineID,Category,Inspector,ScheduledDate,ActualDate,Status,Severity,CorrectiveAction\n")
                _inspections.value.forEach {
                    sb.append("\"${it.id}\",\"${it.mineId}\",\"${it.category.label}\",\"${it.inspectorName}\",\"${it.scheduledDate}\",\"${it.actualDate ?: ""}\",\"${it.status.label}\",\"${it.severity.label}\",\"${it.correctiveAction}\"\n")
                }
            }
            "Safety" -> {
                sb.append("ID,MineID,Location,Category,Severity,ReportedDate,Status,Description\n")
                _safetyIncidents.value.forEach {
                    sb.append("\"${it.id}\",\"${it.mineId}\",\"${it.location}\",\"${it.category.label}\",\"${it.severity.label}\",\"${it.reportedDate}\",\"${it.correctiveActionStatus.label}\",\"${it.description}\"\n")
                }
            }
            "Environmental" -> {
                sb.append("ID,MineID,Metric,Value,Unit,Location,IsBreach,Date,Responsible\n")
                _environmentalReadings.value.forEach {
                    sb.append("\"${it.id}\",\"${it.mineId}\",\"${it.measurementType.label}\",\"${it.value}\",\"${it.unit}\",\"${it.measurementLocation}\",\"${it.isBreach}\",\"${it.measurementDate}\",\"${it.responsiblePerson}\"\n")
                }
            }
            "Contractors" -> {
                sb.append("ID,Company,ContractRef,MineID,Category,ComplianceStatus,Workforce\n")
                _contractors.value.forEach {
                    sb.append("\"${it.id}\",\"${it.companyName}\",\"${it.contractRef}\",\"${it.mineId}\",\"${it.workCategory}\",\"${it.complianceStatus.label}\",\"${it.workforceCount}\"\n")
                }
            }
            else -> {
                sb.append("ID,Timestamp,User,Role,Action,Module,EntityID,Description,SHA256Hash\n")
                _auditLogs.value.forEach {
                    sb.append("\"${it.id}\",\"${it.timestamp}\",\"${it.userName}\",\"${it.userRole}\",\"${it.actionType}\",\"${it.module}\",\"${it.entityId}\",\"${it.description}\",\"${it.currentHash}\"\n")
                }
            }
        }
        return sb.toString()
    }

    // --- SEED SAMPLE DATA (SIH DEMONSTRATION) ---
    fun resetDemoData() {
        loadDemoData()
        appendAuditLog(
            actionType = "RESET",
            module = "SYSTEM",
            entityId = "DEMO-RESET",
            description = "Reset system database to standard SIH evaluation demonstration baseline."
        )
    }

    fun loadDemoData() {
        val sampleMines = listOf(
            Mine(
                id = "DEMO-MINE-01",
                name = "Rajmahal Open Cast Project",
                subsidiary = "ECL (Eastern Coalfields)",
                mineType = MineType.OPENCAST,
                location = "Godda District",
                state = "Jharkhand",
                latitude = 25.0412,
                longitude = 87.3298,
                operationalStatus = OperationalStatus.ACTIVE,
                managerName = "Er. A. K. Verma",
                managerContact = "+91 94311 02910",
                registrationDate = "2018-04-15",
                annualCapacityMTPA = 17.5,
                notes = "High-output mechanized opencast supplying Farakka & Kahalgaon thermal plants."
            ),
            Mine(
                id = "DEMO-MINE-02",
                name = "Jharia Deep Underground Colliery",
                subsidiary = "BCCL (Bharat Coking Coal)",
                mineType = MineType.UNDERGROUND,
                location = "Dhanbad Coalfield",
                state = "Jharkhand",
                latitude = 23.7511,
                longitude = 86.4194,
                operationalStatus = OperationalStatus.ACTIVE,
                managerName = "S. N. Mukherjee",
                managerContact = "+91 94313 88120",
                registrationDate = "2016-09-01",
                annualCapacityMTPA = 3.2,
                notes = "Prime coking coal extraction with specialized continuous miner and telemetric gas monitoring."
            ),
            Mine(
                id = "DEMO-MINE-03",
                name = "Gevra Mega Opencast Project",
                subsidiary = "SECL (South Eastern Coalfields)",
                mineType = MineType.OPENCAST,
                location = "Korba Basin",
                state = "Chhattisgarh",
                latitude = 22.3385,
                longitude = 82.5978,
                operationalStatus = OperationalStatus.ACTIVE,
                managerName = "P. K. Rathore",
                managerContact = "+91 94252 44319",
                registrationDate = "2014-02-10",
                annualCapacityMTPA = 52.0,
                notes = "One of Asia's largest opencast coal mines with heavy in-pit conveyor crushers."
            ),
            Mine(
                id = "DEMO-MINE-04",
                name = "Talcher Basin Colliery (Ananta OCP)",
                subsidiary = "MCL (Mahanadi Coalfields)",
                mineType = MineType.OPENCAST,
                location = "Angul District",
                state = "Odisha",
                latitude = 20.9504,
                longitude = 85.2167,
                operationalStatus = OperationalStatus.ACTIVE,
                managerName = "Debashis Mohanty",
                managerContact = "+91 94370 77123",
                registrationDate = "2019-11-20",
                annualCapacityMTPA = 20.0,
                notes = "Major non-coking coal source for national grid with active eco-restoration plantation."
            ),
            Mine(
                id = "DEMO-MINE-05",
                name = "North Karanpura Coalfield (Pipwar OCP)",
                subsidiary = "CCL (Central Coalfields)",
                mineType = MineType.MIXED,
                location = "Chatra District",
                state = "Jharkhand",
                latitude = 23.8291,
                longitude = 85.0315,
                operationalStatus = OperationalStatus.MAINTENANCE,
                managerName = "K. B. Singh",
                managerContact = "+91 94317 22890",
                registrationDate = "2017-06-12",
                annualCapacityMTPA = 11.0,
                notes = "Mobile In-Pit Crushing & Conveying (IPCC) and coal washery facility."
            )
        )

        val sampleCompliance = listOf(
            ComplianceItem(
                id = "CMP-2026-001",
                mineId = "DEMO-MINE-01",
                category = ComplianceCategory.MINE_SAFETY,
                requirementTitle = "Continuous Dust Suppression along Haul Roads",
                description = "Automated water mist sprinkler operation every 45 mins to maintain respirable airborne dust under statutory limits.",
                applicableRegulation = "DGMS Coal Mines Regulations 2017, Regulation 104",
                responsibleDepartment = "Safety & Civil Engineering",
                responsibleOfficer = "Rajesh Sharma",
                applicableDate = "2026-10-01",
                dueDate = "2026-10-25", // OVERDUE in current context!
                reviewFrequency = "Fortnightly",
                requiredEvidence = "Telemetry flow-meter log and photographic proof of operational fixed mist sprayers",
                status = ComplianceStatus.OVERDUE,
                lastReviewDate = "2026-10-10",
                nextReviewDate = "2026-10-25",
                evidenceDocumentUrl = "https://sih.mineguard.internal/docs/haulroad-sprinkler-audit-v1.pdf",
                remarks = "Pump #3 booster motor reported faulty on Section C. Repair tender initiated.",
                isVerified = false,
                verifiedBy = null
            ),
            ComplianceItem(
                id = "CMP-2026-002",
                mineId = "DEMO-MINE-01",
                category = ComplianceCategory.MINE_SAFETY,
                requirementTitle = "Pit Bench Slope Stability Radar Telemetry Log",
                description = "Daily geotechnical radar telemetry validation and slope movement velocity checks on Highwall #4.",
                applicableRegulation = "DGMS Technical Circular (CMR 2017 Reg 106)",
                responsibleDepartment = "Strata & Geotechnical",
                responsibleOfficer = "Dr. Manas Roy",
                applicableDate = "2026-11-01",
                dueDate = "2026-11-07",
                reviewFrequency = "Weekly",
                requiredEvidence = "Geotechnical sensor displacement graphs (< 2mm/day safe threshold)",
                status = ComplianceStatus.COMPLIANT,
                lastReviewDate = "2026-11-01",
                nextReviewDate = "2026-11-08",
                evidenceDocumentUrl = "https://sih.mineguard.internal/docs/slope-radar-wk44.pdf",
                remarks = "Sensor SSR-02 operating nominally with zero displacement trend.",
                isVerified = true,
                verifiedBy = "Er. A. K. Verma (Mine Manager)"
            ),
            ComplianceItem(
                id = "CMP-2026-003",
                mineId = "DEMO-MINE-01",
                category = ComplianceCategory.ENVIRONMENTAL,
                requirementTitle = "Ambient PM10 & PM2.5 Boundary Station Monitoring",
                description = "Continuous ambient air quality data transmission to SPCB portal with certified calibration certificate.",
                applicableRegulation = "Air (Prevention and Control of Pollution) Act 1981 / MOEFCC EC Clause VI",
                responsibleDepartment = "Environmental Cell",
                responsibleOfficer = "Ananya Das",
                applicableDate = "2026-10-15",
                dueDate = "2026-11-05",
                reviewFrequency = "Monthly",
                requiredEvidence = "SPCB server sync receipt & third-party NABL test report",
                status = ComplianceStatus.ACTION_REQUIRED,
                lastReviewDate = "2026-10-15",
                nextReviewDate = "2026-11-05",
                evidenceDocumentUrl = "https://sih.mineguard.internal/docs/spcb-air-transmission.pdf",
                remarks = "Station 2 optical dust sensor required recalibration after heavy monsoon rains.",
                isVerified = false,
                verifiedBy = null
            ),
            ComplianceItem(
                id = "CMP-2026-004",
                mineId = "DEMO-MINE-02",
                category = ComplianceCategory.MINE_SAFETY,
                requirementTitle = "Methane & Carbon Monoxide Continuous Tele-monitoring",
                description = "Automatic cut-off interlocking verification for underground coal face ventilation sensors.",
                applicableRegulation = "DGMS Coal Mines Regulations 2017, Regulation 144 & 148",
                responsibleDepartment = "Ventilation & Safety",
                responsibleOfficer = "B. K. Sen",
                applicableDate = "2026-10-20",
                dueDate = "2026-11-10",
                reviewFrequency = "Weekly",
                requiredEvidence = "Calibration chamber gas certificate and sensor fail-safe trigger logs",
                status = ComplianceStatus.COMPLIANT,
                lastReviewDate = "2026-10-28",
                nextReviewDate = "2026-11-05",
                evidenceDocumentUrl = "https://sih.mineguard.internal/docs/ug-telemetry-gas-v4.pdf",
                remarks = "All 18 CH4 sensors tested. Automatic trip operated within 1.2 seconds.",
                isVerified = true,
                verifiedBy = "S. N. Mukherjee (Mine Manager)"
            ),
            ComplianceItem(
                id = "CMP-2026-005",
                mineId = "DEMO-MINE-03",
                category = ComplianceCategory.LABOUR_WORKFORCE,
                requirementTitle = "Statutory Vocational Safety Training for Contract Labourers",
                description = "Mandatory 6-day DGMS refresher training and issue of Form B employment cards prior to pit entry.",
                applicableRegulation = "Mines Vocational Training Rules 1966 / Mines Act 1952",
                responsibleDepartment = "HR & VTC",
                responsibleOfficer = "V. N. Tripathi",
                applicableDate = "2026-10-01",
                dueDate = "2026-10-20",
                reviewFrequency = "Quarterly",
                requiredEvidence = "VTC attendance register, biometric verification and DGMS certificates",
                status = ComplianceStatus.OVERDUE,
                lastReviewDate = "2026-10-05",
                nextReviewDate = "2026-10-20",
                evidenceDocumentUrl = "https://sih.mineguard.internal/docs/vtc-batch-oct26.pdf",
                remarks = "Batch #3 of Trans-Coal drivers missed defensive driving module due to shift conflict.",
                isVerified = false,
                verifiedBy = null
            ),
            ComplianceItem(
                id = "CMP-2026-006",
                mineId = "DEMO-MINE-04",
                category = ComplianceCategory.ENVIRONMENTAL,
                requirementTitle = "Zero Effluent Discharge Sump Water Treatment",
                description = "Treatment and 100% recycling of mine water discharge for dust suppression and local irrigation.",
                applicableRegulation = "Water (Prevention and Control of Pollution) Act 1974",
                responsibleDepartment = "Civil & Environment",
                responsibleOfficer = "Sunil Pattnaik",
                applicableDate = "2026-10-10",
                dueDate = "2026-11-15",
                reviewFrequency = "Monthly",
                requiredEvidence = "Sedimentation tank TSS test reports and effluent flow log",
                status = ComplianceStatus.COMPLIANT,
                lastReviewDate = "2026-10-25",
                nextReviewDate = "2026-11-15",
                evidenceDocumentUrl = "https://sih.mineguard.internal/docs/water-treatment-oct26.pdf",
                remarks = "TSS levels maintained at 42 mg/L, well within 100 mg/L permissible limit.",
                isVerified = true,
                verifiedBy = "Debashis Mohanty (Manager)"
            )
        )

        val sampleInspections = listOf(
            Inspection(
                id = "INSP-2026-101",
                mineId = "DEMO-MINE-01",
                category = InspectionCategory.SAFETY,
                inspectorName = "DGMS Dy. Director (Mines)",
                scheduledDate = "2026-10-24",
                actualDate = "2026-10-24",
                status = InspectionStatus.COMPLETED,
                checklist = listOf(
                    InspectionChecklistItem("CHK-01", "Haul road width >= 3 times largest vehicle", true, "Complies (36m width verified)"),
                    InspectionChecklistItem("CHK-02", "Berm height >= tyre radius of largest dumper", false, "Section 4 berm height is only 1.1m (required 1.8m)"),
                    InspectionChecklistItem("CHK-03", "Dust suppression operational", false, "Sprinkler line pressure down on Section C"),
                    InspectionChecklistItem("CHK-04", "Night lighting lux level >= 15 lux at pit benches", true, "Verified 22 lux average")
                ),
                observations = "Highwall bench #4 shows stable strata. Haul road berm near Section 4 requires immediate dressing to reach 1.8m safety height.",
                severity = IssueSeverity.HIGH,
                correctiveAction = "Raise and pack haul road earthen bunds to 1.8m before 5th Nov.",
                responsiblePerson = "Er. A. K. Verma",
                targetCompletionDate = "2026-11-05",
                reviewStatus = "Pending Verification by DGMS Inspector",
                evidenceNote = "Inspection memo #DGMS/NZ/2026/891 issued."
            ),
            Inspection(
                id = "INSP-2026-102",
                mineId = "DEMO-MINE-01",
                category = InspectionCategory.EQUIPMENT,
                inspectorName = "Chief Mechanical Engineer",
                scheduledDate = "2026-11-04",
                actualDate = null,
                status = InspectionStatus.SCHEDULED,
                checklist = listOf(
                    InspectionChecklistItem("CHK-11", "Dumper emergency steering & brake fail test", true, "Pending execution"),
                    InspectionChecklistItem("CHK-12", "Proximity warning sensors calibrated", true, "Pending execution"),
                    InspectionChecklistItem("CHK-13", "Audio-visual alarm (AVR) functionality", true, "Pending execution")
                ),
                observations = "Routine quarterly statutory audit of all 240-tonne electric dumpers and hydraulic excavators.",
                severity = IssueSeverity.MEDIUM,
                correctiveAction = "Audit all contractor HEMM fitness certificates.",
                responsiblePerson = "Mechanical HOD",
                targetCompletionDate = "2026-11-08",
                reviewStatus = "Scheduled",
                evidenceNote = ""
            ),
            Inspection(
                id = "INSP-2026-103",
                mineId = "DEMO-MINE-02",
                category = InspectionCategory.COMPLIANCE,
                inspectorName = "Internal Safety Auditor",
                scheduledDate = "2026-10-18",
                actualDate = "2026-10-18",
                status = InspectionStatus.COMPLETED,
                checklist = listOf(
                    InspectionChecklistItem("CHK-21", "Self-Contained Self Rescuers (SCSR) checked", true, "100% units verified active"),
                    InspectionChecklistItem("CHK-22", "Flame safety lamp station checked", true, "Passed"),
                    InspectionChecklistItem("CHK-23", "Stone dusting on roadway walls", true, "Incombustible content > 75%")
                ),
                observations = "Underground ventilation circuit air velocity is compliant. Stone dust barriers in Seam IX intact.",
                severity = IssueSeverity.LOW,
                correctiveAction = "Maintain weekly stone dust replenishment log.",
                responsiblePerson = "Ventilation Officer",
                targetCompletionDate = "2026-10-25",
                reviewStatus = "Verified Closed",
                evidenceNote = "Signed by BCCL Area Safety Officer."
            )
        )

        val sampleIncidents = listOf(
            SafetyIncident(
                id = "INC-2026-501",
                mineId = "DEMO-MINE-01",
                location = "Haul Road Ramp #2 (Bench 4)",
                category = IncidentCategory.UNSAFE_CONDITION,
                description = "Continuous spillage of boulders from overloaded haulage dumpers causing tyre puncture hazard and roadway obstruction.",
                reportedDate = "2026-11-01",
                reportedTime = "08:45 AM",
                reporterName = "Dilip Soren (Shift Supervisor)",
                severity = IssueSeverity.HIGH,
                immediateActionTaken = "Wheel loader dispatched to clear ramp. Dumper operator cautioned.",
                assignedOfficer = "M. K. Jha (Haulage In-charge)",
                correctiveActionStatus = ActionStatus.IN_PROGRESS,
                closureNotes = "Installing automated weigh-bridge tailgate sensors to prevent overfilling.",
                latitude = 25.0425,
                longitude = 87.3312,
                isOfflineDraft = false
            ),
            SafetyIncident(
                id = "INC-2026-502",
                mineId = "DEMO-MINE-01",
                location = "Pit Workshop Bay 3",
                category = IncidentCategory.EQUIPMENT_HAZARD,
                description = "High-pressure hydraulic hose failure on Komatsu PC-2000 shovel during swing cycle. Hydraulic oil atomized spray.",
                reportedDate = "2026-10-29",
                reportedTime = "14:10 PM",
                reporterName = "R. P. Yadav",
                severity = IssueSeverity.CRITICAL,
                immediateActionTaken = "Machine emergency stop engaged. Fire extinguisher kept ready. Area cordoned.",
                assignedOfficer = "Chief Mechanical Engineer",
                correctiveActionStatus = ActionStatus.RESOLVED,
                closureNotes = "Damaged SAE-100R15 hose replaced. All sister machines inspected for abrasion wear.",
                latitude = 25.0398,
                longitude = 87.3275,
                isOfflineDraft = false
            ),
            SafetyIncident(
                id = "INC-2026-503",
                mineId = "DEMO-MINE-03",
                location = "Overburden Highwall East Face",
                category = IncidentCategory.NEAR_MISS,
                description = "Small tension crack (15mm) observed along top crest of Bench 6 after overnight blasting.",
                reportedDate = "2026-10-31",
                reportedTime = "07:15 AM",
                reporterName = "Geotechnical Surveyor",
                severity = IssueSeverity.HIGH,
                immediateActionTaken = "Shovel relocated 40m back from toe. Visual prism monitors positioned.",
                assignedOfficer = "Dr. Manas Roy",
                correctiveActionStatus = ActionStatus.OPEN,
                closureNotes = "Radar continuous scan initiated. Berm unloading plan submitted to DGMS.",
                latitude = 22.3392,
                longitude = 82.5985,
                isOfflineDraft = false
            ),
            SafetyIncident(
                id = "INC-2026-504",
                mineId = "DEMO-MINE-01",
                location = "Water Tanker Refill Station",
                category = IncidentCategory.SAFETY_VIOLATION,
                description = "Contractor driver operating water sprinkler tanker without fluorescent safety jacket and hard hat.",
                reportedDate = "2026-11-02",
                reportedTime = "06:30 AM",
                reporterName = "Security Guard",
                severity = IssueSeverity.LOW,
                immediateActionTaken = "Driver issued warning memo and mandatory PPE provided before shift entry.",
                assignedOfficer = "Contractor Supervisor",
                correctiveActionStatus = ActionStatus.VERIFIED_CLOSED,
                closureNotes = "Contractor penalized per clause 14 of safety handbook.",
                latitude = 25.0401,
                longitude = 87.3289,
                isOfflineDraft = false
            )
        )

        val sampleEnvReadings = listOf(
            EnvironmentalReading(
                id = "ENV-2026-001",
                mineId = "DEMO-MINE-01",
                measurementType = EnvironmentalMetric.PM10,
                value = 138.5, // BREACH (> 100)
                unit = "µg/m³",
                thresholdMin = 0.0,
                thresholdMax = 100.0,
                isBreach = true,
                measurementDate = "2026-11-02 08:00",
                measurementLocation = "Boundary Station #2 (Lease Boundary East)",
                instrumentOrSource = "Continuous Ambient BAM-1020 Beta Gauge",
                responsiblePerson = "Ananya Das (Env Officer)",
                remarks = "Exceeded threshold due to dry north-easterly wind and sprinkler line outage on Section C."
            ),
            EnvironmentalReading(
                id = "ENV-2026-002",
                mineId = "DEMO-MINE-01",
                measurementType = EnvironmentalMetric.PM25,
                value = 52.0, // OK (<= 60)
                unit = "µg/m³",
                thresholdMin = 0.0,
                thresholdMax = 60.0,
                isBreach = false,
                measurementDate = "2026-11-02 08:00",
                measurementLocation = "Boundary Station #2 (Lease Boundary East)",
                instrumentOrSource = "Laser Spectrometer PM Sensor",
                responsiblePerson = "Ananya Das",
                remarks = "Within prescribed CPCB limits."
            ),
            EnvironmentalReading(
                id = "ENV-2026-003",
                mineId = "DEMO-MINE-01",
                measurementType = EnvironmentalMetric.WATER_PH,
                value = 7.4, // OK (6.5 - 8.5)
                unit = "pH",
                thresholdMin = 6.5,
                thresholdMax = 8.5,
                isBreach = false,
                measurementDate = "2026-11-01 16:30",
                measurementLocation = "Sedimentation Sump Outlet #1",
                instrumentOrSource = "Digital Glass Electrode pH Meter",
                responsiblePerson = "Sunil Pattnaik",
                remarks = "Neutral effluent; zero acid mine drainage."
            ),
            EnvironmentalReading(
                id = "ENV-2026-004",
                mineId = "DEMO-MINE-01",
                measurementType = EnvironmentalMetric.NOISE_LEVEL,
                value = 79.2, // BREACH (> 75 dB)
                unit = "dB(A)",
                thresholdMin = 0.0,
                thresholdMax = 75.0,
                isBreach = true,
                measurementDate = "2026-11-01 11:15",
                measurementLocation = "Coal Handling Plant (CHP) Feeder Breaker",
                instrumentOrSource = "Type 1 Sound Level Calibrated Meter",
                responsiblePerson = "Safety Officer",
                remarks = "Breaker secondary screen chute liner wear identified. Scheduled for rubber lining replacement."
            ),
            EnvironmentalReading(
                id = "ENV-2026-005",
                mineId = "DEMO-MINE-03",
                measurementType = EnvironmentalMetric.PM10,
                value = 88.0,
                unit = "µg/m³",
                thresholdMin = 0.0,
                thresholdMax = 100.0,
                isBreach = false,
                measurementDate = "2026-11-02 07:00",
                measurementLocation = "Gevra Silo Dispatch Rail Yard",
                instrumentOrSource = "Online CAAQMS Station",
                responsiblePerson = "S. K. Dubey",
                remarks = "Automatic fog mist cannons active during rake loading."
            )
        )

        val sampleContractors = listOf(
            Contractor(
                id = "CONTR-2026-01",
                companyName = "Trans-Coal Haulage & Earthmovers Ltd",
                contractRef = "CIL/ECL/NIT-2024/772",
                mineId = "DEMO-MINE-01",
                workCategory = "Overburden Removal & Dumper Haulage",
                startDate = "2024-04-01",
                endDate = "2027-03-31",
                managerName = "Sanjay Agrawal",
                workforceCount = 280,
                requiredDocsUploaded = 11,
                totalRequiredDocs = 12, // 1 pending doc
                complianceStatus = ContractorComplianceStatus.DOCS_PENDING,
                performanceRating = 4.2
            ),
            Contractor(
                id = "CONTR-2026-02",
                companyName = "Apex Drilling & Controlled Blasting Services",
                contractRef = "CIL/ECL/NIT-2025/119",
                mineId = "DEMO-MINE-01",
                workCategory = "Blast Hole Drilling & Bulk Emulsion",
                startDate = "2025-01-15",
                endDate = "2026-12-31",
                managerName = "Capt. R. S. Negi",
                workforceCount = 65,
                requiredDocsUploaded = 10,
                totalRequiredDocs = 10,
                complianceStatus = ContractorComplianceStatus.VERIFIED_COMPLIANT,
                performanceRating = 4.8
            ),
            Contractor(
                id = "CONTR-2026-03",
                companyName = "Bharat Industrial Security Force Pvt Ltd",
                contractRef = "CIL/SECL/NIT-2024/405",
                mineId = "DEMO-MINE-03",
                workCategory = "Perimeter Security & In-Pit Gate Vigilance",
                startDate = "2024-06-01",
                endDate = "2026-05-31",
                managerName = "K. K. Deshmukh",
                workforceCount = 120,
                requiredDocsUploaded = 9,
                totalRequiredDocs = 9,
                complianceStatus = ContractorComplianceStatus.VERIFIED_COMPLIANT,
                performanceRating = 4.6
            )
        )

        val sampleWorkforce = listOf(
            WorkforceRecord(
                id = "WF-001",
                workerId = "EMP-9021",
                workerName = "Ramesh Kumar Soren",
                contractorId = "CONTR-2026-01",
                mineId = "DEMO-MINE-01",
                shift = "Shift A (06:00 - 14:00)",
                attendanceDate = "2026-11-02",
                status = AttendanceStatus.PRESENT,
                checkInTime = "05:48 AM",
                checkOutTime = null,
                supervisorRemarks = "Passed biometric & breathalyzer zero BAC check."
            ),
            WorkforceRecord(
                id = "WF-002",
                workerId = "EMP-9022",
                workerName = "Santosh Murmu",
                contractorId = "CONTR-2026-01",
                mineId = "DEMO-MINE-01",
                shift = "Shift A (06:00 - 14:00)",
                attendanceDate = "2026-11-02",
                status = AttendanceStatus.PRESENT,
                checkInTime = "05:52 AM",
                checkOutTime = null,
                supervisorRemarks = "Assigned Dumper D-18 on South Route."
            ),
            WorkforceRecord(
                id = "WF-003",
                workerId = "EMP-9034",
                workerName = "Babulal Hansda",
                contractorId = "CONTR-2026-02",
                mineId = "DEMO-MINE-01",
                shift = "Shift A (06:00 - 14:00)",
                attendanceDate = "2026-11-02",
                status = AttendanceStatus.ON_LEAVE,
                checkInTime = null,
                checkOutTime = null,
                supervisorRemarks = "Approved statutory compensatory rest."
            )
        )

        val sampleOperational = listOf(
            OperationalReport(
                id = "OPS-2026-4401",
                mineId = "DEMO-MINE-01",
                reportingPeriod = "October 2026 (Monthly)",
                metricName = "Raw Coal Output",
                value = 1450000.0,
                unit = "Tonnes",
                source = "Belt Weigher Telemetry & Silo Rake Dispatch",
                reportingOfficer = "Chief Mining Engineer",
                verificationStatus = ReportVerificationStatus.VERIFIED,
                dateSubmitted = "2026-11-01"
            ),
            OperationalReport(
                id = "OPS-2026-4402",
                mineId = "DEMO-MINE-01",
                reportingPeriod = "October 2026 (Monthly)",
                metricName = "Overburden (OB) Removed",
                value = 4200000.0,
                unit = "Cubic Metres (BCM)",
                source = "Laser Drone Volumetric Pit Scan",
                reportingOfficer = "Survey Officer",
                verificationStatus = ReportVerificationStatus.VERIFIED,
                dateSubmitted = "2026-11-01"
            ),
            OperationalReport(
                id = "OPS-2026-4403",
                mineId = "DEMO-MINE-01",
                reportingPeriod = "Week 44 (Current)",
                metricName = "HEMM Machine Availability",
                value = 86.4,
                unit = "% Available",
                source = "Fleet Management SAP PM Interface",
                reportingOfficer = "Mechanical In-Charge",
                verificationStatus = ReportVerificationStatus.VERIFIED,
                dateSubmitted = "2026-11-02"
            )
        )

        val sampleAlerts = listOf(
            AlertItem(
                id = "ALT-2026-01",
                alertType = AlertType.COMPLIANCE_DEADLINE,
                mineId = "DEMO-MINE-01",
                mineName = "Rajmahal Open Cast Project",
                title = "Overdue: Haul Road Dust Suppression Compliance",
                description = "Statutory requirement DGMS CMR 2017 Reg 104 is past due date (25 Oct 2026). Pump motor replacement pending.",
                priority = IssueSeverity.CRITICAL,
                status = AlertStatus.NEW,
                createdAt = "2026-10-26 09:00:00",
                dueDate = "2026-10-25",
                responsibleOfficer = "Rajesh Sharma",
                escalationLevel = 2
            ),
            AlertItem(
                id = "ALT-2026-02",
                alertType = AlertType.ENVIRONMENTAL_BREACH,
                mineId = "DEMO-MINE-01",
                mineName = "Rajmahal Open Cast Project",
                title = "Air Quality PM10 Exceeded (138.5 µg/m³)",
                description = "Boundary Station #2 reading breached statutory safe limit (100 µg/m³). SPCB notification window active.",
                priority = IssueSeverity.HIGH,
                status = AlertStatus.NEW,
                createdAt = "2026-11-02 08:05:00",
                dueDate = "2026-11-02",
                responsibleOfficer = "Ananya Das",
                escalationLevel = 1
            ),
            AlertItem(
                id = "ALT-2026-03",
                alertType = AlertType.OVERDUE_ACTION,
                mineId = "DEMO-MINE-01",
                mineName = "Rajmahal Open Cast Project",
                title = "Inspection Finding: Haul Road Berm Height",
                description = "DGMS finding on Section 4 safety bund height requires packing to 1.8m before 5th Nov.",
                priority = IssueSeverity.HIGH,
                status = AlertStatus.ACKNOWLEDGED,
                createdAt = "2026-10-24 16:00:00",
                dueDate = "2026-11-05",
                responsibleOfficer = "Er. A. K. Verma",
                escalationLevel = 1
            ),
            AlertItem(
                id = "ALT-2026-04",
                alertType = AlertType.CONTRACTOR_DOC_EXPIRY,
                mineId = "DEMO-MINE-01",
                mineName = "Rajmahal Open Cast Project",
                title = "Contractor Insurance Renewal Pending",
                description = "Trans-Coal Haulage Ltd Workmen Compensation Policy WC-2025/99 expires in 12 days.",
                priority = IssueSeverity.MEDIUM,
                status = AlertStatus.NEW,
                createdAt = "2026-11-01 10:00:00",
                dueDate = "2026-11-14",
                responsibleOfficer = "Contractor Manager",
                escalationLevel = 1
            )
        )

        // Seed initial audit trail with genesis and milestones
        val initialLogs = mutableListOf<AuditLogEntry>()
        var prevHash = AuditCrypto.GENESIS_HASH

        val genesisTime = "2026-10-20 08:00:00"
        val h1 = AuditCrypto.generateEventHash(prevHash, genesisTime, "SYS-ROOT", "SYSTEM", "INITIALIZE", "ROOT-00", "MineGuard 360 AI Platform Initialized for SIH26024")
        initialLogs.add(AuditLogEntry("AUD-1001", genesisTime, "SYS-ROOT", "System Core", "Super Administrator", "INITIALIZE", "SYSTEM", "ROOT-00", "MineGuard 360 AI Platform Initialized for SIH26024", prevHash, h1))
        prevHash = h1

        val t2 = "2026-10-24 16:30:00"
        val h2 = AuditCrypto.generateEventHash(prevHash, t2, "USR-DGMS-01", "INSPECTIONS", "COMPLETE", "INSP-2026-101", "DGMS Inspection completed with 4 checklist points")
        initialLogs.add(AuditLogEntry("AUD-1002", t2, "USR-DGMS-01", "DGMS Dy. Director", "Field Inspector", "COMPLETE", "INSPECTIONS", "INSP-2026-101", "DGMS Inspection completed with 4 checklist points", prevHash, h2))
        prevHash = h2

        val t3 = "2026-11-01 12:00:00"
        val h3 = AuditCrypto.generateEventHash(prevHash, t3, "USR-SIH-2026", "COMPLIANCE_TRACKER", "VERIFY", "CMP-2026-002", "Officially verified Pit Bench Slope Stability Radar Telemetry Log")
        initialLogs.add(AuditLogEntry("AUD-1003", t3, "USR-SIH-2026", "Er. A. K. Verma", "Mine Manager", "VERIFY", "COMPLIANCE_TRACKER", "CMP-2026-002", "Officially verified Pit Bench Slope Stability Radar Telemetry Log", prevHash, h3))
        prevHash = h3

        val t4 = "2026-11-02 08:10:00"
        val h4 = AuditCrypto.generateEventHash(prevHash, t4, "USR-ENV-01", "ENV_MONITORING", "BREACH_TRIGGER", "ENV-2026-001", "Telemetry breach flagged: PM10 reading 138.5 exceeded 100 safe limit")
        initialLogs.add(AuditLogEntry("AUD-1004", t4, "USR-ENV-01", "Ananya Das", "Environmental Officer", "BREACH_TRIGGER", "ENV_MONITORING", "ENV-2026-001", "Telemetry breach flagged: PM10 reading 138.5 exceeded 100 safe limit", prevHash, h4))

        // Reverse to have newest on top
        _auditLogs.value = initialLogs.reversed()
        _mines.value = sampleMines
        _complianceItems.value = sampleCompliance
        _inspections.value = sampleInspections
        _safetyIncidents.value = sampleIncidents
        _environmentalReadings.value = sampleEnvReadings
        _contractors.value = sampleContractors
        _workforceRecords.value = sampleWorkforce
        _operationalReports.value = sampleOperational
        _alerts.value = sampleAlerts
    }
}
