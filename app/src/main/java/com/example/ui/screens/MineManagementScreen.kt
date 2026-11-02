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
fun MineManagementScreen(
    repository: MineGuardRepository
) {
    val mines by repository.mines.collectAsState()
    val compliance by repository.complianceItems.collectAsState()
    val inspections by repository.inspections.collectAsState()
    val incidents by repository.safetyIncidents.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSubsidiary by remember { mutableStateOf("ALL") }
    var selectedMineDetail by remember { mutableStateOf<Mine?>(null) }
    var showAddMineDialog by remember { mutableStateOf(false) }

    val subsidiaries = listOf("ALL", "ECL", "BCCL", "SECL", "MCL", "CCL")

    val filteredMines = mines.filter { mine ->
        val matchesSubsidiary = if (selectedSubsidiary == "ALL") true else mine.subsidiary.contains(selectedSubsidiary, ignoreCase = true)
        val matchesSearch = mine.name.contains(searchQuery, ignoreCase = true) ||
                mine.location.contains(searchQuery, ignoreCase = true) ||
                mine.managerName.contains(searchQuery, ignoreCase = true)
        matchesSubsidiary && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CloudBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Colliery & Mine Registry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MainText
                )
                Text(
                    text = "${filteredMines.size} of ${mines.size} Coal Mines Displayed (CIL)",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }

            Button(
                onClick = { showAddMineDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_mine_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Colliery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by colliery name, district, or manager...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("mine_search_input"),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite,
                focusedBorderColor = Teal,
                unfocusedBorderColor = BorderColor
            )
        )

        // Subsidiary Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(subsidiaries) { sub ->
                FilterChip(
                    selected = selectedSubsidiary == sub,
                    onClick = { selectedSubsidiary = sub },
                    label = { Text(sub, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LightTeal,
                        selectedLabelColor = DeepNavy
                    ),
                    modifier = Modifier.testTag("filter_sub_${sub.lowercase()}")
                )
            }
        }

        // Mine Cards List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredMines) { mine ->
                val mineCompCount = compliance.count { it.mineId == mine.id }
                val mineOverdueCount = compliance.count { it.mineId == mine.id && it.status == ComplianceStatus.OVERDUE }
                val mineHazards = incidents.count { it.mineId == mine.id && it.correctiveActionStatus != ActionStatus.VERIFIED_CLOSED }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedMineDetail = mine }
                        .testTag("mine_card_${mine.id.lowercase()}"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderColor))
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mine.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MainText
                                )
                                Text(
                                    text = "${mine.subsidiary} • ${mine.location}, ${mine.state}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryText
                                )
                            }
                            StatusBadge(
                                text = mine.operationalStatus.label,
                                containerColor = if (mine.operationalStatus == OperationalStatus.ACTIVE) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                                contentColor = if (mine.operationalStatus == OperationalStatus.ACTIVE) SuccessGreen else WarningAmber
                            )
                        }

                        Divider(color = BorderColor.copy(alpha = 0.6f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Mine Type", fontSize = 11.sp, color = SecondaryText)
                                Text(mine.mineType.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MainText)
                            }
                            Column {
                                Text("Annual Capacity", fontSize = 11.sp, color = SecondaryText)
                                Text("${mine.annualCapacityMTPA} MTPA", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MainText)
                            }
                            Column {
                                Text("Manager", fontSize = 11.sp, color = SecondaryText)
                                Text(mine.managerName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MainText)
                            }
                        }

                        // Colliery Indicators summary
                        Surface(
                            color = CloudBackground,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Compliance: $mineCompCount items", fontSize = 11.sp, color = MainText, fontWeight = FontWeight.Medium)
                                if (mineOverdueCount > 0) {
                                    Text("⚠️ $mineOverdueCount Overdue", fontSize = 11.sp, color = CriticalRed, fontWeight = FontWeight.Bold)
                                }
                                if (mineHazards > 0) {
                                    Text("⚡ $mineHazards Open Hazards", fontSize = 11.sp, color = SafetyOrange, fontWeight = FontWeight.Bold)
                                }
                                Text("View Details →", fontSize = 11.sp, color = Teal, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Detail Bottom Sheet / Dialog
    selectedMineDetail?.let { mine ->
        val mCompliance = compliance.filter { it.mineId == mine.id }
        val mInspections = inspections.filter { it.mineId == mine.id }
        val mIncidents = incidents.filter { it.mineId == mine.id }

        AlertDialog(
            onDismissRequest = { selectedMineDetail = null },
            title = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(mine.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DeepNavy)
                        StatusBadge(
                            text = mine.operationalStatus.label,
                            containerColor = SuccessGreen.copy(alpha = 0.15f),
                            contentColor = SuccessGreen
                        )
                    }
                    Text("${mine.subsidiary} • ${mine.location}, ${mine.state}", fontSize = 12.sp, color = SecondaryText)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Colliery Details & Operational Notes:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(mine.notes, fontSize = 12.sp, color = MainText)

                    Text("Statutory Compliance Summary (${mCompliance.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    mCompliance.take(3).forEach { comp ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(comp.requirementTitle, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            ComplianceStatusChip(status = comp.status)
                        }
                    }

                    Text("Recent Inspections (${mInspections.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    mInspections.take(2).forEach { ins ->
                        Text("• ${ins.category.label} by ${ins.inspectorName} (${ins.status.label})", fontSize = 12.sp, color = SecondaryText)
                    }

                    Text("Open Hazards (${mIncidents.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    mIncidents.take(2).forEach { inc ->
                        Text("• ${inc.description.take(45)}... [${inc.severity.label}]", fontSize = 12.sp, color = if (inc.severity == IssueSeverity.CRITICAL) CriticalRed else SafetyOrange)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedMineDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Add Mine Dialog
    if (showAddMineDialog) {
        var newName by remember { mutableStateOf("") }
        var newSub by remember { mutableStateOf("ECL (Eastern Coalfields)") }
        var newLoc by remember { mutableStateOf("") }
        var newManager by remember { mutableStateOf("") }
        var newCapacity by remember { mutableStateOf("10.0") }

        AlertDialog(
            onDismissRequest = { showAddMineDialog = false },
            title = { Text("Register New Coal Colliery", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Colliery Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("new_mine_name")
                    )
                    OutlinedTextField(
                        value = newLoc,
                        onValueChange = { newLoc = it },
                        label = { Text("District & Location *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newManager,
                        onValueChange = { newManager = it },
                        label = { Text("Designated Colliery Manager *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCapacity,
                        onValueChange = { newCapacity = it },
                        label = { Text("Annual Capacity (MTPA)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            val mine = Mine(
                                id = "DEMO-MINE-${(10..99).random()}",
                                name = newName,
                                subsidiary = newSub,
                                mineType = MineType.OPENCAST,
                                location = newLoc,
                                state = "Jharkhand",
                                latitude = 24.5,
                                longitude = 86.5,
                                operationalStatus = OperationalStatus.ACTIVE,
                                managerName = newManager.ifBlank { "Er. Colliery Manager" },
                                managerContact = "+91 94310 00000",
                                registrationDate = "2026-11-02",
                                annualCapacityMTPA = newCapacity.toDoubleOrNull() ?: 10.0,
                                notes = "Registered for demonstration monitoring."
                            )
                            repository.addMine(mine)
                            showAddMineDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                ) {
                    Text("Register Colliery")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMineDialog = false }) { Text("Cancel") }
            }
        )
    }
}
