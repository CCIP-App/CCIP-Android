import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.koukeneko.opass.utils.EventViewModel
import dev.koukeneko.opass.R
import dev.koukeneko.opass.components.AppBar
import dev.koukeneko.opass.components.PanelBtn
import dev.koukeneko.opass.utils.EventUiStateRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    ) {

    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded, skipHiddenState = true
        )
    )

    // Obtain the ViewModel scoped to this Composable
    val eventViewModel: EventViewModel = viewModel()

    val currentIdFromStorage by EventUiStateRepository(context).getCurrentEventId.collectAsStateWithLifecycle(null)
    Log.d("context", "currentFromStorage: $currentIdFromStorage")
    LaunchedEffect(currentIdFromStorage) {

       try {
           if (currentIdFromStorage == null) {
               val firstEventId = eventViewModel.getEventList().firstOrNull()?.eventId
               if (firstEventId != null) {
                   eventViewModel.setCurrentEvent(eventViewModel.getEventList().first().eventId)
               } else {
                   // Handle the case where there are no events
                   Log.d("EventList", "No events found")
               }

           }else{
               currentIdFromStorage?.let {
                   eventViewModel.setCurrentEventId(it)
               }
           }
       }catch (e: Exception){
           Log.d("context", "currentFromStorage: $e")
       }
    }

    // Collect UI state with lifecycle awareness
    val uiState by eventViewModel.uiState.collectAsStateWithLifecycle()
    val events = uiState.eventList
    val currentEvent = uiState.currentEvent
    var searchEvent by remember { mutableStateOf("") }
    val filteredItems = events.filter { event ->
        event.displayName["zh"]?.contains(
            searchEvent, ignoreCase = true
        ) == true
    }





    // update buttons panel
    val buttons = currentEvent?.features?.map { btn ->
        PanelButton(
            title = btn.displayText["zh"] ?: btn.displayText["en"] ?: "未命名",
            icon = when (btn.feature) {
                "webview" -> btn.icon ?: ""
                else -> btn.feature
            },
            onClick = {
                when (btn.feature) {
                    "webview" -> if (btn.url != null) {
                        val encodedUrl = Uri.encode(btn.url)
                        navController.navigate("web_view/${btn.displayText["zh"]}/$encodedUrl")
                    }
                    "schedule" -> navController.navigate("schedule")

                    "telegram" -> if (btn.url != null) {
                    // Directly open the Telegram app
                    val telegramUrl = "https://t.me/${btn.url}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl))
                        context.startActivity(intent)
                    }

                    else -> {
                        // web-view but feature is not set to web-view
                        if (btn.url != null) {
                            val encodedUrl = Uri.encode(btn.url)
                            navController.navigate("web_view/${btn.displayText["zh"]}/$encodedUrl")
                        }
                    }
                }

                Toast.makeText(navController.context, "Button clicked", Toast.LENGTH_SHORT).show()
            },
            type = when (btn.feature) {
                "webview" -> PanelButtonType.WEBVIEW
                else -> PanelButtonType.DEFAULT
            }
        )

    } ?: emptyList() // Provide an empty list as fallback




    // Handle back button press when bottom sheet is expanded
    // If this is not handled, the back button will navigate to the previous screen ( exit the app )
    BackHandler(scaffoldState.bottomSheetState.isVisible, onBack = {
        //hide bottom sheet
        scope.launch {
            scaffoldState.bottomSheetState.partialExpand()
        }
    })



    BottomSheetScaffold(
        topBar = {
            AppBar(subtitle = "KoukeNeko",
                title = uiState.currentEvent?.displayName?.get("zh").orEmpty(),
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
                    IconButton(onClick = {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.rounded_stack_24),
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = "Localized description",
                        )
                    }
                })
        }, scaffoldState = scaffoldState, sheetPeekHeight = 0.dp, sheetContent = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp, end = 10.dp),
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.Search,
                                        contentDescription = "Localized description"
                                    )
                                },
                                label = { Text("搜尋活動") },
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .padding(start = 15.dp),
                                value = searchEvent,
                                onValueChange = {
                                    searchEvent = it
                                })
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                itemsIndexed(filteredItems) { index, event ->
                    val shape = when (index) {
                        0 -> if (filteredItems.size == 1) {
                            // If there's only one item, apply rounded corners to all sides
                            RoundedCornerShape(10.dp)
                        } else {
                            // First item, only round the top corners
                            RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                        }

                        filteredItems.lastIndex -> RoundedCornerShape(
                            bottomStart = 10.dp, bottomEnd = 10.dp
                        )

                        else -> RoundedCornerShape(0.dp)
                    }
                    Card(
                        shape = shape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, end = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // set current event id, viewModel will update the current event
                                    scope.launch {
                                        eventViewModel.setCurrentEvent(event.eventId)
                                    }
                                    //save current event id to EventUiStateRepository
                                    scope.launch {
                                        EventUiStateRepository(context).saveCurrentEventId(event.eventId)
                                    }
                                    // hide bottom sheet
                                    scope.launch {
                                        scaffoldState.bottomSheetState.partialExpand()
                                    }
                                }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AsyncImage(
                                    model = event.logoUrl,
                                    contentDescription = event.displayName["zh"],
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .height(70.dp)
                                        .width(100.dp),
//                                if dark mode or name include devfest, keep the original color
                                    colorFilter = if (event.displayName["zh"]?.contains("DevFest") == true) {
                                        null
                                    } else {
                                        ColorFilter.tint(
                                            if (isSystemInDarkTheme()) {
                                                Color.White
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                )

                                event.displayName["zh"]?.let {
                                    Text(
                                        text = it,
                                    )
                                }
                            }
                            Icon(
                                imageVector = chevron_right(),
                                contentDescription = event.displayName["zh"],
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .height(30.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    if (index < events.lastIndex) {
                        Divider(
                            color = Color.Transparent,
                            thickness = 1.dp,
                        )
                    }
                }
                if (filteredItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "找不到符合的活動")
                        }
                    }
                } else {
                    item {

                        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.R) {
                            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                        } else {
                            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                        }
                    }
                }

                item {
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime)) //can remove
                }
            }

        }) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
