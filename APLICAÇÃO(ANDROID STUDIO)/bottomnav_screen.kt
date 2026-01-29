package com.example.social_rede_mobile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*

@Composable
fun BottomNavigationBar(navController: NavController) {
    val screens = listOf(
        BottomNavScreen.Home,
        BottomNavScreen.Search,
        BottomNavScreen.Stories,
        BottomNavScreen.Messages,
        BottomNavScreen.Notifications,
        BottomNavScreen.Profile
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route


    NavigationBar {
        screens.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}

// Define bottom nav destinations
sealed class BottomNavScreen(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavScreen("home", Icons.Default.Home, "Home")
    object Search : BottomNavScreen("search", Icons.Default.Search, "Search")
    object Stories : BottomNavScreen("stories", Icons.Default.PlayArrow, "Stories")
    object Messages : BottomNavScreen("messages", Icons.Default.Email, "Messages")
    object Notifications : BottomNavScreen("notifications", Icons.Default.FavoriteBorder, "Notifications")
    object Profile : BottomNavScreen("profile", Icons.Default.AccountCircle, "Profile")
}