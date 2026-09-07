package com.shiguang.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseCountdownWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        WidgetUpdater.scheduleMidnight(context)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                WidgetUpdater.updateAll(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val editor = context.getSharedPreferences(WidgetUpdater.PREFS, Context.MODE_PRIVATE).edit()
        appWidgetIds.forEach { editor.remove(WidgetUpdater.eventIdPrefKey(it)) }
        editor.apply()
    }
}

class CountdownWidgetSmall : BaseCountdownWidget()
class CountdownWidgetMedium : BaseCountdownWidget()
