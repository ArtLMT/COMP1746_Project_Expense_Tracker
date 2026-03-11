package com.lmt.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lmt.expensetracker.ui.components.ProjectCard
import com.lmt.expensetracker.viewmodel.ProjectViewModel

@Composable
fun ProjectListScreen(
    viewModel: ProjectViewModel,
    onNavigateToCreateProject: () -> Unit,
    onNavigateToEditProject: (String) -> Unit,
    onNavigateToProjectDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            listState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading projects...", color = MaterialTheme.colorScheme.onSurface)
                }
            }
            listState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: ${listState.error}", color = MaterialTheme.colorScheme.onSurface)
                }
            }
            listState.projects.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No projects found", color = MaterialTheme.colorScheme.onSurface)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 100.dp)
                ) {
                    items(
                        items = listState.projects,
                        key = { it.project.projectId }
                    ) { uiModel ->
                        ProjectCard(
                            uiModel = uiModel,
                            onEdit = { onNavigateToEditProject(uiModel.project.projectId) },
                            onDelete = { viewModel.deleteProject(uiModel.project) },
                            onCardClick = { onNavigateToProjectDetail(uiModel.project.projectId) }
                        )
                    }
                }
            }
        }
    }
}
