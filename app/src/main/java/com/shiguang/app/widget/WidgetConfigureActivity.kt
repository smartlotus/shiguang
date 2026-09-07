@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.shiguang.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.shiguang.app.MainActivity
import com.shiguang.app.data.AppDatabase
import com.shiguang.app.logic.EventUi
import com.shiguang.app.ui.EventCard
import com.shiguang.app.ui.theme.ShiGuangTheme
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** 添加小组件时弹出：选择要展示哪个日子 */
class WidgetConfigureActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val manager = AppWidgetManager.getInstance(this)
        val medium = manager.getAppWidgetInfo(appWidgetId)?.provider ==
            ComponentName(this, CountdownWidgetMedium::class.java)

        setContent {
            ShiGuangTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    PickEventScreen(
                        onPick = { eventId ->
                            lifecycleScope.launchWhenCreated {
                                getSharedPreferences(WidgetUpdater.PREFS, MODE_PRIVATE)
                                    .edit()
                                    .putLong(WidgetUpdater.eventIdPrefKey(appWidgetId), eventId)
                                    .apply()
                                WidgetUpdater.updateOne(this@WidgetConfigureActivity, appWidgetId, medium, eventId)
                                setResult(
                                    RESULT_OK,
                                    Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                )
                                finish()
                            }
                        },
                        onOpenApp = {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PickEventScreen(onPick: (Long) -> Unit, onOpenApp: () -> Unit) {
    val context = LocalContext.current
    val events by produceState<List<EventUi>>(emptyList(), context) {
        AppDatabase.get(context).eventDao().observeAll()
            .map { list -> list.map { EventUi.from(it, LocalDate.now()) } }
            .collect { value = it }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("选择要展示的日子") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        if (events.isEmpty()) {
            Box(
                Modifier.padding(padding).fillMaxSize().padding(horizontal = 40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("还没有可展示的日子", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "先在应用里添加一个纪念日或倒计时",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                    )
                    TextButton(onClick = onOpenApp) { Text("打开拾光") }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(events, key = { it.id }) { event ->
                    EventCard(event, onClick = { onPick(event.id) })
                }
            }
        }
    }
}
