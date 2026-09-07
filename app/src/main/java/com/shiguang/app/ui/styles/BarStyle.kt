package com.shiguang.app.ui.styles

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiguang.app.logic.DateMath
import com.shiguang.app.logic.EventUi
import com.shiguang.app.ui.Formats
import java.time.LocalDate

/**
 * 长条样式：一根长条由长慢慢变短（倒计时），或者由短慢慢填满（纪念日）。
 * 打开页面时会从“满长”动画收缩到当前比例。
 */
@Composable
fun BarStyle(event: EventUi, modifier: Modifier = Modifier) {
    val accent = event.accent
    val target = event.displayFraction.coerceIn(0f, 1f)

    val width = remember(event.id) { Animatable(0f) }
    LaunchedEffect(event.id) {
        width.snapTo(1f)
        width.animateTo(target, tween(durationMillis = 1800, easing = FastOutSlowInEasing))
    }

    val today = LocalDate.now()
    val next = if (event.repeatYearly) DateMath.nextOccurrence(event.target, today, true) else event.target
    val (leftLabel, rightLabel) = when {
        event.repeatYearly -> Formats.short(next.minusYears(1)) to Formats.short(next)
        event.isFuture -> (
            event.startEpochDay?.let { Formats.short(LocalDate.ofEpochDay(it)) } ?: "起点"
            ) to Formats.short(event.target)
        else -> Formats.short(event.target) to "今天"
    }

    Column(modifier.fillMaxWidth().padding(horizontal = 36.dp)) {
        Headline(event, modifier = Modifier.fillMaxWidth(), numberSize = 52.sp)
        Spacer(Modifier.height(44.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accent.copy(alpha = 0.16f))
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(width.value.coerceIn(0.02f, 1f))
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(leftLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(rightLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
