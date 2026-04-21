package com.example.cs501_final_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.cs501_final_project.data.AuthViewModel
import com.example.cs501_final_project.data.CareRouteViewModel
import com.example.cs501_final_project.navigation.AppNav
import com.example.cs501_final_project.ui.theme.CareRouteTheme

class MainActivity : ComponentActivity() {

    private val careRouteViewModel: CareRouteViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CareRouteTheme(
                darkTheme = careRouteViewModel.settings.darkModeEnabled,
                accentTheme = careRouteViewModel.settings.accentTheme
            ) {
                AppNav(
                    viewModel = careRouteViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }
}
