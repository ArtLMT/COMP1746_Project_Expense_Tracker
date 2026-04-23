package com.lmt.expensetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lmt.expensetracker.data.entities.ExpenseEntity
import com.lmt.expensetracker.data.repository.ExpenseRepository
import com.lmt.expensetracker.data.repository.ProjectRepository
import com.lmt.expensetracker.utils.DateUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

data class ExpenseFormState(
    val expenseId: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val date: String = "",
    val amount: String = "",
    val currency: String = "USD",
    val type: String = "Travel",
    val paymentMethod: String = "Cash",
    val claimant: String = "",
    val status: String = "Pending",
    val description: String = "",
    val location: String = "",
    val isEditMode: Boolean = false,
    // Validation errors
    val dateError: String? = null,
    val amountError: String? = null,
    val claimantError: String? = null,
    val typeError: String? = null,
    val paymentMethodError: String? = null
)

data class ExpenseStatusCounts(
    val pending: Int = 0,
    val paid: Int = 0,
    val reimbursed: Int = 0
)

data class ExpenseListState(
    val expenses: List<ExpenseEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val filterStatus: String? = null,
    val filterType: String? = null,
    val selectedProjectId: String? = null,
    val totalAmount: Double = 0.0,
    val projectBudget: Double? = null,
    val statusCounts: ExpenseStatusCounts = ExpenseStatusCounts()
)

