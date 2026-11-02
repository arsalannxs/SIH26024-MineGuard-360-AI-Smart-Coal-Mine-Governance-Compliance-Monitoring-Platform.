package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavSection(
    val title: String,
    val icon: ImageVector,
    val description: String
) {
    OVERVIEW("Overview", Icons.Default.Dashboard, "Key governance KPIs and mine metrics"),
    MINE_MGMT("Mine Management", Icons.Default.Terrain, "Colliery registry and subsidiary overview"),
    COMPLIANCE("Compliance Tracker", Icons.Default.FactCheck, "Statutory compliance and DGMS standards"),
    INSPECTIONS("Inspections", Icons.Default.FindInPage, "Checklists, scheduling, and audit findings"),
    SAFETY("Safety & Incidents", Icons.Default.HealthAndSafety, "Hazards, near-misses, and corrective actions"),
    ENVIRONMENTAL("Environmental", Icons.Default.Eco, "Air quality, water, noise, and telemetry"),
    CONTRACTORS("Contractor Mgmt", Icons.Default.Engineering, "Contractor registry, docs, and compliance"),
    WORKFORCE("Workforce & Attendance", Icons.Default.Groups, "Shifts, biometric tracking, and daily rosters"),
    OPERATIONAL("Operational Reports", Icons.Default.BarChart, "Coal output, overburden, and equipment uptime"),
    AI_RISK("AI Risk Intelligence", Icons.Default.Psychology, "Pattern detection and natural language query"),
    ALERTS("Alerts & Escalations", Icons.Default.Notifications, "Priority alert triage and notification engine"),
    GIS_MAP("GIS Mine Map", Icons.Default.Map, "Interactive mine map & geo-tagged field reporting"),
    AUDIT_TRAIL("Audit Trail", Icons.Default.Shield, "Append-only SHA-256 tamper-evident log"),
    REPORTS("Reports & Exports", Icons.Default.Description, "Statutory report generator & CSV exports"),
    SETTINGS("Settings", Icons.Default.Settings, "Role switcher, 3-min demo & system controls")
}
