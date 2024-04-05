package dev.koukeneko.opass.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import dev.koukeneko.opass.components.AppBar

@Composable
fun ScheduleScreen(
    navController: NavHostController,
) {
//    // Collect UI state with lifecycle awareness
//    val uiState by eventViewModel.uiState.collectAsStateWithLifecycle()
//    // fetch scheduler Feature in current event
//    val schedulerFeature = uiState.currentEvent?.features?.find { it.feature == "schedule" }
//
//    val scheduleList = uiState.sessionList.groupBy {
//        //"start": "2023-07-30T10:00:00+08:00",
//        //"end": "2023-07-30T10:30:00+08:00",
//        it.start.split("T")[0]
//    }.values.toList()

    Scaffold(
        topBar = {
            AppBar(
                title = "temp",
                rightIcon = {
                    IconButton(onClick = {
                        Toast.makeText(
                            navController.context, "Button clicked", Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Localized description")
                    }
                },
                leftIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Go back")
                    }
                })
        }
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            val selectedTabIndex = remember { mutableIntStateOf(0) }

            TabRow(selectedTabIndex = selectedTabIndex.value) {
                Tab(
                    text = { Text(text = "Day 1") },
                    selected = selectedTabIndex.value == 0,
                    onClick = { selectedTabIndex.value = 0 }
                )
                Tab(
                    text = { Text(text = "Day 2") },
                    selected = selectedTabIndex.value == 1,
                    onClick = { selectedTabIndex.value = 1 }
                )
                Tab(
                    text = { Text(text = "Day 2") },
                    selected = selectedTabIndex.value == 1,
                    onClick = { selectedTabIndex.value = 1 }
                )
                Tab(
                    text = { Text(text = "Day 2") },
                    selected = selectedTabIndex.value == 1,
                    onClick = { selectedTabIndex.value = 1 }
                )
                Tab(
                    text = { Text(text = "Day 2") },
                    selected = selectedTabIndex.value == 1,
                    onClick = { selectedTabIndex.value = 1 }
                )
                Tab(
                    text = { Text(text = "Day 2") },
                    selected = selectedTabIndex.value == 1,
                    onClick = { selectedTabIndex.value = 1 }
                )
                Tab(
                    text = { Text(text = "Day 2") },
                    selected = selectedTabIndex.value == 1,
                    onClick = { selectedTabIndex.value = 1 }
                )
            }
            when (selectedTabIndex.value) {
                0 -> {
                    Text("Day 1")
                }
            }
        }
    }
}