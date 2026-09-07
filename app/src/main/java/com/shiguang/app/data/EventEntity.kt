package com.shiguang.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    /** 目标日期（当天 0 点）的 epochDay */
    val targetEpochDay: Long,
    /** 进度条的起始日 epochDay；null 表示用添加当天的日期 */
    val startEpochDay: Long? = null,
    /** 每年重复（生日、纪念日等） */
    val repeatYearly: Boolean = false,
    /** 预设色板下标 0..7 */
    val colorIndex: Int = 0,
    /** 展示样式 0 经典 / 1 多米诺 / 2 长条 / 3 圆环 */
    val styleIndex: Int = 0,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)
