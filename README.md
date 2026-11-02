# MineGuard 360 AI — Smart Coal Mine Governance & Compliance Monitoring Platform

> **"Safer Mines. Smarter Governance. Complete Accountability."**

[![Smart India Hackathon](https://img.shields.io/badge/SIH-2024%2F2026-orange.svg)](https://www.sih.gov.in/)
[![Problem Statement](https://img.shields.io/badge/Problem%20ID-SIH26024-blue.svg)](https://www.sih.gov.in/)
[![Ministry of Coal](https://img.shields.io/badge/Ministry-Ministry%20of%20Coal-navy.svg)](https://coal.nic.in/)
[![Department](https://img.shields.io/badge/Department-Coal%20India%20Limited-green.svg)](https://www.coalindia.in/)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Jetpack%20Compose-teal.svg)](https://developer.android.com/jetpack/compose)

---

## 1. Project Title & Tagline

**MineGuard 360 AI** is an enterprise-grade digital governance, safety inspection, environmental telemetry, and statutory compliance platform engineered specifically for coal mining operations under the **Ministry of Coal** and **Coal India Limited (CIL)**.

*Tagline:* **"Safer Mines. Smarter Governance. Complete Accountability."**

---

## 2. Official SIH Problem Statement Details

- **Problem Statement ID:** SIH26024
- **Title:** AI-Based Smart Governance and Compliance Monitoring System for Coal Mines
- **Organization:** Ministry of Coal
- **Department:** Coal India Limited (CIL)
- **Category:** Software
- **Theme:** Smart Automation

---

## 3. Problem Overview

Coal mining operations span hundreds of open cast and underground collieries across eight operating subsidiaries (ECL, BCCL, CCL, WCL, SECL, MCL, NCL, NEC) employing thousands of statutory officials and contract workers. 

Historically, governance activities—including statutory Directorate General of Mines Safety (DGMS) compliance, environmental air/water telemetry, safety inspections, hazard observations, contractor documentation, and shift attendance—have been fragmented across physical paper logbooks, disparate spreadsheets, and siloed administrative desks.

### Core Challenges Identified:
- **Delayed Compliance Reporting:** Statutory deadlines (DGMS CMR 2017, MOEFCC Environment Clearance clauses, SPCB consents) frequently lapse without early warning.
- **Inconsistent Records:** Disconnected local paper registers lead to conflicting audits during regulatory inspections.
- **Poor Multi-Mine Visibility:** Headquarters and Subsidiary GMs lack real-time visibility into colliery-level hazard velocity and equipment downtime.
- **Delayed Corrective Actions:** Field observations take weeks to convert into assigned, verifiable mitigation tasks.
- **Limited Transparency & Tamper Risks:** Absence of cryptographically verifiable audit trails allows disputes over when and by whom actions were certified.

---

## 4. Proposed Solution

MineGuard 360 AI resolves these challenges through a unified mobile-first platform featuring:
1. **Statutory Compliance Tracker:** Automated deadline tracking for DGMS CMR 2017 regulations, MOEFCC norms, and labour statutes with an authorized evidence-verification workflow.
2. **Mine Inspection Management:** Digital checklists, automated severity triage, photo/memo linking, and corrective action lifecycles.
3. **Safety Observation & Hazard Reporting:** Instant reporting of unsafe working conditions, near misses, and SOP violations with prominent emergency disclaimers.
4. **Environmental Telemetry:** Continuous boundary station monitoring for PM10, PM2.5, Effluent pH, Ambient Noise, and Water Usage with automated threshold breach alerts.
5. **Contractor Management:** Contractor registry, contract NIT tracking, document expiry reminders, and safety performance scoring.
6. **Workforce Attendance:** Shift rosters (Shift A/B/C) with biometric and alcohol breathalyzer verification logging.
7. **Operational Extraction Reports:** Coal output (Tonnes), Overburden removed (BCM), and HEMM machine availability percentages.
8. **AI Risk Intelligence Engine:** Deterministic pattern recognition, composite colliery risk indices (0–100), and a natural-language question answering system grounded in stored records with exact evidence citations.
9. **Automated Alert & Escalation Engine:** Multi-tier notification hub (Acknowledge, Escalate to GM/DGMS, Resolve).
10. **GIS Interactive Mine Map:** Spatial overview of coal basins across Jharkhand, Odisha, and Chhattisgarh with geo-tagged field observations and offline draft synchronization.
11. **Digital Audit Trail:** Cryptographic SHA-256 hash-chaining across all actions ensuring tamper detection.
12. **Statutory Reports & CSV Exports:** One-click dossier generation and CSV data downloads.

---

## 5. Key Features Matrix

| Feature Module | Core Functionality | Primary Users |
| :--- | :--- | :--- |
| **Overview Dashboard** | Executive governance KPIs, compliance progress bar, active alerts, quick field actions | All Roles |
| **Mine Registry** | Multi-subsidiary colliery records, operational status, capacity, and colliery details | Subsidiary Admins, Corporate |
| **Compliance Tracker** | DGMS & MOEFCC requirement register, due dates, officer assignment, evidence verification | Compliance Officers, Managers |
| **Inspection Module** | Schedule audits, execute multi-point checklists, record findings, and assign corrective actions | DGMS & Internal Inspectors |
| **Safety & Hazards** | Rapid hazard reporting, severity escalation, immediate mitigation notes, and resolution loop | Safety Officers, Supervisors |
| **Environmental Cell** | Continuous sensor telemetry (PM10, PM2.5, pH, Noise), statutory limit breach flags | Environmental Officers |
| **Contractor Module** | Statutory NIT tracking, document uploads, workforce counts, performance ratings | Contractor Managers, HR |
| **Workforce & Shifts** | Shift rosters, biometric check-in, alcohol breathalyzer safety confirmation | Shift In-charges |
| **Operational Reports** | Raw coal extraction, overburden removed, HEMM uptime percentage | Mining Engineers, GMs |
| **AI Risk Intelligence**| Composite Risk Index (0-100), NLP query assistant with transparent evidence linking | Management, Safety Directorate |
| **Alerts & Escalation** | Automated alerts for overdue compliance, breaches, and critical hazards | Designated Officers |
| **GIS Mine Map** | Interactive coal basin map, geo-tagged markers, offline draft queueing | Field Surveyors, Engineers |
| **Digital Audit Trail** | Cryptographically chained SHA-256 event stream with tamper detection | Super Admins, Auditors |
| **Reports & Exports** | Statutory dossier preview and real CSV export generation | Regulatory Viewers, GMs |

---

## 6. User Roles and Role-Based Access Control (RBAC)

The application enforces fine-grained permissions across nine standard mining governance personas:

1. **Super Administrator:** System-wide governance, user provisioning, database resets, and tamper audit validation.
2. **Corporate Management (CIL HQ):** Executive oversight across all eight subsidiaries, aggregate production, and safety indices.
3. **Subsidiary Administrator (ECL/BCCL/SECL/MCL/CCL):** Subsidiary-level colliery management, contract approvals, and resource allocation.
4. **Mine Manager:** Colliery statutory authority, compliance certification, and final corrective action closure.
5. **Safety Officer:** DGMS inspection coordination, daily hazard mitigation, and PPE compliance.
6. **Environmental Officer:** SPCB telemetry compliance, CAAQMS station calibration, and effluent discharge monitoring.
7. **Field Inspector:** On-site statutory checklist execution and directive issuance.
8. **Contractor Manager:** Agency document submission, driver vocational training, and equipment fitness tracking.
9. **Read-only Viewer:** Observer access for external regulators, auditors, and evaluators.

*Demo Role Switcher:* A built-in role switcher allows evaluators to simulate and verify permissions for each persona on device.

---

## 7. System Architecture Diagram

```
+-----------------------------------------------------------------------------------+
|                           MineGuard 360 Android Client                            |
|                            (Jetpack Compose + Material 3)                         |
+-----------------------------------------+-----------------------------------------+
                                          |
                      +-------------------+-------------------+
                      |                                       |
                      v                                       v
         +-------------------------+             +-------------------------+
         |     UI & Navigation     |             |    Offline Field Mode   |
         |  - 15 Modules (Drawer)  |             |  - Local Draft Queue    |
         |  - Top & Bottom Nav     |             |  - Sync Reconciliation  |
         +------------+------------+             +------------+------------+
                      |                                       |
                      +-------------------+-------------------+
                                          |
                                          v
                      +---------------------------------------+
                      |       MineGuard Repository Layer      |
                      |   - StateFlow Reactive Streams        |
                      |   - Entity Mutation & Validation      |
                      +-------------------+-------------------+
                                          |
        +---------------------------------+---------------------------------+
        |                                 |                                 |
        v                                 v                                 v
+---------------+                 +---------------+                 +---------------+
| AI Risk Engine|                 | Crypto Audit  |                 | Data Registry |
| - NLP Queries |                 | - SHA-256     |                 | - Mines & Ops |
| - Risk Scores |                 | - Hash-Chain  |                 | - Compliance  |
| - Evidence    |                 | - Tamper Log  |                 | - Telemetry   |
+---------------+                 +---------------+                 +---------------+
```

---

## 8. Technology Stack

- **Platform:** Native Android (Min SDK 24, Target SDK 36)
- **Language:** Kotlin 2.2.10 (100% Coroutines & StateFlow)
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Architecture:** MVVM (Model-View-ViewModel) + Reactive Repository Pattern
- **Persistence:** Local Structured Storage + StateFlow Reactive Cache with Room compatibility
- **Cryptographic Engine:** Java Security `MessageDigest` (SHA-256 sequential hash-chaining)
- **GIS Mapping:** Custom Compose Canvas vector map with coordinate projection
- **Data Export:** RFC 4180-compliant CSV generation engine
- **Testing:** JUnit4 + Kotlinx Coroutines Test + AndroidX Test Suite

---

## 9. Real Project Folder Structure

```
MineGuard-360/
├── app/
│   ├── build.gradle.kts
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── ai/
│   │   │   │   │   └── AIRiskEngine.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Entities.kt
│   │   │   │   │   │   └── UserRole.kt
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── MineGuardRepository.kt
│   │   │   │   │   └── util/
│   │   │   │   │       └── AuditCrypto.kt
│   │   │   │   └── ui/
│   │   │   │       ├── MineGuardApp.kt
│   │   │   │       ├── components/
│   │   │   │       │   ├── ActionDialogs.kt
│   │   │   │       │   ├── CommonComponents.kt
│   │   │   │       │   └── SIHDemoWalkthrough.kt
│   │   │   │       ├── navigation/
│   │   │   │       │   └── NavSection.kt
│   │   │   │       ├── screens/
│   │   │   │       │   ├── AIRiskScreen.kt
│   │   │   │       │   ├── AlertsScreen.kt
│   │   │   │       │   ├── AuditTrailScreen.kt
│   │   │   │       │   ├── ComplianceTrackerScreen.kt
│   │   │   │       │   ├── ContractorScreen.kt
│   │   │   │       │   ├── EnvironmentalScreen.kt
│   │   │   │       │   ├── GISMineMapScreen.kt
│   │   │   │       │   ├── InspectionsScreen.kt
│   │   │   │       │   ├── MineManagementScreen.kt
│   │   │   │       │   ├── OperationalReportsScreen.kt
│   │   │   │       │   ├── OverviewScreen.kt
│   │   │   │       │   ├── ReportsExportScreen.kt
│   │   │   │       │   ├── SafetyIncidentsScreen.kt
│   │   │   │       │   ├── SettingsScreen.kt
│   │   │   │       │   └── WorkforceScreen.kt
│   │   │   │       └── theme/
│   │   │   │           ├── Color.kt
│   │   │   │           ├── Theme.kt
│   │   │   │           └── Type.kt
│   │   │   └── res/
│   │   │       ├── drawable/
│   │   │       │   ├── ic_launcher_background.xml
│   │   │       │   ├── ic_launcher_foreground.xml
│   │   │       │   └── ic_mineguard_logo.png
│   │   │       ├── mipmap-*/
│   │   │       └── values/
│   │   │           └── strings.xml
│   │   └── test/java/com/example/
│   │       └── ExampleUnitTest.kt
├── build.gradle.kts
├── settings.gradle.kts
├── metadata.json
└── README.md
```

---

## 10. Data Model & Schema Overview

- **Mine:** `id`, `name`, `subsidiary`, `mineType`, `location`, `state`, `latitude`, `longitude`, `operationalStatus`, `managerName`, `annualCapacityMTPA`
- **ComplianceItem:** `id`, `mineId`, `category`, `requirementTitle`, `applicableRegulation`, `responsibleOfficer`, `dueDate`, `status`, `evidenceDocumentUrl`, `isVerified`, `verifiedBy`
- **Inspection:** `id`, `mineId`, `category`, `inspectorName`, `scheduledDate`, `actualDate`, `status`, `checklist`, `observations`, `severity`, `correctiveAction`
- **SafetyIncident:** `id`, `mineId`, `location`, `category`, `description`, `reportedDate`, `severity`, `immediateActionTaken`, `assignedOfficer`, `correctiveActionStatus`, `latitude`, `longitude`, `isOfflineDraft`
- **EnvironmentalReading:** `id`, `mineId`, `measurementType`, `value`, `unit`, `thresholdMax`, `isBreach`, `measurementDate`, `location`, `responsiblePerson`
- **Contractor:** `id`, `companyName`, `contractRef`, `mineId`, `workCategory`, `workforceCount`, `requiredDocsUploaded`, `totalRequiredDocs`, `complianceStatus`, `performanceRating`
- **AuditLogEntry:** `id`, `timestamp`, `userId`, `userName`, `userRole`, `actionType`, `module`, `entityId`, `description`, `previousHash`, `currentHash`

---

## 11. Three-Minute SIH Demo Walkthrough

The application contains a built-in interactive **3-Minute Demo Guide** (accessible via the top banner or Settings screen) that navigates evaluators through 14 sequential evaluation steps:

1. **Overview Dashboard:** View high-level KPIs, compliance progress bar, and active alerts.
2. **Mine Registry:** Filter by "Rajmahal Open Cast Project" (ECL) to inspect colliery data.
3. **Compliance Register:** Open the statutory register categorized by DGMS and MOEFCC.
4. **Overdue Compliance Action:** Examine `Continuous Dust Suppression along Haul Roads` (CMR 2017 Reg 104) flagged as overdue.
5. **Schedule Inspection:** Open the scheduling form with statutory multi-point checklists.
6. **Submit Safety Observation:** Log a high-severity boulder spillage observation with immediate action notes.
7. **Environmental Monitoring:** Observe an ambient PM10 telemetry breach (138.5 µg/m³ vs 100 µg/m³ limit).
8. **GIS Mine Map:** Explore geo-tagged pins across Indian coal basins and test offline draft saving.
9. **AI Risk Intelligence:** View composite risk scoring (0–100) across all monitored mines.
10. **Ask AI Query:** Execute: *"Which mines have highest number of overdue compliance actions?"*
11. **Verify Evidence:** Inspect deterministic evidence citations directly linking record `CMP-2026-001`.
12. **Assign Corrective Action:** Update hazard status from *Open* to *In Progress* / *Resolved*.
13. **Generate Compliance Report:** Preview statutory audit summary and download the generated CSV.
14. **Digital Audit Trail:** Inspect the tamper-evident SHA-256 hash-chain validating all recorded actions.

---

## 12. Security, Privacy & Compliance Rules

- **Zero Mock Claims:** All sample data is explicitly marked with `(Demonstration Prototype)` identifiers. No real confidential Coal India files or employee PII are stored.
- **Safety Disclaimers:** Submitting digital observations does not replace emergency sirens, statutory mine evacuation procedures, or DGMS mandatory reporting.
- **Location Privacy:** GPS coordinates are collected solely for safety mapping and hazard localization with full manual fallback.
- **Tamper Evidence:** Every record modification appends a cryptographically linked SHA-256 hash event. Modifying any prior block breaks the chain.

---

## 13. Running the Project & Verification

### Build & Compilation:
To compile the debug APK:
```bash
gradle :app:assembleDebug
```

### Running Unit Tests:
To execute unit tests covering repository operations, audit crypto, and the AI risk engine:
```bash
gradle :app:testDebugUnitTest
```

---

## 14. License & Attribution

Developed for the **Smart India Hackathon (SIH26024)** in collaboration with the **Ministry of Coal** and **Coal India Limited (CIL)**. Built with Jetpack Compose & Material 3.
