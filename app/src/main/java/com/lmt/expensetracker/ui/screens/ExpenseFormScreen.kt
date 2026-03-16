package com.lmt.expensetracker.ui.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lmt.expensetracker.ui.components.ConfirmationDialog
import com.lmt.expensetracker.ui.components.SuccessDialog
import com.lmt.expensetracker.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==================== SCREEN ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseFormScreen(
    viewModel: ExpenseViewModel,
    projectId: String,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val showConfirmDialog by viewModel.showConfirmDialog.collectAsStateWithLifecycle()
    val saveSuccess by viewModel.saveSuccess.collectAsStateWithLifecycle()

    // ── Set project ID on first composition ──
    remember {
        viewModel.setProjectId(projectId)
        0
    }

    // ── Focus Requesters ──
    val amountFocus = remember { FocusRequester() }
    val claimantFocus = remember { FocusRequester() }
    val descriptionFocus = remember { FocusRequester() }
    val locationFocus = remember { FocusRequester() }

    // ── Date Picker State ──
    var showDatePicker by remember { mutableStateOf(false) }

    // ── Dropdown States ──
    var currencyExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var paymentMethodExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    // ── Single Source of Truth from ViewModel ──
    val currencies = viewModel.currencies
    val expenseTypes = viewModel.expenseTypes
    val paymentMethods = viewModel.paymentMethods
    val statuses = viewModel.statuses

    // ── Scaffold Layout ──
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (formState.isEditMode) "Edit Expense" else "New Expense"
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
                    Text("Save Expense")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Project (locked / read-only) ──
            FormTextField(
                value = projectId,
                onValueChange = {},
                label = "Project",
                readOnly = true,
                enabled = false
            )

            // ── Date — read-only, opens DatePicker on click ──
            val dateInteraction = remember { MutableInteractionSource() }
            LaunchedEffect(dateInteraction) {
                dateInteraction.interactions.collect { interaction ->
                    if (interaction is PressInteraction.Release) {
                        showDatePicker = true
                    }
                }
            }
            FormTextField(
                value = formatIsoToDisplay(formState.date),
                onValueChange = {},
                label = "Date",
                placeholder = "DD-MM-YYYY",
                readOnly = true,
                isError = formState.dateError != null,
                errorMessage = formState.dateError,
                trailingIcon = {
                    Icon(
                        Icons.Outlined.CalendarToday,
                        contentDescription = "Pick date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                interactionSource = dateInteraction
            )

            // ── Amount ──
            FormTextField(
                value = formState.amount,
                onValueChange = { viewModel.onAmountChange(it) },
                label = "Amount",
                isError = formState.amountError != null,
                errorMessage = formState.amountError,
                placeholder = "1500.50",
                modifier = Modifier.focusRequester(amountFocus),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { claimantFocus.requestFocus() }
                )
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )

            // ── Currency — ExposedDropdownMenuBox ──
            ExposedDropdownMenuBox(
                expanded = currencyExpanded,
                onExpandedChange = { currencyExpanded = it }
            ) {
                OutlinedTextField(
                    value = formState.currency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Currency") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded)
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
                    expanded = currencyExpanded,
                    onDismissRequest = { currencyExpanded = false }
                ) {
                    currencies.forEach { currency ->
                        DropdownMenuItem(
                            text = { Text(currency) },
                            onClick = {
                                viewModel.onCurrencyChange(currency)
                                currencyExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            // ── Expense Type — ExposedDropdownMenuBox ──
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it }
            ) {
                OutlinedTextField(
                    value = formState.type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Expense Type") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
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
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    expenseTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                viewModel.onTypeChange(type)
                                typeExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            // ── Payment Method — ExposedDropdownMenuBox ──
            ExposedDropdownMenuBox(
                expanded = paymentMethodExpanded,
                onExpandedChange = { paymentMethodExpanded = it }
            ) {
                OutlinedTextField(
                    value = formState.paymentMethod,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Payment Method") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentMethodExpanded)
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
                    expanded = paymentMethodExpanded,
                    onDismissRequest = { paymentMethodExpanded = false }
                ) {
                    paymentMethods.forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method) },
                            onClick = {
                                viewModel.onPaymentMethodChange(method)
                                paymentMethodExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            // ── Status — ExposedDropdownMenuBox ──
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
                    statuses.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status) },
                            onClick = {
                                viewModel.onStatusChange(status)
                                statusExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )

            // ── Claimant ──
            FormTextField(
                value = formState.claimant,
                onValueChange = { viewModel.onClaimantChange(it) },
                label = "Claimant",
                isError = formState.claimantError != null,
                errorMessage = formState.claimantError,
                modifier = Modifier.focusRequester(claimantFocus),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { descriptionFocus.requestFocus() }
                )
            )

            // ── Description ──
            FormTextField(
                value = formState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = "Description (Optional)",
                maxLines = 3,
                modifier = Modifier.focusRequester(descriptionFocus),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { locationFocus.requestFocus() }
                )
            )

            // ── Location ──
            FormTextField(
                value = formState.location,
                onValueChange = { viewModel.onLocationChange(it) },
                label = "Location (Optional)",
                modifier = Modifier.focusRequester(locationFocus),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )

            // Bottom spacer so content isn't hidden behind the BottomAppBar
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ── Date Picker Dialog ──
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.onDateChange(formatMillisToIso(millis))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ── Confirmation Dialog ──
    if (showConfirmDialog) {
        ConfirmationDialog(
            title = "Confirm Expense Details",
            confirmText = "Save",
            dismissText = "Cancel",
            onConfirm = { viewModel.saveExpense() },
            onDismiss = { viewModel.dismissConfirmDialog() },
            messageContent = {
                val labelStyle = SpanStyle(fontWeight = FontWeight.Bold)
                Text(
                    text = buildAnnotatedString {
                        withStyle(labelStyle) { append("Amount: ") }
                        append("${formState.currency} ${formState.amount}\n")
                        withStyle(labelStyle) { append("Type: ") }
                        append("${formState.type}\n")
                        withStyle(labelStyle) { append("Payment Method: ") }
                        append("${formState.paymentMethod}\n")
                        withStyle(labelStyle) { append("Claimant: ") }
                        val safeClaimant = if (formState.claimant.length > 50) formState.claimant.take(50) + "..." else formState.claimant
                        append("$safeClaimant\n")
                        withStyle(labelStyle) { append("Description: ") }
                        val safeDesc = if (formState.description.length > 100) formState.description.take(100) + "..." else formState.description
                        append("$safeDesc\n")
                        withStyle(labelStyle) { append("Date: ") }
                        append("${formatIsoToDisplay(formState.date)}\n")
                        withStyle(labelStyle) { append("Status: ") }
                        append("${formState.status}\n\n")
                        append("Do you want to save this expense?")
                    }
                )
            }
        )
    }

    // ── Success Dialog ──
    if (saveSuccess) {
        SuccessDialog(
            title = "Expense Saved",
            message = "Your expense has been saved successfully!",
            onDismiss = {
                viewModel.resetSaveSuccess()
                onSaveSuccess()
            }
        )
    }
}

// ==================== HELPERS ====================

/** Convert epoch millis to ISO date string (yyyy-MM-dd). */
private fun formatMillisToIso(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(millis))
}

/** Convert ISO date (yyyy-MM-dd) to display format (dd-MM-yyyy). Returns raw value on error. */
private fun formatIsoToDisplay(isoDate: String): String {
    if (isoDate.isBlank()) return ""
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = input.parse(isoDate)
        if (date != null) output.format(date) else isoDate
    } catch (_: Exception) {
        isoDate
    }
}
