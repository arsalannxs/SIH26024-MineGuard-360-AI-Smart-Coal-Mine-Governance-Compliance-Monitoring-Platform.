package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.MineGuardRepository
import com.example.ui.components.*
import com.example.ui.navigation.NavSection
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineGuardApp(
    repository: MineGuardRepository
) {
    var currentSection by remember { mutableStateOf(NavSection.OVERVIEW) }
    var currentDemoStepIndex by remember { mutableStateOf(0) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentUser by repository.currentUser.collectAsState()
    val mines by repository.mines.collectAsState()
    val alerts by repository.alerts.collectAsState()
    val isOffline by repository.isOfflineMode.collectAsState()

    // Dialog states
    var showRoleSwitcherDialog by remember { mutableStateOf(false) }
    var showDemoWalkthroughDialog by remember { mutableStateOf(false) }
    var showAddComplianceDialog by remember { mutableStateOf(false) }
    var showScheduleInspectionDialog by remember { mutableStateOf(false) }
    var showReportSafetyDialog by remember { mutableStateOf(false) }
    var showAddEnvDialog by remember { mutableStateOf(false) }

    // Android Back Handler to navigate back to Overview if on another screen
    BackHandler(enabled = currentSection != NavSection.OVERVIEW) {
        currentSection = NavSection.OVERVIEW
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DeepNavy,
                modifier = Modifier.width(310.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Drawer Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SafetyOrange,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(24.dp))
                            }
                        }
                        Column {
                            Text("MineGuard 360 AI", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Text("SIH26024 • Ministry of Coal", fontSize = 11.sp, color = BorderColor)
                        }
                    }

                    Surface(
                        color = NavyCardSurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(currentUser.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(currentUser.role.displayName, fontSize = 10.sp, color = SafetyOrange)
                            }
                            IconButton(
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    showRoleSwitcherDialog = true
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = "Role", tint = LightTeal, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    HorizontalDivider(color = Slate.copy(alpha = 0.5f))

                    // 15 Navigation Items
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(NavSection.values()) { section ->
                            val isSelected = currentSection == section
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        currentSection = section
                                        scope.launch { drawerState.close() }
                                    }
                                    .testTag("nav_item_${section.name.lowercase()}"),
                                color = if (isSelected) SafetyOrange else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = section.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) DeepNavy else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = section.title,
                                        color = if (isSelected) DeepNavy else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // 3-Min Demo trigger at drawer bottom
                    Button(
                        onClick = {
                            scope.launch { drawerState.close() }
                            showDemoWalkthroughDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                        modifier = Modifier.fillMaxWidth().testTag("drawer_demo_btn")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("3-Minute SIH Demo", color = DeepNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "MineGuard 360",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (isOffline) {
                                    Surface(color = WarningAmber, shape = RoundedCornerShape(4.dp)) {
                                        Text("OFFLINE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DeepNavy, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Text(
                                text = currentSection.title,
                                fontSize = 11.sp,
                                color = LightTeal
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("drawer_menu_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        // Role chip in TopBar
                        Surface(
                            color = NavyCardSurface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showRoleSwitcherDialog = true }
                                .padding(end = 4.dp)
                                .testTag("topbar_role_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = SafetyOrange, modifier = Modifier.size(14.dp))
                                Text(
                                    text = currentUser.role.name.take(6),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Alerts notification badge
                        IconButton(
                            onClick = { currentSection = NavSection.ALERTS },
                            modifier = Modifier.testTag("topbar_alerts_btn")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (alerts.isNotEmpty()) {
                                        Badge(containerColor = CriticalRed) {
                                            Text("${alerts.size}")
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DeepNavy,
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                // Bottom navigation bar with 5 key mobile access tabs
                NavigationBar(
                    containerColor = SurfaceWhite,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    val mobileTabs = listOf(
                        NavSection.OVERVIEW,
                        NavSection.COMPLIANCE,
                        NavSection.SAFETY,
                        NavSection.AI_RISK,
                        NavSection.GIS_MAP
                    )

                    mobileTabs.forEach { tab ->
                        val isSelected = currentSection == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentSection = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) SafetyOrange else Slate
                                )
                            },
                            label = {
                                Text(
                                    text = when (tab) {
                                        NavSection.OVERVIEW -> "Home"
                                        NavSection.COMPLIANCE -> "Statutory"
                                        NavSection.SAFETY -> "Safety"
                                        NavSection.AI_RISK -> "AI Risk"
                                        NavSection.GIS_MAP -> "GIS Map"
                                        else -> tab.title
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) DeepNavy else SecondaryText
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = LightTeal
                            ),
                            modifier = Modifier.testTag("bottom_nav_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentSection) {
                    NavSection.OVERVIEW -> OverviewScreen(
                        repository = repository,
                        onNavigate = { currentSection = it },
                        onOpenAddCompliance = { showAddComplianceDialog = true },
                        onOpenScheduleInspection = { showScheduleInspectionDialog = true },
                        onOpenReportSafety = { showReportSafetyDialog = true },
                        onOpenAddEnv = { showAddEnvDialog = true },
                        onOpenDemoWalkthrough = { showDemoWalkthroughDialog = true }
                    )
                    NavSection.MINE_MGMT -> MineManagementScreen(repository = repository)
                    NavSection.COMPLIANCE -> ComplianceTrackerScreen(
                        repository = repository,
                        onOpenAddCompliance = { showAddComplianceDialog = true }
                    )
                    NavSection.INSPECTIONS -> InspectionsScreen(
                        repository = repository,
                        onOpenScheduleInspection = { showScheduleInspectionDialog = true }
                    )
                    NavSection.SAFETY -> SafetyIncidentsScreen(
                        repository = repository,
                        onOpenReportSafety = { showReportSafetyDialog = true }
                    )
                    NavSection.ENVIRONMENTAL -> EnvironmentalScreen(
                        repository = repository,
                        onOpenAddEnv = { showAddEnvDialog = true }
                    )
                    NavSection.CONTRACTORS -> ContractorScreen(repository = repository)
                    NavSection.WORKFORCE -> WorkforceScreen(repository = repository)
                    NavSection.OPERATIONAL -> OperationalReportsScreen(repository = repository)
                    NavSection.AI_RISK -> AIRiskScreen(repository = repository)
                    NavSection.ALERTS -> AlertsScreen(repository = repository)
                    NavSection.GIS_MAP -> GISMineMapScreen(repository = repository)
                    NavSection.AUDIT_TRAIL -> AuditTrailScreen(repository = repository)
                    NavSection.REPORTS -> ReportsExportScreen(repository = repository)
                    NavSection.SETTINGS -> SettingsScreen(
                        repository = repository,
                        onOpenRoleSwitcher = { showRoleSwitcherDialog = true },
                        onOpenDemoWalkthrough = { showDemoWalkthroughDialog = true }
                    )
                }
            }
        }
    }

    // Role Switcher Dialog
    if (showRoleSwitcherDialog) {
        RoleSwitcherDialog(
            currentRole = currentUser.role,
            onRoleSelected = {
                repository.setCurrentRole(it)
                showRoleSwitcherDialog = false
            },
            onDismiss = { showRoleSwitcherDialog = false }
        )
    }

    // 3-Minute SIH Guided Demo Walkthrough
    if (showDemoWalkthroughDialog) {
        SIHDemoWalkthroughDialog(
            currentStepIndex = currentDemoStepIndex,
            onNavigateToStep = { targetSection, stepIdx ->
                currentSection = targetSection
                currentDemoStepIndex = stepIdx
            },
            onDismiss = { showDemoWalkthroughDialog = false },
            onResetData = { repository.resetDemoData() }
        )
    }

    // Add Compliance Dialog
    if (showAddComplianceDialog) {
        AddComplianceDialog(
            mines = mines,
            onAdd = { repository.addComplianceItem(it) },
            onDismiss = { showAddComplianceDialog = false }
        )
    }

    // Schedule Inspection Dialog
    if (showScheduleInspectionDialog) {
        ScheduleInspectionDialog(
            mines = mines,
            onSchedule = { repository.scheduleInspection(it) },
            onDismiss = { showScheduleInspectionDialog = false }
        )
    }

    // Report Safety Dialog
    if (showReportSafetyDialog) {
        ReportSafetyDialog(
            mines = mines,
            onReport = { repository.reportSafetyIncident(it) },
            onDismiss = { showReportSafetyDialog = false }
        )
    }

    // Add Environmental Dialog
    if (showAddEnvDialog) {
        AddEnvironmentalReadingDialog(
            mines = mines,
            onAdd = { repository.addEnvironmentalReading(it) },
            onDismiss = { showAddEnvDialog = false }
        )
    }
}
