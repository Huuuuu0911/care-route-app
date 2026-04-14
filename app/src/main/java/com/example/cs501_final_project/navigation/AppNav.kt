package com.example.cs501_final_project.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cs501_final_project.ui.BodyPart3DScreen
import com.example.cs501_final_project.ui.DetailScreen
import com.example.cs501_final_project.ui.FollowUpScreen
import com.example.cs501_final_project.ui.HistoryScreen
import com.example.cs501_final_project.ui.HomeScreen
import com.example.cs501_final_project.ui.MapScreen
import com.example.cs501_final_project.ui.SettingScreen
import androidx.compose.foundation.layout.padding

sealed class BottomNavItem(
    val route: String,
    val title: String
) {
    object Home : BottomNavItem("home", "Home")
    object History : BottomNavItem("history", "History")
    object Map : BottomNavItem("map", "Map")
    object Setting : BottomNavItem("setting", "Setting")
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

    val bottomItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.History,
        BottomNavItem.Map,
        BottomNavItem.Setting
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // 这些页面不显示底部导航栏
    val hideBottomBarRoutes = listOf(
        "body_3d",
        "detail/{part}",
        "follow_up/{part}/{symptomText}/{painLevel}"
    )

    val showBottomBar = hideBottomBarRoutes.none { it == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                when (item.route) {
                                    "home" -> Icon(Icons.Default.Home, contentDescription = item.title)
                                    "history" -> Icon(Icons.Default.History, contentDescription = item.title)
                                    "map" -> Icon(Icons.Default.LocationOn, contentDescription = item.title)
                                    else -> Icon(Icons.Default.Settings, contentDescription = item.title)
                                }
                            },
                            label = { Text(item.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    onStartClick = {
                        navController.navigate("body_3d")
                    },
                    onHistoryClick = {
                        navController.navigate("history")
                    },
                    onMapClick = {
                        navController.navigate("map")
                    },
                    onSettingClick = {
                        navController.navigate("setting")
                    }
                )
            }

            composable("history") {
                HistoryScreen()
            }

            composable("map") {
                MapScreen()
            }

            composable("setting") {
                SettingScreen()
            }

            composable("body_3d") {
                BodyPart3DScreen(navController = navController)
            }

            composable(
                route = "detail/{part}",
                arguments = listOf(
                    navArgument("part") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val part = backStackEntry.arguments?.getString("part") ?: ""
                DetailScreen(
                    part = part,
                    navController = navController
                )
            }


            ) { backStackEntry ->
                val part = backStackEntry.arguments?.getString("part") ?: ""
                val symptomText = backStackEntry.arguments?.getString("symptomText") ?: ""
                val painLevel = backStackEntry.arguments?.getInt("painLevel") ?: 0

                FollowUpScreen(
                    part = part,
                    symptomText = symptomText,
                    painLevel = painLevel,
                    navController = navController
                )
            }
        }
    }
}