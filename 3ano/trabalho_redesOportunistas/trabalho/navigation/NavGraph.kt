package com.example.trabalho.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.example.trabalho.ui.screens.ChatScreen
import com.example.trabalho.ui.screens.HomeScreen
import com.example.trabalho.ui.viewmodel.ChatViewModel
import com.example.trabalho.ui.viewmodel.HomeViewModel

enum class AppScreens { Home, Chat }

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppScreens.Home.name
    ) {
        // Ecrã inicial
        composable(route = AppScreens.Home.name) {
            val homeViewModel: HomeViewModel = viewModel()

            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToSimulator = { roleSelecionado ->
                    // Em vez de navegar só para "Chat", navegamos para "Chat/SEED" ou "Chat/HELPER"
                    navController.navigate("${AppScreens.Chat.name}/$roleSelecionado")
                }
            )
        }

        // Ecrã de Chat
        composable(
            route = "${AppScreens.Chat.name}/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "Desconhecido"

            val chatViewModel: ChatViewModel = viewModel()
            val chatUIState by chatViewModel.uiState.collectAsState()
            val networkState by chatViewModel.networkState.collectAsState()

            ChatScreen(
                chatUIState = chatUIState,
                networkState = networkState,
                userRole = role,
                onMessageChange = { chatViewModel.updateInputText(it) },
                onSendMessage = { chatViewModel.sendMessage() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}