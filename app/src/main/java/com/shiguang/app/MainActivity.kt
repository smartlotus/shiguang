package com.shiguang.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shiguang.app.ui.DetailScreen
import com.shiguang.app.ui.EditScreen
import com.shiguang.app.ui.EventsViewModel
import com.shiguang.app.ui.HomeScreen
import com.shiguang.app.ui.theme.ShiGuangTheme
import com.shiguang.app.widget.WidgetUpdater

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WidgetUpdater.scheduleMidnight(this)
        setContent {
            ShiGuangTheme {
                ShiGuangApp()
            }
        }
    }
}

sealed interface Screen {
    data object Home : Screen
    data class Detail(val eventId: Long) : Screen
    data class Edit(val eventId: Long?) : Screen
}

@Composable
fun ShiGuangApp(vm: EventsViewModel = viewModel()) {
    val events by vm.events.collectAsState()
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    BackHandler(enabled = screen !is Screen.Home) { screen = Screen.Home }

    when (val s = screen) {
        Screen.Home -> HomeScreen(
            events = events,
            onAdd = { screen = Screen.Edit(null) },
            onOpen = { screen = Screen.Detail(it) },
        )

        is Screen.Detail -> {
            val event = events.find { it.id == s.eventId }
            if (event == null) {
                HomeScreen(
                    events = events,
                    onAdd = { screen = Screen.Edit(null) },
                    onOpen = { screen = Screen.Detail(it) },
                )
            } else {
                DetailScreen(
                    event = event,
                    onBack = { screen = Screen.Home },
                    onEdit = { screen = Screen.Edit(s.eventId) },
                    onStyleChange = { vm.setStyle(s.eventId, it) },
                )
            }
        }

        is Screen.Edit -> EditScreen(
            existing = s.eventId?.let { id -> events.find { it.id == id } },
            onSave = { title, targetEpochDay, startEpochDay, repeatYearly, colorIndex, note ->
                vm.save(s.eventId, title, targetEpochDay, startEpochDay, repeatYearly, colorIndex, note)
                screen = Screen.Home
            },
            onDelete = s.eventId?.let { id ->
                val action: () -> Unit = {
                    vm.delete(id)
                    screen = Screen.Home
                }
                action
            },
            onBack = {
                screen = if (s.eventId != null) Screen.Detail(s.eventId) else Screen.Home
            },
        )
    }
}
