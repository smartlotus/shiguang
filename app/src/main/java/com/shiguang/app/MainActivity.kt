@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.shiguang.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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

    /** 小组件深链：待打开的事件 id（null 表示无） */
    private val pendingOpenEventId = mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        pendingOpenEventId.value = intent?.getLongExtra(EXTRA_OPEN_EVENT_ID, -1L)?.takeIf { it > 0 }
        WidgetUpdater.scheduleMidnight(this)
        setContent {
            ShiGuangTheme {
                ShiGuangApp(pendingOpenEventId = pendingOpenEventId)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingOpenEventId.value = intent.getLongExtra(EXTRA_OPEN_EVENT_ID, -1L).takeIf { it > 0 }
    }

    companion object {
        const val EXTRA_OPEN_EVENT_ID = "open_event_id"
    }
}

sealed interface Screen {
    data object Home : Screen
    data class Detail(val eventId: Long) : Screen
    data class Edit(val eventId: Long?) : Screen

    /** 导航层级：决定转场方向（进深=右滑入，返回=左滑入） */
    fun depth(): Int = when (this) {
        Home -> 0
        is Detail -> 1
        is Edit -> if (eventId != null) 2 else 1
    }
}

@Composable
fun ShiGuangApp(
    vm: EventsViewModel = viewModel(),
    pendingOpenEventId: MutableState<Long?>? = null,
) {
    val events by vm.events.collectAsState()
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    BackHandler(enabled = screen !is Screen.Home) { screen = Screen.Home }

    // 小组件点进来的深链：跳到对应日子的详情页
    LaunchedEffect(pendingOpenEventId?.value) {
        val id = pendingOpenEventId?.value ?: return@LaunchedEffect
        if (events?.any { it.id == id } == true) screen = Screen.Detail(id)
        pendingOpenEventId.value = null
    }

    AnimatedContent(
        targetState = screen,
        label = "nav",
        transitionSpec = {
            if (targetState.depth() >= initialState.depth()) {
                (slideInHorizontally(tween(280)) { it / 5 } + fadeIn(tween(280))) togetherWith
                    (slideOutHorizontally(tween(280)) { -it / 7 } + fadeOut(tween(220)))
            } else {
                (slideInHorizontally(tween(280)) { -it / 5 } + fadeIn(tween(280))) togetherWith
                    (slideOutHorizontally(tween(280)) { it / 7 } + fadeOut(tween(220)))
            }
        },
    ) { target ->
        when (val s = target) {
            Screen.Home -> HomeScreen(
                events = events,
                onAdd = { screen = Screen.Edit(null) },
                onOpen = { screen = Screen.Detail(it) },
            )

            is Screen.Detail -> {
                val event = events?.find { it.id == s.eventId }
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
                existing = s.eventId?.let { id -> events?.find { it.id == id } },
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
}
