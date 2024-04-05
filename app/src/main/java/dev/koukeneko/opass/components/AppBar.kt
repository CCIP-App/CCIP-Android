package dev.koukeneko.opass.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import dev.koukeneko.opass.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppBar(
    subtitle: String? = null,
    title: String? = null,
    leftIcon: @Composable (() -> Unit),
    rightIcon: @Composable (() -> Unit)
) {
    CenterAlignedTopAppBar(
        title = { Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Check if title is not null before displaying
            if (title != null) {
                Text(title)
            }
            // Check if subtitle is not null before displaying
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.titleSmall)
            }
        } },
        navigationIcon = leftIcon, // Directly use the lambda expression
        actions = {
            rightIcon.invoke()
        }
    )
}

