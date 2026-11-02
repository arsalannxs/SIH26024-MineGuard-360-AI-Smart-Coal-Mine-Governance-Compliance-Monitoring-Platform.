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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.MineGuardRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ComplianceTrackerScreen(
    repository: MineGuardRepository,
    onOpenAddCompliance: () -> Unit
) {
    val complianceItems by repository.complianceItems.collectAsState()
    val mines by repository.mines.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var itemToVerify by remember { mutableStateOf<ComplianceItem?>(null) }
    var verificationNotes by remember { mutableStateOf("Verified compliance evidence and telemetry logs on site.") }

    val statusFilters = listOf("ALL", "OVERDUE", "ACTION_REQUIRED", "PENDING", "COMPLIANT")

    val filteredItems = complianceItems.filter { item ->
        val matchesStatus = when (selectedStatusFilter) {
            "ALL" -> true
            "OVERDUE" -> item.status == ComplianceStatus.OVERDUE
            "ACTION_REQUIRED" -> item.status == ComplianceStatus.ACTION_REQUIRED
            "PENDING" -> item.status == ComplianceStatus.PENDING || item.status == ComplianceStatus.UNDER_REVIEW
            "COMPLIANT" -> item.status == ComplianceStatus.COMPLIANT
            else -> true
        }
        val matchesSearch = item.requirementTitle.contains(searchQuery, ignoreCase = true) ||
                item.applicableRegulation.contains(searchQuery, ignoreCase = true) ||
                item.responsibleOfficer.contains(searchQuery, ignoreCase = true)
        matchesStatus && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CloudBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Statutory Compliance Register",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MainText
                )
                Text(
                    text = "${filteredItems.size} Requirements • DGMS & MOEFCC Mandates",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }

            Button(
                onClick = onOpenAddCompliance,
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_compliance_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Record", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by regulation (e.g., CMR 2017), title, or officer...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("compliance_search_input"),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite,
                focusedBorderColor = Teal,
                unfocusedBorderColor = BorderColor
            )
        )

        // Status Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(statusFilters) { status ->
                val label = when (status) {
                    "ALL" -> "All (${complianceItems.size})"
                    "OVERDUE" -> "Overdue (${complianceItems.count { it.status == ComplianceStatus.OVERDUE }})"
                    "ACTION_REQUIRED" -> "Action Needed (${complianceItems.count { it.status == ComplianceStatus.ACTION_REQUIRED }})"
                    "PENDING" -> "Pending (${complianceItems.count { it.status == ComplianceStatus.PENDING || it.status == ComplianceStatus.UNDER_REVIEW }})"
                    "COMPLIANT" -> "Compliant (${complianceItems.count { it.status == ComplianceStatus.COMPLIANT }})"
                    else -> status
                }
                FilterChip(
                    selected = selectedStatusFilter == status,
                    onClick = { selectedStatusFilter = status },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (status == "OVERDUE") CriticalRed.copy(alpha = 0.15f) else LightTeal,
                        selectedLabelColor = if (status == "OVERDUE") CriticalRed else DeepNavy
                    ),
                    modifier = Modifier.testTag("filter_status_${status.lowercase()}")
                )
            }
        }

        // Compliance List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredItems) { item ->
                val mineName = mines.find { it.id == item.mineId }?.name ?: item.mineId

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("compliance_card_${item.id.lowercase()}"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (item.status == ComplianceStatus.OVERDUE) CriticalRed.copy(alpha = 0.5f) else BorderColor
                        )
                    )
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
                            Text(
                                text = mineName,
                                fontSize = 12.sp,
                                color = SecondaryText,
                                fontWeight = FontWeight.Medium
                            )
                            ComplianceStatusChip(status = item.status)
                        }

                        Text(
                            text = item.requirementTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MainText
                        )

                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText,
                            lineHeight = 16.sp
                        )

                        // Regulation Badge
                        Surface(
                            color = CloudBackground,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Gavel, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(14.dp))
                                Text(
                                    text = item.applicableRegulation,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DeepNavy
                                )
                            }
                        }

                        Divider(color = BorderColor.copy(alpha = 0.5f))

                        // Metadata Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Due Date", fontSize = 11.sp, color = SecondaryText)
                                Text(
                                    item.dueDate,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.status == ComplianceStatus.OVERDUE) CriticalRed else MainText
                                )
                            }
                            Column {
                                Text("Responsible Officer", fontSize = 11.sp, color = SecondaryText)
                                Text(item.responsibleOfficer, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MainText)
                            }
                            Column {
                                Text("Review Freq", fontSize = 11.sp, color = SecondaryText)
                                Text(item.reviewFrequency, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MainText)
                            }
                        }

                        // Evidence and Remarks Note
                        if (item.remarks.isNotBlank()) {
                            Text(
                                text = "Remarks: ${item.remarks}",
                                fontSize = 11.sp,
                                color = if (item.status == ComplianceStatus.OVERDUE) CriticalRed else Slate,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Verification / Action Footer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (item.isVerified) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                    Text("Verified by ${item.verifiedBy ?: "Mine Manager"}", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Medium)
                                }
                            } else {
                                Text("Evidence: Required on site", fontSize = 11.sp, color = WarningAmber, fontWeight = FontWeight.Medium)
                            }

                            if (!item.isVerified) {
                                OutlinedButton(
                                    onClick = { itemToVerify = item },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Teal),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp).testTag("verify_btn_${item.id.lowercase()}")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Verify & Close", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Verify Evidence Dialog
    itemToVerify?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToVerify = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Teal)
                    Text("Verify Statutory Compliance", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = item.requirementTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DeepNavy
                    )
                    Text(
                        text = "Regulation: ${item.applicableRegulation}",
                        fontSize = 12.sp,
                        color = SecondaryText
                    )
                    Text(
                        text = "As ${currentUser.role.displayName}, record that physical proof or telemetry verification was validated for this requirement.",
                        fontSize = 12.sp,
                        color = MainText
                    )

                    OutlinedTextField(
                        value = verificationNotes,
                        onValueChange = { verificationNotes = it },
                        label = { Text("Official Inspection & Verification Remarks *") },
                        modifier = Modifier.fillMaxWidth().testTag("verification_notes_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.verifyComplianceEvidence(item.id, verificationNotes)
                        itemToVerify = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.testTag("confirm_verify_button")
                ) {
                    Text("Certify Compliant")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToVerify = null }) { Text("Cancel") }
            }
        )
    }
}