//            Text(text)

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = uiState.currentEvent?.logoUrl ?: "",
                    contentDescription = uiState.currentEvent?.displayName?.get("zh").orEmpty(),
                    modifier = Modifier
                        .width(250.dp)
                        .height(180.dp)
                        .align(Alignment.BottomCenter),
                    colorFilter = if (uiState.currentEvent?.displayName?.get("zh")
                            ?.contains("DevFest", ignoreCase = true) == true
                    ) {
                        null
                    } else {
                        ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(4), // Fixed count of 4 items per row
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(buttons) { button ->
                    if (button.type == PanelButtonType.DEFAULT) {
                        PanelBtn(
                            title = button.title,
                            icon = iconMapper(button.icon),
                            onClick = button.onClick,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (button.type == PanelButtonType.WEBVIEW) {
                        PanelBtn(
                            title = button.title,
                            iconUrl = button.icon,
                            onClick = button.onClick,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun iconMapper(iconName: String): ImageVector {
    return when (iconName.lowercase().trim()) {
        "fastpass" -> ticket()
        "discord" -> discord()
        "ticket" -> qrcode()
        "schedule" -> calendar_clock()
        "announcement" -> speakerphone()
        "wifi" -> wifi()
        "venue" -> map()
        "sponsors" -> cash()
        "staffs" -> user_grop()
        "telegram" -> telegram()
        "im" -> chat()
        else -> cube() // A default icon if the name doesn't match
    }
}

data class PanelButton(
    val title: String,
    val icon: String,
    val onClick: () -> Unit,
    var type: PanelButtonType = PanelButtonType.DEFAULT
)

enum class PanelButtonType {
    DEFAULT, WEBVIEW, WIFI
}



