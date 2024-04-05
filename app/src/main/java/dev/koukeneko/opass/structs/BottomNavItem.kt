package dev.koukeneko.opass.structs

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: @Composable () -> Unit,
)
