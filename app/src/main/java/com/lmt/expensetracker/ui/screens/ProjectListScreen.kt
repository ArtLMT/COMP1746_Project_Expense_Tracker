package com.lmt.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lmt.expensetracker.viewmodel.ProjectViewModel

@Composable
fun ProjectListScreen(
    viewModel: ProjectViewModel,
    onNavigateToCreateProject: () -> Unit,
    onNavigateToEditProject: (String) -> Unit,
    onNavigateToProjectDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState by viewModel.listState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CustomColors.BackgroundDark)
    ) {
        when {
            listState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading projects...", color = CustomColors.White)
                }
            }
            listState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: ${listState.error}", color = CustomColors.White)
                }
            }
            listState.projects.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No projects found", color = CustomColors.White)
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
                    ) { projectWithSpent ->
                        ProjectCard(
                            projectWithSpent = projectWithSpent,
                            onEdit = { onNavigateToEditProject(projectWithSpent.project.projectId) },
                            onDelete = { viewModel.deleteProject(projectWithSpent.project) },
                            onCardClick = { onNavigateToProjectDetail(projectWithSpent.project.projectId) }
                        )
                    }
                }
            }
        }
    }
}
