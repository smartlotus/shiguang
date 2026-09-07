package com.shiguang.app.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.shiguang.app.MainActivity
import com.shiguang.app.R
import com.shiguang.app.data.AppDatabase
import com.shiguang.app.data.EventEntity
import com.shiguang.app.logic.DateMath
import com.shiguang.app.ui.Formats
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt

/** 小组件的统一更新逻辑：读取“每个小组件 → 事件”的绑定，渲染 RemoteViews */
object WidgetUpdater {

    const val PREFS = "widget_prefs"
    const val ACTION_MIDNIGHT = "com.shiguang.app.action.MIDNIGHT"
    private const val MIDNIGHT_REQUEST = 2001

    fun eventIdPrefKey(appWidgetId: Int) = "event_$appWidgetId"

    private val BG_DRAWABLES = listOf(
        R.drawable.widget_bg_0,
        R.drawable.widget_bg_1,
        R.drawable.widget_bg_2,
        R.drawable.widget_bg_3,
        R.drawable.widget_bg_4,
        R.drawable.widget_bg_5,
        R.drawable.widget_bg_6,
        R.drawable.widget_bg_7,
    )

    private fun openAppPendingIntent(context: Context): PendingIntent = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    /** 刷新全部已添加的小组件 */
    suspend fun updateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val events = AppDatabase.get(context).eventDao().observeAll().first().associateBy { it.id }
        val providers = listOf(
            ComponentName(context, CountdownWidgetSmall::class.java) to false,
            ComponentName(context, CountdownWidgetMedium::class.java) to true,
        )
        for ((provider, medium) in providers) {
            val ids = manager.getAppWidgetIds(provider) ?: continue
            for (id in ids) {
                val eventId = prefs.getLong(eventIdPrefKey(id), -1L)
                manager.updateAppWidget(id, buildViews(context, events[eventId], medium))
            }
        }
    }

    /** 配置页选定后立即渲染单个小组件 */
    suspend fun updateOne(context: Context, appWidgetId: Int, medium: Boolean, eventId: Long) {
        val manager = AppWidgetManager.getInstance(context)
        val entity = AppDatabase.get(context).eventDao().byId(eventId)
        manager.updateAppWidget(appWidgetId, buildViews(context, entity, medium))
    }

    fun buildViews(context: Context, entity: EventEntity?, medium: Boolean): RemoteViews {
        val layout = if (medium) R.layout.widget_medium else R.layout.widget_small
        val views = RemoteViews(context.packageName, layout)
        views.setOnClickPendingIntent(R.id.w_root, openAppPendingIntent(context))

        if (entity == null) {
            views.setInt(R.id.w_root, "setBackgroundResource", R.drawable.widget_bg_0)
            views.setTextViewText(R.id.w_title, "拾光")
            views.setTextViewText(R.id.w_prefix, "")
            views.setTextViewText(R.id.w_days, "--")
            views.setTextViewText(R.id.w_date, "长按小组件重新选择日子")
            if (medium) views.setViewVisibility(R.id.w_progress, View.INVISIBLE)
            return views
        }

        val today = LocalDate.now()
        val target = LocalDate.ofEpochDay(entity.targetEpochDay)
        val repeat = entity.repeatYearly
        val headline = DateMath.headlineDays(target, today, repeat)
        val prefix = when {
            DateMath.isToday(target, today, repeat) -> "今天"
            repeat || !target.isBefore(today) -> "还有"
            else -> "已经"
        }
        val startDate = entity.startEpochDay?.let(LocalDate::ofEpochDay)
            ?: Instant.ofEpochMilli(entity.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
        val progress = DateMath.progress(target, today, startDate, repeat)
        val fraction = (if (progress.phase == DateMath.Phase.COUNTDOWN) progress.remaining else progress.fraction)
            .coerceIn(0f, 1f)

        views.apply {
            setInt(R.id.w_root, "setBackgroundResource", BG_DRAWABLES[entity.colorIndex.coerceIn(0, 7)])
            setTextViewText(R.id.w_title, entity.title)
            setTextViewText(R.id.w_prefix, prefix)
            setTextViewText(R.id.w_days, headline.toString())
            setTextViewText(R.id.w_date, Formats.short(target) + if (repeat) " · 每年" else "")
            if (medium) {
                setViewVisibility(R.id.w_progress, View.VISIBLE)
                setProgressBar(R.id.w_progress, 100, (fraction * 100).roundToInt(), false)
            }
        }
        return views
    }

    /** 安排在下一个午夜刷新（倒计时翻天的时刻） */
    fun scheduleMidnight(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context, MIDNIGHT_REQUEST,
            Intent(context, MidnightReceiver::class.java).setAction(ACTION_MIDNIGHT),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val nextMidnight = LocalDate.now().plusDays(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextMidnight, pendingIntent)
    }
}
