package com.lmt.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lmt.expensetracker.data.entities.ProjectEntity
import com.lmt.expensetracker.data.repository.ProjectRepository
import com.lmt.expensetracker.ui.theme.BudgetStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

// ==================== UI MODELS ====================

data class ProjectCardUiModel(
    val project: ProjectEntity,
    val spentAmount: Double,
    val progressFraction: Float,
    val financialStatus: BudgetStatus // "On Track", "At Risk", "Over Budget"
)

data class ProjectFormState(
    val projectId: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val manager: String = "",
    val status: String = "Active",
    val budget: String = "",
    val specialRequirements: String = "",
    val clientDepartmentInfo: String = "",
    val isEditMode: Boolean = false,
    val nameError: String? = null,
    val descriptionError: String? = null,
    val startDateError: String? = null,
    val endDateError: String? = null,
    val managerError: String? = null,
    val budgetError: String? = null
)

data class ProjectListState(
    val projects: List<ProjectCardUiModel> = emptyList(), // Đã đổi sang UiModel
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val filterStatus: String? = null
)

data class StatusCounts(
    val active: Int = 0,
    val completed: Int = 0,
    val onHold: Int = 0
)

// ==================== VIEWMODEL ====================

class ProjectViewModel(private val repository: ProjectRepository) : ViewModel() {

    private val _formState = MutableStateFlow(ProjectFormState())
    val formState: StateFlow<ProjectFormState> = _formState.asStateFlow()

    private val _listState = MutableStateFlow(ProjectListState())
    val listState: StateFlow<ProjectListState> = _listState.asStateFlow()

    private val _statusCounts = MutableStateFlow(StatusCounts())
    val statusCounts: StateFlow<StatusCounts> = _statusCounts.asStateFlow()

    private val _showConfirmDialog = MutableStateFlow(false)
    val showConfirmDialog: StateFlow<Boolean> = _showConfirmDialog.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // Single Source of Truth for status options — consumed by the UI layer
    val statusOptions: List<String> = listOf(
        "Active", "Completed", "On Hold"
    )

    init {
        loadProjects()
    }

    // ==================== LOGIC HELPERS ====================
//TODO: Tính toàn bộ Expense rồi so với Budget
    /**
     * Chuyển đổi dữ liệu thô từ DB sang dữ liệu đã tính toán cho UI
     */
    private fun mapToUiModels(projects: List<com.lmt.expensetracker.data.entities.ProjectWithSpent>): List<ProjectCardUiModel> {
        return projects.map { item ->
            val progress = if (item.project.budget > 0) {
                (item.spentAmount / item.project.budget).toFloat()
            } else 0f

            val finStatus = when {
                progress >= 1.0f -> BudgetStatus.OVER_BUDGET
                progress >= 0.8f -> BudgetStatus.AT_RISK
                else -> BudgetStatus.ON_TRACK
            }

            ProjectCardUiModel(
                project = item.project,
                spentAmount = item.spentAmount,
                progressFraction = progress,
                financialStatus = finStatus
            )
        }
    }

    // ==================== PROJECT LIST OPERATIONS ====================

