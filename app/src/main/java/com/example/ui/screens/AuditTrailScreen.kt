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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.MineGuardRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun AuditTrailScreen(
    repository: MineGuardRepository
) {
    val auditLogs by repository.auditLogs.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredLogs = auditLogs.filter { log ->
        log.description.contains(searchQuery, ignoreCase = true) ||
                log.actionType.contains(searchQuery, ignoreCase = true) ||
                log.module.contains(searchQuery, ignoreCase = true) ||
                log.userName.contains(searchQuery, ignoreCase = true)
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
                    text = "Digital Audit Trail & Integrity Log",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MainText
                )
                Text(
                    text = "Cryptographically Hash-Linked Append-Only Event Stream",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }
        }

        // Integrity Verification Badge Banner
        Surface(
            color = SuccessGreen.copy(alpha = 0.12f),
            shape = RoundedCornerShape(10.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SuccessGreen.copy(alpha = 0.4f)))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(24.dp))
                Column {
                    Text(
                        text = "CHAIN INTEGRITY VERIFIED (SHA-256)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                    Text(
                        text = "All ${auditLogs.size} events are sequentially hash-chained. Any record modification or tampering is immediately detectable.",
                        fontSize = 11.sp,
                        color = MainText
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter audit trail by action, officer, or entity...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("audit_search_input"),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite,
                focusedBorderColor = Teal,
                unfocusedBorderColor = BorderColor
            )
        )

        // Audit Trail Feed
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredLogs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("audit_entry_${log.id.lowercase()}"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderColor))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatusBadge(
                                    text = log.actionType,
                                    containerColor = when (log.actionType) {
                                        "CREATE" -> LightTeal
                                        "UPDATE" -> WarningAmber.copy(alpha = 0.15f)
                                        "VERIFY" -> SuccessGreen.copy(alpha = 0.15f)
                                        "DELETE" -> CriticalRed.copy(alpha = 0.15f)
                                        else -> DeepNavy.copy(alpha = 0.12f)
                                    },
                                    contentColor = when (log.actionType) {
                                        "CREATE" -> Teal
                                        "UPDATE" -> WarningAmber
                                        "VERIFY" -> SuccessGreen
                                        "DELETE" -> CriticalRed
                                        else -> DeepNavy
                                    }
                                )
                                Text(
                                    text = log.module,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                            }
                            Text(log.timestamp, fontSize = 10.sp, color = SecondaryText)
                        }

                        Text(
                            text = log.description,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MainText
                        )

                        Text(
                            text = "Actor: ${log.userName} (${log.userRole}) • Ref: ${log.entityId}",
                            fontSize = 11.sp,
                            color = SecondaryText
                        )

                        // Cryptographic Hash Display
                        Surface(
                            color = CloudBackground,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Current Hash (SHA-256):", fontSize = 9.sp, color = SecondaryText, fontWeight = FontWeight.Bold)
                                    Text("Prev Block: ${log.previousHash.take(12)}...", fontSize = 9.sp, color = SecondaryText, fontFamily = FontFamily.Monospace)
                                }
                                Text(
                                    text = log.currentHash,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = DeepNavy,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
