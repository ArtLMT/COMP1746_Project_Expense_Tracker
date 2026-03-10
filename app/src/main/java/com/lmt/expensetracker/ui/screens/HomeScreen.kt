package com.lmt.expensetracker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lmt.expensetracker.data.database.AppDatabase
import com.lmt.expensetracker.data.repository.ExpenseRepository
import com.lmt.expensetracker.data.repository.ProjectRepository
import com.lmt.expensetracker.viewmodel.ExpenseViewModel
import com.lmt.expensetracker.viewmodel.ProjectViewModel

// ============================================================================
// COLOR PALETTE - EXACT HEX FROM TAILWIND CONFIG
// ============================================================================
object CustomColors {
    val Primary = Color(0xFF19E65E)          // Neon Green
    val BackgroundDark = Color(0xFF112116)   // Dark background
    val SurfaceDark = Color(0xFF1A3222)      // Card/Surface dark
    val MutedText = Color(0xFF999999)        // Muted gray text
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)
    val StatusActive = Color(0xFF19E65E)
    val StatusAtRisk = Color(0xFFEAB308)     // Yellow-500
    val StatusAtRiskBg = Color(0x33EAB308)   // Yellow-500/20
    val StatusNew = Color(0xFF22C55E)        // Green-500
    val StatusNewBg = Color(0x1A22C55E)      // Green-500/10
    val StatusPending = Color(0xFF6B7280)    // Gray-500
    val StatusPendingBg = Color(0x1A6B7280)  // Gray-500/10
}

// ============================================================================
// MAIN HOME SCREEN
// ============================================================================
@Composable
fun HomeScreen(
    database: AppDatabase
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.ProjectList) }
    var selectedProjectId by remember { mutableStateOf<String?>(null) }

    val projectRepository = ProjectRepository(database.appDao())
    val expenseRepository = ExpenseRepository(database.appDao())

    val projectViewModel: ProjectViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ProjectViewModel(projectRepository) as T
            }
        }
    )

    val expenseViewModel: ExpenseViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ExpenseViewModel(expenseRepository) as T
            }
        }
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CustomColors.BackgroundDark),
        containerColor = CustomColors.BackgroundDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    projectViewModel.resetForm()
                    projectViewModel.setEditMode(false)
                    currentScreen = Screen.ProjectForm
                },
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = 48.dp), // Pushes FAB down to overlap bottom bar appropriately
                containerColor = CustomColors.Primary,
                contentColor = CustomColors.BackgroundDark,
                shape = RoundedCornerShape(28.dp) // rounded-full (w-14 h-14)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add Project",
                    modifier = Modifier.size(32.dp),
                    tint = CustomColors.BackgroundDark
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            CustomBottomNavigation(
                onHomeClick = {
                    currentScreen = Screen.ProjectList
                    selectedTab = 0
                },
                onSettingsClick = {
                    currentScreen = Screen.ExpenseList
                    selectedTab = 1
                }
            )
        }
    ) { innerPadding ->
        when (currentScreen) {
            Screen.ProjectList -> {
                DashboardContent(
                    modifier = Modifier.padding(innerPadding),
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    viewModel = projectViewModel,
                    onNavigateToEdit = { projectId ->
                        projectViewModel.loadProjectForEdit(projectId)
                        projectViewModel.setEditMode(true)
                        currentScreen = Screen.ProjectForm
                    },
                    onNavigateToExpenses = { projectId ->
                        selectedProjectId = projectId
                        currentScreen = Screen.ExpenseListForProject
                    }
                )
            }
            Screen.ProjectForm -> {
                ProjectFormScreen(
                    viewModel = projectViewModel,
                    onNavigateBack = {
                        currentScreen = Screen.ProjectList
                        projectViewModel.resetForm()
                    },
                    onSaveSuccess = {
                        currentScreen = Screen.ProjectList
                        projectViewModel.resetForm()
                    }
                )
            }
            Screen.ExpenseList -> {
                ExpenseListScreen(
                    viewModel = expenseViewModel,
                    projectId = null,
                    onNavigateToCreateExpense = {
                        expenseViewModel.resetForm()
                        expenseViewModel.setEditMode(false)
                        currentScreen = Screen.ExpenseForm
                    },
                    onNavigateToEditExpense = { expenseId ->
                        expenseViewModel.loadExpenseForEdit(expenseId)
                        expenseViewModel.setEditMode(true)
                        currentScreen = Screen.ExpenseForm
                    },
                    onNavigateBack = {
                        currentScreen = Screen.ProjectList
                    }
                )
            }
            Screen.ExpenseForm -> {
                ExpenseFormScreen(
                    viewModel = expenseViewModel,
                    projectId = "",
                    onNavigateBack = {
                        currentScreen = Screen.ExpenseList
                        expenseViewModel.resetForm()
                    },
                    onSaveSuccess = {
                        currentScreen = Screen.ExpenseList
                        expenseViewModel.resetForm()
                    }
                )
            }
            Screen.ExpenseListForProject -> {
                selectedProjectId?.let { projectId ->
                    ExpenseListScreen(
                        viewModel = expenseViewModel,
                        projectId = projectId,
                        onNavigateToCreateExpense = {
                            expenseViewModel.setProjectId(projectId)
                            expenseViewModel.resetForm()
                            expenseViewModel.setEditMode(false)
                            currentScreen = Screen.ExpenseFormForProject
                        },
                        onNavigateToEditExpense = { expenseId ->
                            expenseViewModel.loadExpenseForEdit(expenseId)
                            expenseViewModel.setEditMode(true)
                            currentScreen = Screen.ExpenseFormForProject
                        },
                        onNavigateBack = {
                            currentScreen = Screen.ProjectList
                            selectedProjectId = null
                        }
                    )
                }
            }
            Screen.ExpenseFormForProject -> {
                selectedProjectId?.let { projectId ->
                    ExpenseFormScreen(
                        viewModel = expenseViewModel,
                        projectId = projectId,
                        onNavigateBack = {
                            currentScreen = Screen.ExpenseListForProject
                            expenseViewModel.resetForm()
                        },
                        onSaveSuccess = {
                            currentScreen = Screen.ExpenseListForProject
                            expenseViewModel.resetForm()
                        }
                    )
                }
            }
        }
    }
}

