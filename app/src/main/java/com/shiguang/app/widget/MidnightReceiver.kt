package com.shiguang.app.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** 每天零点被 AlarmManager 唤醒，刷新所有小组件并预约下一个午夜 */
class MidnightReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                WidgetUpdater.updateAll(context)
                WidgetUpdater.scheduleMidnight(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
