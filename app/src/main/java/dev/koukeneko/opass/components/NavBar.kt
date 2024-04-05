package dev.koukeneko.opass.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import dev.koukeneko.opass.structs.BottomNavItem

@Composable
fun NavigationBarComponent(
    bottomNavItems: List<BottomNavItem>,
    navController: NavController,
    backStackEntry: State<NavBackStackEntry?>
) {
    NavigationBar {
        val currentRoute = backStackEntry.value?.destination?.route
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = item.icon,
                label = { Text(item.name) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // This will clear the backstack to the start destination and start fresh
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