    fun loadProjects() {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true)
            try {
                repository.getAllProjectsWithSpent().collect { projects ->
                    _statusCounts.value = StatusCounts(
                        active = projects.count { it.project.status == "Active" },
                        completed = projects.count { it.project.status == "Completed" },
                        onHold = projects.count { it.project.status == "On Hold" }
                    )

                    val filtered = if (_listState.value.filterStatus != null) {
                        projects.filter { it.project.status == _listState.value.filterStatus }
                    } else {
                        projects
                    }

                    val searched = if (_listState.value.searchQuery.isNotEmpty()) {
                        filtered.filter {
                            it.project.name.contains(_listState.value.searchQuery, ignoreCase = true) ||
                                    it.project.description.contains(_listState.value.searchQuery, ignoreCase = true)
                        }
                    } else {
                        filtered
                    }

                    // ViewModel thực hiện tính toán ở đây trước khi đẩy ra UI
                    _listState.value = _listState.value.copy(
                        projects = mapToUiModels(searched),
                        isLoading = false,
                        error = null
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

    fun searchProjects(query: String) {
        _listState.value = _listState.value.copy(searchQuery = query)
        viewModelScope.launch {
            try {
                repository.searchProjectsWithSpent(query).collect { projects ->
                    val filtered = if (_listState.value.filterStatus != null) {
                        projects.filter { it.project.status == _listState.value.filterStatus }
                    } else {
                        projects
                    }

                    _listState.value = _listState.value.copy(
                        projects = mapToUiModels(filtered),
                        error = null
                    )
                }
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(
                    error = e.message ?: "Search failed"
                )
            }
        }
    }

    fun filterByStatus(status: String?) {
        _listState.value = _listState.value.copy(filterStatus = status)
        loadProjects()
    }

    // ... (Các hàm khác như saveProject, onNameChange, validateForm giữ nguyên) ...

    fun deleteProject(projectEntity: ProjectEntity) {
        viewModelScope.launch {
            try {
                repository.deleteProject(projectEntity)
                loadProjects()
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(error = "Failed to delete project")
            }
        }
    }

    fun onNameChange(name: String) {
        _formState.value = _formState.value.copy(
            name = name,
            nameError = if (name.isBlank()) "Project name is required" else null
        )
    }

    fun onDescriptionChange(description: String) {
        _formState.value = _formState.value.copy(
            description = description,
            descriptionError = if (description.isBlank()) "Description is required" else null
        )
    }

    fun onStartDateChange(date: String) {
        _formState.value = _formState.value.copy(
            startDate = date,
            startDateError = if (date.isBlank()) "Start date is required" else null
        )
    }

    fun onEndDateChange(date: String) {
        _formState.value = _formState.value.copy(
            endDate = date,
            endDateError = if (date.isBlank()) "End date is required" else null
        )
    }

    fun onManagerChange(manager: String) {
        _formState.value = _formState.value.copy(
            manager = manager,
            managerError = if (manager.isBlank()) "Manager name is required" else null
        )
    }

    fun onStatusChange(status: String) {
        _formState.value = _formState.value.copy(status = status)
    }

    fun onBudgetChange(budget: String) {
        _formState.value = _formState.value.copy(
            budget = budget,
            budgetError = when {
                budget.isBlank() -> "Budget is required"
                budget.toDoubleOrNull() == null -> "Budget must be a valid number"
                budget.toDouble() < 0 -> "Budget cannot be negative"
                else -> null
            }
        )
    }

    fun onSpecialRequirementsChange(special: String) {
        _formState.value = _formState.value.copy(specialRequirements = special)
    }

    fun onClientInfoChange(info: String) {
        _formState.value = _formState.value.copy(clientDepartmentInfo = info)
    }

    private fun validateForm(): Boolean {
        val state = _formState.value
        var isValid = true
        if (state.name.isBlank()) { _formState.value = _formState.value.copy(nameError = "Project name is required"); isValid = false }
        if (state.description.isBlank()) { _formState.value = _formState.value.copy(descriptionError = "Description is required"); isValid = false }
        if (state.startDate.isBlank()) { _formState.value = _formState.value.copy(startDateError = "Start date is required"); isValid = false }
        if (state.endDate.isBlank()) { _formState.value = _formState.value.copy(endDateError = "End date is required"); isValid = false }
        if (state.manager.isBlank()) { _formState.value = _formState.value.copy(managerError = "Manager name is required"); isValid = false }
        if (state.budget.isBlank() || state.budget.toDoubleOrNull() == null || state.budget.toDouble() < 0) {
            _formState.value = _formState.value.copy(budgetError = "Budget must be a valid positive number")
            isValid = false
        }
        return isValid
    }

    fun requestConfirmation() {
        if (validateForm()) _showConfirmDialog.value = true
    }

    fun saveProject() {
        val state = _formState.value
        viewModelScope.launch {
            try {
                val project = ProjectEntity(
                    projectId = state.projectId,
                    name = state.name,
                    description = state.description,
                    startDate = state.startDate,
                    endDate = state.endDate,
                    manager = state.manager,
                    status = state.status,
                    budget = state.budget.toDouble(),
                    specialRequirements = state.specialRequirements,
                    clientDepartmentInfo = state.clientDepartmentInfo
                )
                if (state.isEditMode) repository.updateProject(project)
                else repository.insertProject(project)
                _showConfirmDialog.value = false
                _saveSuccess.value = true
                resetForm()
                loadProjects()
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(error = "Failed to save project: ${e.message}")
            }
        }
    }

    fun setEditMode(isEdit: Boolean) { _formState.value = _formState.value.copy(isEditMode = isEdit) }

    fun loadProjectForEdit(projectId: String) {
        viewModelScope.launch {
            repository.getProjectById(projectId).collect { project ->
                project?.let {
                    _formState.value = ProjectFormState(
                        projectId = it.projectId,
                        name = it.name,
                        description = it.description,
                        startDate = it.startDate,
                        endDate = it.endDate,
                        manager = it.manager,
                        status = it.status,
                        budget = it.budget.toString(),
                        specialRequirements = it.specialRequirements,
                        clientDepartmentInfo = it.clientDepartmentInfo,
                        isEditMode = true
                    )
                }
            }
        }
    }

    fun dismissConfirmDialog() { _showConfirmDialog.value = false }
    fun resetForm() { _formState.value = ProjectFormState(); _saveSuccess.value = false }
    fun resetSaveSuccess() { _saveSuccess.value = false }
}