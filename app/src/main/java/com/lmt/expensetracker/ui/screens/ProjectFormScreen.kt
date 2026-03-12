package com.lmt.expensetracker.ui.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lmt.expensetracker.ui.components.ConfirmationDialog
import com.lmt.expensetracker.ui.components.SuccessDialog
import com.lmt.expensetracker.viewmodel.ProjectViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==================== SCREEN ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectFormScreen(
    viewModel: ProjectViewModel,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val showConfirmDialog by viewModel.showConfirmDialog.collectAsStateWithLifecycle()
    val saveSuccess by viewModel.saveSuccess.collectAsStateWithLifecycle()

    // ── Focus Requesters ──
    val nameFocus = remember { FocusRequester() }
    val descriptionFocus = remember { FocusRequester() }
    val budgetFocus = remember { FocusRequester() }
    val managerFocus = remember { FocusRequester() }
    val clientFocus = remember { FocusRequester() }
    val specialReqFocus = remember { FocusRequester() }

    // ── Date Picker State ──
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // ── Status Dropdown ──
    var statusExpanded by remember { mutableStateOf(false) }
    val statusOptions = viewModel.statusOptions   // Single Source of Truth

    // ── Scaffold Layout ──
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (formState.isEditMode) "Edit Project" else "New Project"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Button(
                    onClick = { viewModel.requestConfirmation() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Save Project")
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Section: Core Info ──
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FormTextField(
                    value = formState.name,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = "Project Name",
                    isError = formState.nameError != null,
                    errorMessage = formState.nameError,
                    modifier = Modifier.focusRequester(nameFocus),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { descriptionFocus.requestFocus() }
                    )
                )

                FormTextField(
                    value = formState.description,
                    onValueChange = { viewModel.onDescriptionChange(it) },
                    label = "Description",
                    isError = formState.descriptionError != null,
                    errorMessage = formState.descriptionError,
                    maxLines = 3,
                    modifier = Modifier.focusRequester(descriptionFocus),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { budgetFocus.requestFocus() }
                    )
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )

            // ── Section: Timeline & Status ──
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Start Date — read-only, opens DatePicker on click
                    val startDateInteraction = remember { MutableInteractionSource() }
                    LaunchedEffect(startDateInteraction) {
                        startDateInteraction.interactions.collect { interaction ->
                            if (interaction is PressInteraction.Release) {
                                showStartDatePicker = true
                            }
                        }
                    }
                    FormTextField(
                        value = formState.startDate,
                        onValueChange = {},
                        label = "Start Date",
                        placeholder = "YYYY-MM-DD",
                        readOnly = true,
                        isError = formState.startDateError != null,
                        errorMessage = formState.startDateError,
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            Icon(
                                Icons.Outlined.CalendarToday,
                                contentDescription = "Pick start date",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        interactionSource = startDateInteraction
                    )

                    // End Date — read-only, opens DatePicker on click
                    val endDateInteraction = remember { MutableInteractionSource() }
                    LaunchedEffect(endDateInteraction) {
                        endDateInteraction.interactions.collect { interaction ->
                            if (interaction is PressInteraction.Release) {
                                showEndDatePicker = true
                            }
                        }
                    }
                    FormTextField(
                        value = formState.endDate,
                        onValueChange = {},
                        label = "End Date",
                        placeholder = "YYYY-MM-DD",
                        readOnly = true,
                        isError = formState.endDateError != null,
                        errorMessage = formState.endDateError,
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            Icon(
                                Icons.Outlined.CalendarToday,
                                contentDescription = "Pick end date",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        interactionSource = endDateInteraction
                    )
                }

                // Status — ExposedDropdownMenuBox
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it }
                ) {
                    OutlinedTextField(
                        value = formState.status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        statusOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.onStatusChange(option)
                                    statusExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )

            // ── Section: Financials & People ──
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FormTextField(
                    value = formState.budget,
                    onValueChange = { viewModel.onBudgetChange(it) },
                    label = "Budget",
                    isError = formState.budgetError != null,
                    errorMessage = formState.budgetError,
                    placeholder = "0.00",
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.AttachMoney,
                            contentDescription = "Money",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        Text(
                            text = "USD",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    modifier = Modifier.focusRequester(budgetFocus),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { managerFocus.requestFocus() }
                    )
                )

                FormTextField(
                    value = formState.manager,
                    onValueChange = { viewModel.onManagerChange(it) },
                    label = "Project Manager",
                    isError = formState.managerError != null,
                    errorMessage = formState.managerError,
                    trailingIcon = {
                        Icon(
                            Icons.Outlined.PersonAdd,
                            contentDescription = "Add Manager",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.focusRequester(managerFocus),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { clientFocus.requestFocus() }
                    )
                )

                FormTextField(
                    value = formState.clientDepartmentInfo,
                    onValueChange = { viewModel.onClientInfoChange(it) },
                    label = "Client / Department (Optional)",
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Domain,
                            contentDescription = "Department",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.focusRequester(clientFocus),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { specialReqFocus.requestFocus() }
                    )
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )

            // ── Section: Additional Details ──
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FormTextField(
                    value = formState.specialRequirements,
                    onValueChange = { viewModel.onSpecialRequirementsChange(it) },
                    label = "Special Requirements (Optional)",
                    maxLines = 3,
                    modifier = Modifier.focusRequester(specialReqFocus),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    )
                )
            }

            // Bottom spacer so content isn't hidden behind the BottomAppBar
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ── Date Picker Dialogs ──
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.onStartDateChange(formatMillisToDate(millis))
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.onEndDateChange(formatMillisToDate(millis))
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ── Confirmation Dialog ──
    if (showConfirmDialog) {
        ConfirmationDialog(
            title = "Confirm Project Details",
            message = """
                Name: ${formState.name}
                Manager: ${formState.manager}
                Budget: ${'$'}${formState.budget}
                Status: ${formState.status}
                
                Do you want to save this project?
            """.trimIndent(),
            confirmText = "Save",
            dismissText = "Cancel",
            onConfirm = { viewModel.saveProject() },
            onDismiss = { viewModel.dismissConfirmDialog() }
        )
    }

    // ── Success Dialog ──
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

// ==================== HELPER ====================

private fun formatMillisToDate(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(millis))
}

// ==================== FORM TEXT FIELD ====================

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
    readOnly: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(placeholder) }
        } else null,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage) }
        } else null,
        isError = isError,
        singleLine = maxLines == 1,
        maxLines = maxLines,
        readOnly = readOnly,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource ?: remember { MutableInteractionSource() },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error,
            errorSupportingTextColor = MaterialTheme.colorScheme.error
        ),
        shape = MaterialTheme.shapes.small,
        modifier = modifier.fillMaxWidth()
    )
}