// ============================================================================
// DASHBOARD CONTENT - MAIN UI FROM HTML
// ============================================================================
@Composable
private fun DashboardContent(
    modifier: Modifier = Modifier,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    viewModel: ProjectViewModel,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToExpenses: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CustomColors.BackgroundDark)
    ) {
        // Header Section
        HeaderSection(
            selectedTab = selectedTab,
            onTabSelected = { index ->
                onTabSelected(index)
                when(index) {
                    0 -> viewModel.filterByStatus("Active")
                    1 -> viewModel.filterByStatus("Completed")
                    2 -> viewModel.filterByStatus("On Hold")
                }
            }
        )

        // Search Bar
        SearchBarSection(searchQuery = searchQuery, onSearchChange = onSearchChange)

        // Delegate rendering projects to ProjectListScreen!
        ProjectListScreen(
            viewModel = viewModel,
            onNavigateToCreateProject = {}, // handled by FAB in HomeScreen
            onNavigateToEditProject = onNavigateToEdit,
            onNavigateToProjectDetail = onNavigateToExpenses,
            modifier = Modifier.weight(1f)
        )
    }
}

// ============================================================================
// HEADER SECTION
// ============================================================================
@Composable
private fun HeaderSection(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        // Greeting & Notification
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Good morning, Alex",
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Dashboard",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = CustomColors.White
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFF1E293B), // Match Tailwind gray-800
                        shape = RoundedCornerShape(50.dp)
                    )
                    .clickable { }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFFD1D5DB) // Match Tailwind gray-300
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0x0DFFFFFF), // Match white/5
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            listOf("Active" to 12, "Completed" to 8, "On Hold" to 3).forEachIndexed { index, (label, count) ->
                StatusTab(
                    label = label,
                    count = count,
                    isSelected = selectedTab == index,
                    modifier = Modifier.weight(1f),
                    onClick = { onTabSelected(index) }
                )
            }
        }
    }
}

@Composable
fun StatusTab(
    label: String,
    count: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = if (isSelected) CustomColors.Primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 12.sp, // Match text-xs
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp, // tracking-wider
            color = if (isSelected) CustomColors.Black else Color(0xFF9CA3AF) // gray-400
        )
        Text(
            text = count.toString(),
            fontSize = 18.sp, // Match text-lg
            fontWeight = FontWeight.Bold,
            color = if (isSelected) CustomColors.Black else CustomColors.White
        )
    }
}

// ============================================================================
// SEARCH BAR SECTION
// ============================================================================
@Composable
private fun SearchBarSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    color = CustomColors.SurfaceDark, // #1a3222
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp),
                tint = Color(0xFF6B7280) // gray-500
            )

            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .weight(1f),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = CustomColors.White
                ),
                singleLine = true,
                cursorBrush = SolidColor(CustomColors.Primary),
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search projects...",
                            fontSize = 16.sp,
                            color = Color(0xFF9CA3AF) // gray-400
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

