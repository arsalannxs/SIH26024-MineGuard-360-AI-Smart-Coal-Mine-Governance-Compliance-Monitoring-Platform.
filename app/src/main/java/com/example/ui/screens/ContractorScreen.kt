package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.MineGuardRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ContractorScreen(
    repository: MineGuardRepository
) {
    val contractors by repository.contractors.collectAsState()
    val mines by repository.mines.collectAsState()

    var showRegisterDialog by remember { mutableStateOf(false) }

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
                    text = "Contractor Governance & Statutory Filing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MainText
                )
                Text(
                    text = "${contractors.size} Mining Contractors • Verification & Document Tracking",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }

            Button(
                onClick = { showRegisterDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("register_contractor_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Register", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Contractors List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(contractors) { c ->
                val mineName = mines.find { it.id == c.mineId }?.name ?: c.mineId

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contractor_card_${c.id.lowercase()}"),
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
                                    text = c.companyName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MainText
                                )
                                Text(
                                    text = "Ref: ${c.contractRef} • $mineName",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryText
                                )
                            }
                            StatusBadge(
                                text = c.complianceStatus.label,
                                containerColor = when (c.complianceStatus) {
                                    ContractorComplianceStatus.VERIFIED_COMPLIANT -> SuccessGreen.copy(alpha = 0.15f)
                                    ContractorComplianceStatus.UNDER_AUDIT -> Teal.copy(alpha = 0.15f)
                                    ContractorComplianceStatus.DOCS_PENDING -> WarningAmber.copy(alpha = 0.15f)
                                    ContractorComplianceStatus.NON_COMPLIANT -> CriticalRed.copy(alpha = 0.15f)
                                },
                                contentColor = when (c.complianceStatus) {
                                    ContractorComplianceStatus.VERIFIED_COMPLIANT -> SuccessGreen
                                    ContractorComplianceStatus.UNDER_AUDIT -> Teal
                                    ContractorComplianceStatus.DOCS_PENDING -> WarningAmber
                                    ContractorComplianceStatus.NON_COMPLIANT -> CriticalRed
                                }
                            )
                        }

                        Text("Work Scope: ${c.workCategory}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DeepNavy)

                        Divider(color = BorderColor.copy(alpha = 0.5f))

                        // Details Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Contract Period", fontSize = 11.sp, color = SecondaryText)
                                Text("${c.startDate} to ${c.endDate}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MainText)
                            }
                            Column {
                                Text("Workforce", fontSize = 11.sp, color = SecondaryText)
                                Text("${c.workforceCount} Persons", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MainText)
                            }
                            Column {
                                Text("Safety Rating", fontSize = 11.sp, color = SecondaryText)
                                Text("★ ${c.performanceRating} / 5.0", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SafetyOrange)
                            }
                        }

                        // Document upload progress
                        Surface(
                            color = CloudBackground,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.FolderShared, contentDescription = null, tint = Teal, modifier = Modifier.size(16.dp))
                                    Text("Statutory Docs: ${c.requiredDocsUploaded} / ${c.totalRequiredDocs} Verified", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MainText)
                                }
                                if (c.requiredDocsUploaded < c.totalRequiredDocs) {
                                    Text("⚠️ Missing Docs", fontSize = 11.sp, color = WarningAmber, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Register Dialog
    if (showRegisterDialog) {
        var compName by remember { mutableStateOf("") }
        var contractRef by remember { mutableStateOf("CIL/ECL/NIT-2026/089") }
        var workCat by remember { mutableStateOf("Overburden Haulage & Earthmoving") }
        var workforce by remember { mutableStateOf("150") }

        AlertDialog(
            onDismissRequest = { showRegisterDialog = false },
            title = { Text("Register Mining Contractor", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = compName,
                        onValueChange = { compName = it },
                        label = { Text("Company / Agency Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("contractor_name_input")
                    )
                    OutlinedTextField(
                        value = contractRef,
                        onValueChange = { contractRef = it },
                        label = { Text("Contract NIT Reference *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = workCat,
                        onValueChange = { workCat = it },
                        label = { Text("Work Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = workforce,
                        onValueChange = { workforce = it },
                        label = { Text("Registered Workforce Count") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (compName.isNotBlank()) {
                            val newC = Contractor(
                                id = "CONTR-${System.currentTimeMillis() % 10000}",
                                companyName = compName,
                                contractRef = contractRef,
                                mineId = mines.firstOrNull()?.id ?: "DEMO-MINE-01",
                                workCategory = workCat,
                                startDate = "2026-11-01",
                                endDate = "2028-10-31",
                                managerName = "Operations In-charge",
                                workforceCount = workforce.toIntOrNull() ?: 100,
                                requiredDocsUploaded = 8,
                                totalRequiredDocs = 10,
                                complianceStatus = ContractorComplianceStatus.UNDER_AUDIT,
                                performanceRating = 4.0
                            )
                            repository.registerContractor(newC)
                            showRegisterDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                ) {
                    Text("Register Contractor")
                }
            },
            dismissButton = { TextButton(onClick = { showRegisterDialog = false }) { Text("Cancel") } }
        )
    }
}
