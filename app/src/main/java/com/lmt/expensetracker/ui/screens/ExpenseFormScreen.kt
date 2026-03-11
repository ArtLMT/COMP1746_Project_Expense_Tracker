package com.lmt.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lmt.expensetracker.ui.components.ConfirmationDialog
import com.lmt.expensetracker.ui.components.SuccessDialog
import com.lmt.expensetracker.viewmodel.ExpenseViewModel

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

    var typeExpanded by remember { mutableStateOf(false) }
    var paymentMethodExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }

    val currencies = listOf("USD", "EUR", "GBP", "JPY", "VND")

    // Set project ID on first composition
    remember {
        viewModel.setProjectId(projectId)
        0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = if (formState.isEditMode) "Edit Expense" else "New Expense",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
        }

        // Form Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date
            FormTextFieldComponent(
                value = formState.date,
                onValueChange = { viewModel.onDateChange(it) },
                label = "Date (DD/MM/YYYY) *",
                isError = formState.dateError != null,
                errorMessage = formState.dateError,
                placeholder = "25/12/2024"
            )

            // Amount
            FormTextFieldComponent(
                value = formState.amount,
                onValueChange = { viewModel.onAmountChange(it) },
                label = "Amount *",
                isError = formState.amountError != null,
                errorMessage = formState.amountError,
                placeholder = "1500.50"
            )

            // Currency Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = formState.currency,
                    onValueChange = {},
                    label = { Text("Currency") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { currencyExpanded = !currencyExpanded }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select currency")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )
                DropdownMenu(
                    expanded = currencyExpanded,
                    onDismissRequest = { currencyExpanded = false }
                ) {
                    currencies.forEach { currency ->
                        DropdownMenuItem(
                            text = { Text(currency) },
                            onClick = {
                                viewModel.onCurrencyChange(currency)
                                currencyExpanded = false
                            }
                        )
                    }
                }
            }

            // Expense Type Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = formState.type,
                    onValueChange = {},
                    label = { Text("Expense Type *") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { typeExpanded = !typeExpanded }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select type")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )
                DropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    viewModel.expenseTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                viewModel.onTypeChange(type)
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            // Payment Method Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = formState.paymentMethod,
                    onValueChange = {},
                    label = { Text("Payment Method *") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { paymentMethodExpanded = !paymentMethodExpanded }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select method")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )
                DropdownMenu(
                    expanded = paymentMethodExpanded,
                    onDismissRequest = { paymentMethodExpanded = false }
                ) {
                    viewModel.paymentMethods.forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method) },
                            onClick = {
                                viewModel.onPaymentMethodChange(method)
                                paymentMethodExpanded = false
                            }
                        )
                    }
                }
            }

            // Status Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = formState.status,
                    onValueChange = {},
                    label = { Text("Status") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { statusExpanded = !statusExpanded }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select status")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )
                DropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    viewModel.statuses.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status) },
                            onClick = {
                                viewModel.onStatusChange(status)
                                statusExpanded = false
                            }
                        )
                    }
                }
            }

            // Claimant
            FormTextFieldComponent(
                value = formState.claimant,
                onValueChange = { viewModel.onClaimantChange(it) },
                label = "Claimant *",
                isError = formState.claimantError != null,
                errorMessage = formState.claimantError
            )

            // Description
            FormTextFieldComponent(
                value = formState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = "Description (Optional)",
                maxLines = 3
            )

            // Location
            FormTextFieldComponent(
                value = formState.location,
                onValueChange = { viewModel.onLocationChange(it) },
                label = "Location (Optional)"
            )

            // Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.requestConfirmation() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Save Expense",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                TextButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Box(modifier = Modifier.size(16.dp))
        }
    }

    // Confirmation Dialog
    if (showConfirmDialog) {
        ConfirmationDialog(
            title = "Confirm Expense Details",
            message = """
                Amount: ${formState.currency} ${formState.amount}
                Type: ${formState.type}
                Payment Method: ${formState.paymentMethod}
                Claimant: ${formState.claimant}
                Date: ${formState.date}
                Status: ${formState.status}
                
                Do you want to save this expense?
            """.trimIndent(),
            confirmText = "Save",
            dismissText = "Cancel",
            onConfirm = { viewModel.saveExpense() },
            onDismiss = { viewModel.dismissConfirmDialog() }
        )
    }

    // Success Dialog
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

@Composable
fun FormTextFieldComponent(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    placeholder: String = "",
    maxLines: Int = 1
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            maxLines = maxLines,
            singleLine = maxLines == 1
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
