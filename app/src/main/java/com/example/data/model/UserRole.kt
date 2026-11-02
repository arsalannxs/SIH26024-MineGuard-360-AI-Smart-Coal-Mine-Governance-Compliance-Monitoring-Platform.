package com.example.data.model

enum class UserRole(val displayName: String, val description: String) {
    SUPER_ADMIN("Super Administrator", "Full system governance and audit control"),
    CORPORATE_MGMT("Corporate Management", "CIL Headquarters executive visibility"),
    SUBSIDIARY_ADMIN("Subsidiary Admin", "Subsidiary-level operational control (BCCL/ECL/SECL)"),
    MINE_MANAGER("Mine Manager", "Colliery operational and statutory authority"),
    SAFETY_OFFICER("Safety Officer", "DGMS safety inspections and incident management"),
    ENVIRONMENTAL_OFFICER("Environmental Officer", "Pollution control and land restoration monitoring"),
    INSPECTOR("Field Inspector", "Statutory audit and site checklist enforcement"),
    CONTRACTOR("Contractor Manager", "Workforce, machinery and contractual compliance"),
    READ_ONLY_VIEWER("Read-only Viewer", "Authorised regulatory / observer access")
}

data class CurrentUser(
    val id: String = "USR-SIH-001",
    val name: String = "Rajesh Sharma",
    val role: UserRole = UserRole.MINE_MANAGER,
    val subsidiary: String = "ECL (Eastern Coalfields)",
    val assignedMineId: String = "MINE-ECL-01"
)