// ============================================================================
// PROJECTS VIEW & CARD
// ============================================================================
@Composable
fun ProjectCard(
    projectWithSpent: com.lmt.expensetracker.data.entities.ProjectWithSpent,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCardClick: () -> Unit
) {
    val project = projectWithSpent.project
    val statusColor = when (project.status) {
        "On Track" -> CustomColors.StatusActive
        "At Risk" -> CustomColors.StatusAtRisk
        "New" -> CustomColors.StatusNew
        "Active" -> CustomColors.StatusActive
        "Pending" -> CustomColors.StatusPending
        else -> CustomColors.StatusPending
    }

    val statusBgColor = when (project.status) {
        "On Track" -> CustomColors.StatusActive
        "At Risk" -> CustomColors.StatusAtRiskBg
        "New" -> CustomColors.StatusNewBg
        "Active" -> CustomColors.StatusActive
        "Pending" -> CustomColors.StatusPendingBg
        else -> CustomColors.StatusPendingBg
    }

    val statusTextColor = when (project.status) {
        "On Track" -> CustomColors.Black
        "Active" -> CustomColors.Black
        else -> statusColor
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .background(
                color = CustomColors.SurfaceDark, // bg-[#1a3222]
                shape = RoundedCornerShape(12.dp) // rounded-xl
            )
            .border(
                width = 1.dp,
                color = Color(0xFF1F2937), // border-gray-800
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current
            ) { onCardClick() }
    ) {
        // Card Content
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Title & Status Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = project.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CustomColors.White,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .background(
                            color = statusBgColor,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .then(
                            if (project.status == "At Risk") {
                                Modifier.border(1.dp, Color(0x33EAB308), RoundedCornerShape(4.dp))
                            } else Modifier
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = project.status.uppercase(),
                        fontSize = 10.sp, // text-[10px]
                        fontWeight = FontWeight.Bold,
                        color = statusTextColor
                    )
                }
            }

            // Project Details Grid (2 columns)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Manager
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Manager",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF9CA3AF) // gray-400
                        )
                        Text(
                            text = project.manager, // Show actual manager
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8) // gray-400
                        )
                    }

                    // Budget
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Payments,
                            contentDescription = "Budget",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF9CA3AF) // gray-400
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$${project.budget}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE5E7EB) // slate-200
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Date Range
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = "Date",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF9CA3AF) // gray-400
                        )
                        Text(
                            text = "${project.startDate} - ${project.endDate}", // Use actual dates
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF) // gray-400
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color(0xFF1F1F1F))
            ) {
                val progressFraction = if (project.budget > 0) {
                    (projectWithSpent.spentAmount / project.budget).coerceIn(0.0, 1.0).toFloat()
                } else {
                    0f
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressFraction)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(CustomColors.Primary)
                )
            }
        }
    }
}

// ============================================================================
// CUSTOM BOTTOM NAVIGATION
// ============================================================================
@Composable
private fun CustomBottomNavigation(
    onHomeClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(CustomColors.BackgroundDark)
    ) {
        BottomAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .drawBehind { // Add border top manually
                    val strokeWidth = 1.dp.toPx()
                    val y = 0f
                    drawLine(
                        color = Color(0xFF244730), // border-[#244730]
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                },
            containerColor = Color(0xFF1A3222), // bg-[#1a3222]
            contentPadding = PaddingValues(0.dp),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Button
                NavigationItem(
                    icon = Icons.Outlined.Home,
                    label = "Home",
                    isSelected = true,
                    onClick = onHomeClick
                )

                // Center Spacer for FAB
                Spacer(modifier = Modifier.width(56.dp))

                // Settings Button
                NavigationItem(
                    icon = Icons.Outlined.Settings,
                    label = "Settings",
                    isSelected = false,
                    onClick = onSettingsClick
                )
            }
        }
    }
}

@Composable
private fun NavigationItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .width(64.dp), // w-16
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(28.dp),
            tint = if (isSelected) CustomColors.Primary else Color(0xFF93C8A5) // #93c8a5
        )
        Text(
            text = label.uppercase(),
            fontSize = 10.sp, // text-[10px]
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp, // tracking-tighter
            color = if (isSelected) CustomColors.Primary else Color(0xFF93C8A5)
        )
    }
}

// ============================================================================
// SCREEN NAVIGATION
// ============================================================================
sealed class Screen {
    object ProjectList : Screen()
    object ProjectForm : Screen()
    object ExpenseList : Screen()
    object ExpenseForm : Screen()
    object ExpenseListForProject : Screen()
    object ExpenseFormForProject : Screen()
}
