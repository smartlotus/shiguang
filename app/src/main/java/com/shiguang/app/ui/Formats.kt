package com.shiguang.app.ui

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object Formats {

    private val cnDate = DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA)
    private val shortDate = DateTimeFormatter.ofPattern("yyyy.M.d", Locale.CHINA)

    /** “2026年10月1日 · 周四” */
    fun fullDate(d: LocalDate): String =
        d.format(cnDate) + " · " + d.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINA)

    /** “2026.10.1”（小组件等紧凑场合） */
    fun short(d: LocalDate): String = d.format(shortDate)

    /** LocalDate ↔ DatePicker 的 UTC 毫秒 */
    fun toMillis(d: LocalDate): Long =
        d.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    fun fromMillis(millis: Long?): LocalDate? =
        millis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
}
