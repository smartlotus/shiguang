package com.shiguang.app.logic

import com.shiguang.app.data.EventEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

/** 展示层模型：把派生数据（天数、进度、文案前缀）一次性算好 */
data class EventUi(
    val id: Long,
    val title: String,
    val target: LocalDate,
    val startEpochDay: Long?,
    val repeatYearly: Boolean,
    val colorIndex: Int,
    val styleIndex: Int,
    val note: String,
    val headline: Long,
    val isToday: Boolean,
    val isFuture: Boolean,
    val progress: DateMath.Progress,
) {
    /** 头条前缀：还 / 已 / 今天 */
    val prefix: String
        get() = when {
            isToday -> "就是今天"
            repeatYearly || isFuture -> "还有"
            else -> "已经"
        }

    /** 紧凑前缀（卡片用）：还 / 已 / 今天 */
    val shortPrefix: String
        get() = when {
            isToday -> "今天"
            repeatYearly || isFuture -> "还"
            else -> "已"
        }

    /** 长条/圆环显示比例：倒计时显示剩余（由长变短），纪念日显示已流逝（逐渐填满） */
    val displayFraction: Float
        get() = if (progress.phase == DateMath.Phase.COUNTDOWN) progress.remaining else progress.fraction

    val headlineAbs: Long get() = abs(headline)

    companion object {
        fun from(e: EventEntity, today: LocalDate): EventUi {
            val target = LocalDate.ofEpochDay(e.targetEpochDay)
            val createdDate = Instant.ofEpochMilli(e.createdAt)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            val start = e.startEpochDay?.let(LocalDate::ofEpochDay) ?: createdDate
            return EventUi(
                id = e.id,
                title = e.title,
                target = target,
                startEpochDay = e.startEpochDay,
                repeatYearly = e.repeatYearly,
                colorIndex = e.colorIndex.coerceIn(0, 7),
                styleIndex = e.styleIndex.coerceIn(0, 3),
                note = e.note,
                headline = DateMath.headlineDays(target, today, e.repeatYearly),
                isToday = DateMath.isToday(target, today, e.repeatYearly),
                isFuture = !target.isBefore(today),
                progress = DateMath.progress(target, today, start, e.repeatYearly),
            )
        }
    }
}
