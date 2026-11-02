package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.MineGuardRepository
import com.example.ui.components.*
import com.example.ui.theme.*

data class MapPin(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String, // Mine, Hazard, Sensor, Inspection
    val latitude: Double,
    val longitude: Double,
    val pinColor: Color
)

@Composable
fun GISMineMapScreen(
    repository: MineGuardRepository
) {
    val mines by repository.mines.collectAsState()
    val incidents by repository.safetyIncidents.collectAsState()
    val envReadings by repository.environmentalReadings.collectAsState()
    val isOffline by repository.isOfflineMode.collectAsState()

    var selectedPinCategory by remember { mutableStateOf("ALL") }
    var selectedPin by remember { mutableStateOf<MapPin?>(null) }
    var showFieldReportDialog by remember { mutableStateOf(false) }

    // Map pins collection
    val allPins = remember(mines, incidents, envReadings) {
        val list = mutableListOf<MapPin>()
        mines.forEach { m ->
            list.add(
                MapPin(
                    id = m.id,
                    title = m.name,
                    subtitle = "${m.subsidiary} • ${m.mineType.label}",
                    category = "MINE",
                    latitude = m.latitude,
                    longitude = m.longitude,
                    pinColor = DeepNavy
                )
            )
        }
        incidents.filter { it.latitude != null }.forEach { inc ->
            list.add(
                MapPin(
                    id = inc.id,
                    title = "Hazard: ${inc.category.label}",
                    subtitle = "${inc.description.take(40)}... (${inc.severity.label})",
                    category = "HAZARD",
                    latitude = inc.latitude!!,
                    longitude = inc.longitude!!,
                    pinColor = if (inc.severity == IssueSeverity.CRITICAL) CriticalRed else SafetyOrange
                )
            )
        }
        envReadings.take(2).forEachIndexed { idx, env ->
            list.add(
                MapPin(
                    id = env.id,
                    title = "Station: ${env.measurementType.label}",
                    subtitle = "${env.value} ${env.unit} (${if (env.isBreach) "Breach" else "Nominal"})",
                    category = "SENSOR",
                    latitude = 25.0400 + (idx * 0.005),
                    longitude = 87.3280 + (idx * 0.004),
                    pinColor = if (env.isBreach) CriticalRed else Teal
                )
            )
        }
        list
    }

    val filteredPins = allPins.filter {
        if (selectedPinCategory == "ALL") true else it.category == selectedPinCategory
    }

    val offlineDraftsCount = incidents.count { it.isOfflineDraft }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CloudBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GIS Mine Map & Field Telemetry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MainText
                )
                Text(
                    text = "Geo-spatial overview of collieries, telemetry stations and field hazards",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }

            Button(
                onClick = { showFieldReportDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("geo_field_report_btn")
            ) {
                Icon(Icons.Default.AddLocationAlt, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Geo Report", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
            }
        }

        // Offline Status Bar
        Surface(
            color = if (isOffline) WarningAmber.copy(alpha = 0.15f) else LightTeal,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = if (isOffline) Icons.Default.CloudOff else Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = if (isOffline) WarningAmber else Teal,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isOffline) "Offline Field Mode Active • $offlineDraftsCount Pending Drafts" else "Online Synced • Central Telemetry Connected",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOffline) WarningAmber else DeepNavy
                    )
                }

                if (offlineDraftsCount > 0) {
                    TextButton(
                        onClick = { repository.syncOfflineDrafts() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Sync Now", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Teal)
                    }
                }
            }
        }

        // Filter chips for pins
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("ALL", "MINE", "HAZARD", "SENSOR").forEach { cat ->
                FilterChip(
                    selected = selectedPinCategory == cat,
                    onClick = { selectedPinCategory = cat },
                    label = { Text(cat, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LightTeal,
                        selectedLabelColor = DeepNavy
                    ),
                    modifier = Modifier.testTag("filter_map_${cat.lowercase()}")
                )
            }
        }

        // Interactive Visual Map Canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EFF3)),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderColor))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(filteredPins) {
                            detectTapGestures { offset ->
                                // Hit detection against pins
                                val tapped = filteredPins.firstOrNull { pin ->
                                    val latRel = ((pin.latitude - 20.0) / 6.0).toFloat().coerceIn(0.1f, 0.9f)
                                    val lngRel = ((pin.longitude - 82.0) / 6.0).toFloat().coerceIn(0.1f, 0.9f)
                                    val px = lngRel * size.width
                                    val py = (1f - latRel) * size.height
                                    val dist = kotlin.math.hypot(offset.x - px, offset.y - py)
                                    dist < 40f
                                }
                                selectedPin = tapped
                            }
                        }
                ) {
                    // Draw map coordinate grid lines
                    val w = size.width
                    val h = size.height

                    // Subtle background topographical lines
                    for (i in 1..4) {
                        val y = h * (i / 5f)
                        drawLine(
                            color = Color(0xFFD2DFE6),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                        val x = w * (i / 5f)
                        drawLine(
                            color = Color(0xFFD2DFE6),
                            start = Offset(x, 0f),
                            end = Offset(x, h),
                            strokeWidth = 1f
                        )
                    }

                    // Coalfields outline contours
                    drawCircle(color = DeepNavy.copy(alpha = 0.05f), radius = 100f, center = Offset(w * 0.75f, h * 0.35f)) // Rajmahal / Jharia
                    drawCircle(color = DeepNavy.copy(alpha = 0.05f), radius = 80f, center = Offset(w * 0.35f, h * 0.65f)) // Korba Gevra
                    drawCircle(color = DeepNavy.copy(alpha = 0.05f), radius = 70f, center = Offset(w * 0.65f, h * 0.80f)) // Talcher

                    // Draw Pins
                    filteredPins.forEach { pin ->
                        val latRel = ((pin.latitude - 20.0) / 6.0).toFloat().coerceIn(0.1f, 0.9f)
                        val lngRel = ((pin.longitude - 82.0) / 6.0).toFloat().coerceIn(0.1f, 0.9f)
                        val px = lngRel * w
                        val py = (1f - latRel) * h

                        // Glow / radar wave for hazards
                        if (pin.category == "HAZARD") {
                            drawCircle(color = pin.pinColor.copy(alpha = 0.3f), radius = 18f, center = Offset(px, py))
                        }

                        // Pin Base
                        drawCircle(color = pin.pinColor, radius = 9f, center = Offset(px, py))
                        drawCircle(color = Color.White, radius = 4f, center = Offset(px, py))
                    }
                }

                // Map Overlay Info Legend
                Surface(
                    color = Color.White.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Text("Indian Coalfields GIS Grid", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                        Text("Jharkhand • Odisha • Chhattisgarh", fontSize = 9.sp, color = SecondaryText)
                    }
                }

                // Selected Pin Popup Card
                selectedPin?.let { pin ->
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(10.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pin.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DeepNavy)
                                Text(pin.subtitle, fontSize = 11.sp, color = SecondaryText, maxLines = 1)
                                Text("Lat: ${"%.4f".format(pin.latitude)}, Lng: ${"%.4f".format(pin.longitude)}", fontSize = 10.sp, color = Slate)
                            }
                            IconButton(onClick = { selectedPin = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Pins List Table
        Text("Geo-Tagged Observation Points (${filteredPins.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MainText)

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredPins) { p ->
                Surface(
                    color = SurfaceWhite,
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderColor)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPin = p }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(p.pinColor)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DeepNavy)
                            Text(p.subtitle, fontSize = 11.sp, color = SecondaryText, maxLines = 1)
                        }
                        Text("${"%.2f".format(p.latitude)}, ${"%.2f".format(p.longitude)}", fontSize = 10.sp, color = Slate)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Field Report Dialog with Offline Draft option
    if (showFieldReportDialog) {
        var reportDesc by remember { mutableStateOf("") }
        var reportLoc by remember { mutableStateOf("Rajmahal Haul Road East") }
        var reportLat by remember { mutableStateOf("25.0412") }
        var reportLng by remember { mutableStateOf("87.3298") }
        var reportSeverity by remember { mutableStateOf(IssueSeverity.MEDIUM) }

        AlertDialog(
            onDismissRequest = { showFieldReportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.GpsFixed, contentDescription = null, tint = SafetyOrange)
                    Text("Geo-Tagged Field Observation", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Location Privacy: Coordinates are collected strictly for colliery safety mapping and hazard localization.",
                        fontSize = 11.sp,
                        color = SecondaryText
                    )

                    OutlinedTextField(
                        value = reportDesc,
                        onValueChange = { reportDesc = it },
                        label = { Text("Field Observation *") },
                        placeholder = { Text("Describe pit wall condition, equipment, or dust plume...") },
                        modifier = Modifier.fillMaxWidth().testTag("field_obs_desc")
                    )

                    OutlinedTextField(
                        value = reportLoc,
                        onValueChange = { reportLoc = it },
                        label = { Text("Colliery Bench / Location Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = reportLat,
                            onValueChange = { reportLat = it },
                            label = { Text("Latitude") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = reportLng,
                            onValueChange = { reportLng = it },
                            label = { Text("Longitude") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Severity Level", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IssueSeverity.values().forEach { sev ->
                            FilterChip(
                                selected = reportSeverity == sev,
                                onClick = { reportSeverity = sev },
                                label = { Text(sev.label, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reportDesc.isNotBlank()) {
                            val incident = SafetyIncident(
                                id = "INC-${System.currentTimeMillis() % 10000}",
                                mineId = mines.firstOrNull()?.id ?: "DEMO-MINE-01",
                                location = reportLoc,
                                category = IncidentCategory.UNSAFE_CONDITION,
                                description = reportDesc,
                                reportedDate = "2026-11-02",
                                reportedTime = "10:15 AM",
                                reporterName = "Field Surveyor",
                                severity = reportSeverity,
                                immediateActionTaken = "Recorded via GPS Field Reporter",
                                assignedOfficer = "Colliery Safety Manager",
                                correctiveActionStatus = ActionStatus.OPEN,
                                closureNotes = "",
                                latitude = reportLat.toDoubleOrNull() ?: 25.0412,
                                longitude = reportLng.toDoubleOrNull() ?: 87.3298,
                                isOfflineDraft = isOffline
                            )
                            repository.reportSafetyIncident(incident)
                            showFieldReportDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isOffline) WarningAmber else DeepNavy)
                ) {
                    Text(if (isOffline) "Save Offline Draft" else "Submit Observation")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFieldReportDialog = false }) { Text("Cancel") }
            }
        )
    }
}
