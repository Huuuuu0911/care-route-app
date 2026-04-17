package com.example.cs501_final_project.navigation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cs501_final_project.data.AuthSession
import com.example.cs501_final_project.data.AuthViewModel
import com.example.cs501_final_project.data.CareRouteViewModel
import com.example.cs501_final_project.ui.AuthScreen
import com.example.cs501_final_project.ui.BodyPart3DScreen
import com.example.cs501_final_project.ui.DetailScreen
import com.example.cs501_final_project.ui.FollowUpScreen
import com.example.cs501_final_project.ui.HistoryScreen
import com.example.cs501_final_project.ui.HomeScreen
import com.example.cs501_final_project.ui.MapScreen
import com.example.cs501_final_project.ui.SettingScreen

private sealed class BottomNavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val activeColor: Color
) {
    data object Home : BottomNavDestination("home", "Home", Icons.Default.Home, Color(0xFF4F8EEB))
    data object History : BottomNavDestination("history", "History", Icons.Default.History, Color(0xFF7B61FF))
    data object Map : BottomNavDestination("map", "Map", Icons.Default.Map, Color(0xFF12B76A))
    data object Setting : BottomNavDestination("setting", "Setting", Icons.Default.Settings, Color(0xFFF79009))
}

@Composable
fun AppNav(
    viewModel: CareRouteViewModel,
    authViewModel: AuthViewModel
) {
    val session = authViewModel.session

    if (session == null) {
        AuthScreen(authViewModel = authViewModel)
    } else {
        MainAppNav(
            viewModel = viewModel,
            authViewModel = authViewModel,
            session = session
        )
    }
}

@Composable
private fun MainAppNav(
    viewModel: CareRouteViewModel,
    authViewModel: AuthViewModel,
    session: AuthSession
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(session.userId, session.displayName, session.isEmergencyMode) {
        val currentName = viewModel.selfProfile.name.trim()
        if (session.isEmergencyMode) {
            if (currentName.isBlank() || currentName == "You") {
                viewModel.updateSelfProfile(viewModel.selfProfile.copy(name = "Emergency Guest"))
            }
        } else if (currentName.isBlank() || currentName == "You" || currentName == "Emergency Guest") {
            viewModel.updateSelfProfile(viewModel.selfProfile.copy(name = session.displayName))
        }
    }

    val topLevelRoutes = setOf(
        BottomNavDestination.Home.route,
        BottomNavDestination.History.route,
        BottomNavDestination.Map.route,
        BottomNavDestination.Setting.route,
        "map?query={query}"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                CareRouteBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        val targetRoute = when (route) {
                            BottomNavDestination.Map.route -> "map"
                            else -> route
                        }

                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavDestination.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavDestination.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onStartClick = { navController.navigate("body3d") },
                    onHistoryClick = { navController.navigate(BottomNavDestination.History.route) },
                    onMapClick = { navController.navigate(BottomNavDestination.Map.route) },
                    onSettingClick = { navController.navigate(BottomNavDestination.Setting.route) }
                )
            }

            composable(BottomNavDestination.History.route) {
                HistoryScreen(
                    viewModel = viewModel,
                    onOpenMap = { query ->
                        navController.navigate("map?query=${Uri.encode(query)}")
                    }
                )
            }

            composable(BottomNavDestination.Map.route) {
                MapScreen()
            }

            composable(
                route = "map?query={query}",
                arguments = listOf(
                    navArgument("query") {
                        type = NavType.StringType
                        defaultValue = ""
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val query = Uri.decode(backStackEntry.arguments?.getString("query").orEmpty())
                MapScreen(initialQuery = query)
            }

            composable(BottomNavDestination.Setting.route) {
                SettingScreen(viewModel = viewModel)
            }

            composable("body3d") {
                BodyPart3DScreen(navController = navController)
            }

            composable(
                route = "detail/{part}",
                arguments = listOf(
                    navArgument("part") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                DetailScreen(
                    part = Uri.decode(backStackEntry.arguments?.getString("part").orEmpty()),
                    navController = navController
                )
            }

            composable(
                route = "follow_up/{part}/{symptomText}/{painLevel}",
                arguments = listOf(
                    navArgument("part") { type = NavType.StringType },
                    navArgument("symptomText") { type = NavType.StringType },
                    navArgument("painLevel") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                FollowUpScreen(
                    part = Uri.decode(backStackEntry.arguments?.getString("part").orEmpty()),
                    symptomText = Uri.decode(backStackEntry.arguments?.getString("symptomText").orEmpty()),
                    painLevel = backStackEntry.arguments?.getString("painLevel")?.toIntOrNull() ?: 0,
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun CareRouteBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavDestination.Home,
        BottomNavDestination.History,
        BottomNavDestination.Map,
        BottomNavDestination.Setting
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 14.dp,
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEach { item ->
                val selected = when (item.route) {
                    "map" -> currentRoute == "map" || currentRoute == "map?query={query}"
                    else -> currentRoute == item.route
                }

                val selectedBrush = Brush.horizontalGradient(
                    colors = listOf(
                        item.activeColor.copy(alpha = 0.18f),
                        item.activeColor.copy(alpha = 0.08f)
                    )
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            brush = if (selected) {
                                selectedBrush
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, Color.Transparent)
                                )
                            }
                        )
                        .clickable { onNavigate(item.route) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (selected) item.activeColor else Color(0xFF98A2B3)
                        )

                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) item.activeColor else Color(0xFF667085)
                        )
                    }
                }
            }
        }
    }
}