class ExpenseViewModel(
    application: Application,
    private val repository: ExpenseRepository,
    private val projectRepository: ProjectRepository
) : AndroidViewModel(application) {

    private val _formState = MutableStateFlow(ExpenseFormState())
    val formState: StateFlow<ExpenseFormState> = _formState.asStateFlow()

    private val _listState = MutableStateFlow(ExpenseListState())
    val listState: StateFlow<ExpenseListState> = _listState.asStateFlow()

    private val _showConfirmDialog = MutableStateFlow(false)
    val showConfirmDialog: StateFlow<Boolean> = _showConfirmDialog.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    // Dropdowns data
    val expenseTypes = listOf(
        "Travel",
        "Equipment",
        "Materials",
        "Services",
        "Software/Licenses",
        "Labour costs",
        "Utilities",
        "Miscellaneous"
    )

    val paymentMethods = listOf(
        "Cash",
        "Credit Card",
        "Bank Transfer",
        "Cheque"
    )

    val statuses = listOf(
        "Pending",
        "Paid",
        "Reimbursed"
    )

    val currencies: List<String> = listOf("USD", "EUR", "GBP", "JPY", "VND")


    // ==================== EXPENSE LIST OPERATIONS ====================
    private var loadJob: Job? = null

    fun loadExpenses(projectId: String? = null) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true)
            try {
                // Load project budget if projectId is provided
                val budget = if (projectId != null) {
                    projectRepository.getProjectById(projectId).firstOrNull()?.budget
                } else null

                val flow = if (projectId != null) {
                    repository.getExpensesByProjectId(projectId)
                } else {
                    repository.getAllExpenses()
                }

                flow.collect { expenses ->
                    // Compute status counts from unfiltered expenses
                    val counts = ExpenseStatusCounts(
                        pending = expenses.count { it.status == "Pending" },
                        paid = expenses.count { it.status == "Paid" },
                        reimbursed = expenses.count { it.status == "Reimbursed" }
                    )

                    val currentState = _listState.value
                    var filtered = expenses.filter { expense ->
                        val matchesStatus = if (currentState.filterStatus != null) expense.status == currentState.filterStatus else true
                        val matchesType = if (currentState.filterType != null) expense.type == currentState.filterType else true
                        matchesStatus && matchesType
                    }

                    val total = filtered.sumOf { it.amount }

                    _listState.value = _listState.value.copy(
                        expenses = filtered,
                        isLoading = false,
                        error = null,
                        selectedProjectId = projectId,
                        totalAmount = total,
                        projectBudget = budget,
                        statusCounts = counts
                    )
                }
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun filterByStatus(status: String?) {
        _listState.value = _listState.value.copy(filterStatus = status)
        loadExpenses(_listState.value.selectedProjectId)
    }

    fun filterByType(type: String?) {
        _listState.value = _listState.value.copy(filterType = type)
        loadExpenses(_listState.value.selectedProjectId)
    }

    fun deleteExpense(expense: ExpenseEntity) {
        val context = getApplication<Application>().applicationContext
        viewModelScope.launch {
            try {
                val result = repository.deleteExpense(expense.expenseId, context)
                result.onFailure { e ->
                    _listState.value = _listState.value.copy(
                        error = "Deleted locally, but cloud sync failed: ${e.message}"
                    )
                }
                loadExpenses(_listState.value.selectedProjectId)
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(
                    error = "Failed to delete expense"
                )
            }
        }
    }

    // ==================== EXPENSE FORM OPERATIONS ====================

    fun onDateChange(date: String) {
        _formState.value = _formState.value.copy(
            date = date,
            dateError = if (date.isBlank()) "Date is required" else null
        )
    }

    fun onAmountChange(amount: String) {
        _formState.value = _formState.value.copy(
            amount = amount,
            amountError = when {
                amount.isBlank() -> "Amount is required"
                amount.toDoubleOrNull() == null -> "Amount must be a valid number"
                amount.toDouble() <= 0 -> "Amount must be greater than 0"
                else -> null
            }
        )
    }

    fun onCurrencyChange(currency: String) {
        _formState.value = _formState.value.copy(currency = currency)
    }

    fun onTypeChange(type: String) {
        _formState.value = _formState.value.copy(
            type = type,
            typeError = null
        )
    }

    fun onPaymentMethodChange(method: String) {
        _formState.value = _formState.value.copy(
            paymentMethod = method,
            paymentMethodError = null
        )
    }

    fun onClaimantChange(claimant: String) {
        val sanitized = claimant.take(500)
        _formState.value = _formState.value.copy(
            claimant = sanitized,
            claimantError = if (sanitized.isBlank()) "Claimant name is required" else null
        )
    }

    fun onStatusChange(status: String) {
        _formState.value = _formState.value.copy(status = status)
    }

    fun onDescriptionChange(description: String) {
        _formState.value = _formState.value.copy(description = description.take(500))
    }

    fun onLocationChange(location: String) {
        _formState.value = _formState.value.copy(location = location.take(500))
    }

    // Validate form
    private fun validateForm(): Boolean {
        val state = _formState.value
        var isValid = true

        if (state.date.isBlank()) {
            _formState.value = _formState.value.copy(dateError = "Date is required")
            isValid = false
        }

        if (state.amount.isBlank() || state.amount.toDoubleOrNull() == null || state.amount.toDouble() <= 0) {
            _formState.value = _formState.value.copy(amountError = "Amount must be a valid positive number")
            isValid = false
        }

        if (state.claimant.isBlank()) {
            _formState.value = _formState.value.copy(claimantError = "Claimant name is required")
            isValid = false
        }

        return isValid
    }

    fun requestConfirmation() {
        if (validateForm()) {
            _showConfirmDialog.value = true
        }
    }

    fun saveExpense() {
        val state = _formState.value
        val context = getApplication<Application>().applicationContext
        viewModelScope.launch {
            try {
                val expense = ExpenseEntity(
                    expenseId = state.expenseId,
                    projectId = state.projectId,
                    date = state.date,
                    amount = state.amount.toDouble(),
                    currency = state.currency,
                    type = state.type,
                    paymentMethod = state.paymentMethod,
                    claimant = state.claimant,
                    status = state.status,
                    description = state.description,
                    location = state.location
                )
                val result = if (state.isEditMode) {
                    repository.updateExpense(expense, context)
                } else {
                    repository.insertExpense(expense, context)
                }
                result.onFailure { e ->
                    _listState.value = _listState.value.copy(
                        error = "Saved locally, but cloud sync failed: ${e.message}"
                    )
                }
                _showConfirmDialog.value = false
                _saveSuccess.value = true
                loadExpenses(_listState.value.selectedProjectId)
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(
                    error = "Failed to save expense: ${e.message}"
                )
            }
        }
    }

    fun setEditMode(isEdit: Boolean) {
        _formState.value = _formState.value.copy(isEditMode = isEdit)
    }

    fun loadExpenseForEdit(expenseId: String) {
        viewModelScope.launch {
            try {
                val expense = repository.getExpenseById(expenseId).firstOrNull()
                if (expense != null) {
                    _formState.value = ExpenseFormState(
                        expenseId = expense.expenseId,
                        projectId = expense.projectId,
                        date = expense.date,
                        amount = expense.amount.toString(),
                        currency = expense.currency,
                        type = expense.type,
                        paymentMethod = expense.paymentMethod,
                        claimant = expense.claimant,
                        status = expense.status,
                        description = expense.description,
                        location = expense.location,
                        isEditMode = true
                    )
                }
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(
                    error = "Failed to load expense: ${e.message}"
                )
            }
        }
    }

    fun dismissConfirmDialog() {
        _showConfirmDialog.value = false
    }

    fun resetForm() {
        _formState.value = ExpenseFormState(
            projectId = _listState.value.selectedProjectId ?: ""
        )
        _saveSuccess.value = false
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }

    fun setProjectId(projectId: String) {
        _listState.value = _listState.value.copy(selectedProjectId = projectId)
        _formState.value = _formState.value.copy(projectId = projectId)
        loadExpenses(projectId)
    }

    fun resetFilters() {
        _listState.value = _listState.value.copy(
            filterStatus = null,
            filterType = null,
             selectedProjectId = null
        )
    }
}