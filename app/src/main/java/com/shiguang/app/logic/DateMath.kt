package com.shiguang.app.logic

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min

/**
 * 纯日期计算逻辑（不依赖 Android），配合单元测试。
 *
 * 三类事件：
 *  - 倒计时：目标日在未来 → “还 N 天”，进度条剩余比例由长变短；
 *  - 纪念日：目标日在过去且不重复 → “已 N 天”，以“第一个 365 天”为一个周期填充；
 *  - 每年重复：头条数字显示距下一个周年的天数，进度按“上一个周年 → 下一个周年”循环。
 */
object DateMath {

    enum class Phase { COUNTDOWN, ANNIVERSARY }

    data class Progress(
        val phase: Phase,
        /** 0f..1f，一个周期内已流逝的比例 */
        val fraction: Float,
        /** 一个完整周期的总天数 */
        val totalDays: Long,
        /** 已流逝天数 */
        val elapsedDays: Long,
    ) {
        /** 剩余比例（倒计时时长条由此由长变短） */
        val remaining: Float get() = 1f - fraction
    }

    /** 目标日的“下一次发生”：不重复 → 目标日本身；每年重复 → 今天或以后的下一个周年 */
    fun nextOccurrence(target: LocalDate, today: LocalDate, repeatYearly: Boolean): LocalDate {
        if (!repeatYearly) return target
        val thisYear = target.withYear(today.year) // 2月29日会自动收敛为2月28日
        return if (thisYear.isBefore(today)) thisYear.plusYears(1) else thisYear
    }

    /** 头条数字（天）：重复 → 距下一个周年；未来 → 距目标日；过去不重复 → 目标日至今 */
    fun headlineDays(target: LocalDate, today: LocalDate, repeatYearly: Boolean): Long =
        if (repeatYearly) {
            ChronoUnit.DAYS.between(today, nextOccurrence(target, today, true))
        } else if (!target.isBefore(today)) {
            ChronoUnit.DAYS.between(today, target)
        } else {
            ChronoUnit.DAYS.between(target, today)
        }

    /** 当天是否就是目标日 / 周年当天 */
    fun isToday(target: LocalDate, today: LocalDate, repeatYearly: Boolean): Boolean =
        headlineDays(target, today, repeatYearly) == 0L

    fun progress(target: LocalDate, today: LocalDate, start: LocalDate, repeatYearly: Boolean): Progress = when {
        repeatYearly -> {
            val next = nextOccurrence(target, today, true)
            val prev = next.minusYears(1)
            val total = max(1L, ChronoUnit.DAYS.between(prev, next))
            val elapsed = ChronoUnit.DAYS.between(prev, today).coerceIn(0L, total)
            Progress(Phase.ANNIVERSARY, elapsed.toFloat() / total, total, elapsed)
        }
        target.isBefore(today) -> {
            // 纪念日：以“第一个 365 天”为一个周期填充
            val elapsed = ChronoUnit.DAYS.between(target, today)
            Progress(Phase.ANNIVERSARY, min(elapsed, 365L).toFloat() / 365f, 365L, elapsed)
        }
        else -> {
            // 倒计时：从起始日（默认为添加当天）到目标日
            val s = if (start.isBefore(target)) start else target
            val total = max(1L, ChronoUnit.DAYS.between(s, target))
            val elapsed = ChronoUnit.DAYS.between(s, today).coerceIn(0L, total)
            Progress(Phase.COUNTDOWN, elapsed.toFloat() / total, total, elapsed)
        }
    }

    /** 多米诺木棍根数：把任意天数映射到 8..40 根 */
    fun stickCount(totalDays: Long): Int = totalDays.coerceIn(8L, 40L).toInt()
}
