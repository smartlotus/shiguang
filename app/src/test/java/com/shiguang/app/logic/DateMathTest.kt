package com.shiguang.app.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateMathTest {

    private val today: LocalDate = LocalDate.of(2026, 9, 7)

    // ---- 头条数字 ----

    @Test
    fun countdown_future_days() {
        assertEquals(24L, DateMath.headlineDays(LocalDate.of(2026, 10, 1), today, false))
    }

    @Test
    fun countdown_today_is_zero() {
        assertEquals(0L, DateMath.headlineDays(today, today, false))
        assertTrue(DateMath.isToday(today, today, false))
    }

    @Test
    fun anniversary_elapsed_days() {
        assertEquals(366L, DateMath.headlineDays(LocalDate.of(2025, 9, 6), today, false))
        assertFalse(DateMath.isToday(LocalDate.of(2025, 9, 6), today, false))
    }

    @Test
    fun repeat_next_anniversary() {
        assertEquals(
            LocalDate.of(2026, 12, 25),
            DateMath.nextOccurrence(LocalDate.of(2020, 12, 25), today, true)
        )
        // 今年周年已过 → 算明年
        assertEquals(
            LocalDate.of(2027, 3, 8),
            DateMath.nextOccurrence(LocalDate.of(2020, 3, 8), today, true)
        )
    }

    @Test
    fun repeat_today_is_anniversary() {
        assertEquals(0L, DateMath.headlineDays(LocalDate.of(2019, 9, 7), today, true))
        assertTrue(DateMath.isToday(LocalDate.of(2019, 9, 7), today, true))
    }

    @Test
    fun repeat_feb29_collapses_to_feb28() {
        val next = DateMath.nextOccurrence(LocalDate.of(2024, 2, 29), LocalDate.of(2026, 9, 7), true)
        assertEquals(LocalDate.of(2027, 2, 28), next)
    }

    // ---- 进度 ----

    @Test
    fun progress_countdown_shrinks() {
        val p = DateMath.progress(
            target = LocalDate.of(2026, 10, 1),
            today = LocalDate.of(2026, 9, 16),
            start = LocalDate.of(2026, 9, 1),
            repeatYearly = false
        )
        assertEquals(DateMath.Phase.COUNTDOWN, p.phase)
        assertEquals(0.5f, p.fraction, 0.0001f)
        assertEquals(0.5f, p.remaining, 0.0001f)
        assertEquals(30L, p.totalDays)
        assertEquals(15L, p.elapsedDays)
    }

    @Test
    fun progress_countdown_clamps_when_today_before_start() {
        // 起始日在今天之后（例如手动改过）：比例不应为负
        val p = DateMath.progress(
            target = LocalDate.of(2026, 10, 1),
            today = LocalDate.of(2026, 9, 7),
            start = LocalDate.of(2026, 9, 20),
            repeatYearly = false
        )
        assertEquals(0f, p.fraction, 0.0001f)
        assertEquals(1f, p.remaining, 0.0001f)
    }

    @Test
    fun progress_anniversary_fills_first_365() {
        val p = DateMath.progress(
            target = LocalDate.of(2026, 3, 1),
            today = today,
            start = today,
            repeatYearly = false
        )
        assertEquals(DateMath.Phase.ANNIVERSARY, p.phase)
        assertEquals(190f / 365f, p.fraction, 0.0001f)
    }

    @Test
    fun progress_anniversary_caps_at_365() {
        val p = DateMath.progress(
            target = LocalDate.of(2020, 1, 1),
            today = today,
            start = today,
            repeatYearly = false
        )
        assertEquals(1f, p.fraction, 0.0001f)
    }

    @Test
    fun progress_repeat_uses_yearly_cycle() {
        val p = DateMath.progress(
            target = LocalDate.of(2020, 12, 25),
            today = today,
            start = today,
            repeatYearly = true
        )
        // 上一个周年 2025-12-25 → 下一个周年 2026-12-25，今天走了 256 天
        assertEquals(DateMath.Phase.ANNIVERSARY, p.phase)
        assertEquals(256f / 365f, p.fraction, 0.01f)
    }

    // ---- 多米诺 ----

    @Test
    fun stick_count_mapping() {
        assertEquals(8, DateMath.stickCount(3))
        assertEquals(40, DateMath.stickCount(1000))
        assertEquals(20, DateMath.stickCount(20))
    }
}
