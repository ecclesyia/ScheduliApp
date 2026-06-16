package com.example.scheduliapp.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.scheduliapp.data.LocalJsonDataRepository
import com.example.scheduliapp.theme.*
import com.example.scheduliapp.ui.screens.AccountsScreen
import com.example.scheduliapp.ui.screens.ScheduleScreen
import com.example.scheduliapp.ui.screens.ScrumBoardScreen

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current.applicationContext
    val viewModel: MainScreenViewModel = viewModel {
        MainScreenViewModel(LocalJsonDataRepository(context))
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (state) {
        MainScreenUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = IndigoAccent)
            }
        }
        is MainScreenUiState.Success -> {
            val data = (state as MainScreenUiState.Success).data
            MainAppLayout(
                viewModel = viewModel,
                tasks = data.tasks,
                meetings = data.meetings,
                accounts = data.accounts,
                activityLogs = data.activityLogs,
                modifier = modifier
            )
        }
        is MainScreenUiState.Error -> {
            Box(
                modifier = modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error loading Scheduli data: ${(state as MainScreenUiState.Error).throwable.message}",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Actual implementation with proper signatures
@Composable
fun MainAppLayout(
    viewModel: MainScreenViewModel,
    tasks: List<com.example.scheduliapp.data.Task>,
    meetings: List<com.example.scheduliapp.data.Meeting>,
    accounts: List<com.example.scheduliapp.data.ConnectedAccount>,
    activityLogs: List<com.example.scheduliapp.data.ActivityLog>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceContainer,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = "Scrum Board") },
                    label = { Text("Scrum Board") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IndigoAccent,
                        selectedTextColor = IndigoAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = SurfaceCard
                    )
                )
                
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Schedule") },
                    label = { Text("Schedule") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealAccent,
                        selectedTextColor = TealAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = SurfaceCard
                    )
                )
                
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Link, contentDescription = "Integrations") },
                    label = { Text("Sync Accounts") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IndigoAccent,
                        selectedTextColor = IndigoAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = SurfaceCard
                    )
                )
            }
        },
        containerColor = DarkBackground,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        val innerModifier = Modifier.padding(paddingValues)
        
        when (selectedTab) {
            0 -> ScrumBoardScreen(
                viewModel = viewModel,
                tasks = tasks,
                activityLogs = activityLogs,
                modifier = innerModifier
            )
            1 -> ScheduleScreen(
                viewModel = viewModel,
                meetings = meetings,
                tasks = tasks,
                modifier = innerModifier
            )
            2 -> AccountsScreen(
                viewModel = viewModel,
                accounts = accounts,
                modifier = innerModifier
            )
        }
    }
}
