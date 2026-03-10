package com.lmt.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lmt.expensetracker.ui.components.ConfirmationDialog
import com.lmt.expensetracker.ui.components.SuccessDialog
import com.lmt.expensetracker.viewmodel.ProjectViewModel

@Composable
fun ProjectFormScreen(
    viewModel: ProjectViewModel,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val showConfirmDialog by viewModel.showConfirmDialog.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    var statusExpanded by remember { mutableStateOf(false) }

    val statusOptions = listOf("Active", "Completed", "On Hold", "On Track", "At Risk", "New", "Pending")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CustomColors.BackgroundDark)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CustomColors.BackgroundDark)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = CustomColors.White,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onNavigateBack() }
            )
            Text(
                text = if (formState.isEditMode) "Edit Project" else "New Project",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = CustomColors.White
            )
            Text(
                text = "Save",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = CustomColors.Primary,
                modifier = Modifier.clickable { viewModel.requestConfirmation() }
            )
        }
        
        HorizontalDivider(color = Color(0xFF1F2937), thickness = 1.dp)

        // Form Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section: Core Info
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FormTextField(
                    value = formState.name,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = "PROJECT NAME *",
                    isError = formState.nameError != null,
                    errorMessage = formState.nameError
                )

                FormTextField(
                    value = formState.description,
                    onValueChange = { viewModel.onDescriptionChange(it) },
                    label = "DESCRIPTION *",
                    isError = formState.descriptionError != null,
                    errorMessage = formState.descriptionError,
                    maxLines = 3
                )
            }

            HorizontalDivider(color = Color(0x80346544), thickness = 1.dp)

            // Section: Timeline & Status
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FormTextField(
                        value = formState.startDate,
                        onValueChange = { viewModel.onStartDateChange(it) },
                        label = "START DATE *",
                        modifier = Modifier.weight(1f),
                        isError = formState.startDateError != null,
                        errorMessage = formState.startDateError,
                        placeholder = "YYYY-MM-DD"
                    )

                    FormTextField(
                        value = formState.endDate,
                        onValueChange = { viewModel.onEndDateChange(it) },
                        label = "END DATE *",
                        modifier = Modifier.weight(1f),
                        isError = formState.endDateError != null,
                        errorMessage = formState.endDateError,
                        placeholder = "YYYY-MM-DD"
                    )
                }

                // Status Dropdown Customization
                Column {
                    Text(
                        text = "STATUS *",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF93C8A5),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(
                                    color = Color(0xFF1A3222),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF346544),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { statusExpanded = !statusExpanded }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formState.status,
                                color = CustomColors.White,
                                fontSize = 16.sp
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Select status",
                                tint = Color(0xFF93C8A5)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false },
                            modifier = Modifier.background(CustomColors.SurfaceDark)
                        ) {
                            statusOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, color = CustomColors.White) },
                                    onClick = {
                                        viewModel.onStatusChange(option)
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            HorizontalDivider(color = Color(0x80346544), thickness = 1.dp)

            // Section: Financials & People
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FormTextField(
                    value = formState.budget,
                    onValueChange = { viewModel.onBudgetChange(it) },
                    label = "BUDGET *",
                    isError = formState.budgetError != null,
                    errorMessage = formState.budgetError,
                    placeholder = "0.00",
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.AttachMoney,
                            contentDescription = "Money",
                            tint = Color(0xFF93C8A5),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingContent = {
                        Text(
                            text = "USD",
                            color = Color(0xFF93C8A5),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                )

                FormTextField(
                    value = formState.manager,
                    onValueChange = { viewModel.onManagerChange(it) },
                    label = "PROJECT MANAGER *",
                    isError = formState.managerError != null,
                    errorMessage = formState.managerError,
                    trailingContent = {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF112116), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.PersonAdd,
                                contentDescription = "Add Manager",
                                tint = CustomColors.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                )
                
                FormTextField(
                    value = formState.clientDepartmentInfo,
                    onValueChange = { viewModel.onClientInfoChange(it) },
                    label = "CLIENT / DEPARTMENT (OPTIONAL)",
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Domain,
                            contentDescription = "Department",
                            tint = Color(0xFF93C8A5),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }

            HorizontalDivider(color = Color(0x80346544), thickness = 1.dp)

            // Section: Additional Details
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FormTextField(
                    value = formState.specialRequirements,
                    onValueChange = { viewModel.onSpecialRequirementsChange(it) },
                    label = "SPECIAL REQUIREMENTS (OPTIONAL)",
                    maxLines = 3
                )
            }

            Box(modifier = Modifier.size(32.dp))
        }
    }

    // Confirmation Dialog
    if (showConfirmDialog) {
        ConfirmationDialog(
            title = "Confirm Project Details",
            message = """
                Name: ${formState.name}
                Manager: ${formState.manager}
                Budget: $${formState.budget}
                Status: ${formState.status}
                
                Do you want to save this project?
            """.trimIndent(),
            confirmText = "Save",
            dismissText = "Cancel",
            onConfirm = { viewModel.saveProject() },
            onDismiss = { viewModel.dismissConfirmDialog() }
        )
    }

    // Success Dialog
    if (saveSuccess) {
        SuccessDialog(
            title = "Project Saved",
            message = "Your project has been saved successfully!",
            onDismiss = {
                viewModel.resetSaveSuccess()
                onSaveSuccess()
            }
        )
    }
}

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String? = null,
    maxLines: Int = 1,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF93C8A5), // text-[#93c8a5]
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (maxLines > 1) 120.dp else 56.dp)
                .background(
                    color = Color(0xFF1A3222), // bg-[#1a3222]
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (isError) MaterialTheme.colorScheme.error else Color(0xFF346544), // border-[#346544]
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = if (maxLines > 1) 16.dp else 0.dp),
            verticalAlignment = if (maxLines > 1) Alignment.Top else Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Box(modifier = Modifier.padding(end = 12.dp)) {
                    leadingIcon()
                }
            }
            
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color(0xFF93C8A5).copy(alpha = 0.5f), // text-[#93c8a5] placeholder
                        fontSize = 16.sp
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    maxLines = maxLines,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(CustomColors.Primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (trailingContent != null) {
                Box(modifier = Modifier.padding(start = 12.dp)) {
                    trailingContent()
                }
            }
        }
        
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}
