package com.shiguang.app.ui.styles

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiguang.app.logic.DateMath
import com.shiguang.app.logic.EventUi
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 多米诺样式：每一根竖立的小木棍代表一段日子。
 * 时间流逝，木棍一根接一根地倒下；进入页面时会重放一次“倒下”的过程。
 * 倒计时 → 剩下的木棍站立的越来越少；纪念日 → 倒下的木棍越积越多。
 */
@Composable
fun DominoStyle(event: EventUi, modifier: Modifier = Modifier) {
    val accent = event.accent
    val sticks = DateMath.stickCount(event.progress.totalDays)
    val elapsedFraction = event.progress.fraction.coerceIn(0f, 1f)

    // 进场动画：把“当前已倒下”的部分，一次性按次序重放出来
    val reveal = remember(event.id) { Animatable(0f) }
    LaunchedEffect(event.id) {
        reveal.animateTo(
            1f,
            tween(durationMillis = 2400, easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f))
        )
    }
    // 下一根即将倒下的木棍：轻微摇摆蓄力
    val swayAngle by rememberInfiniteTransition(label = "sway").animateFloat(
        initialValue = -2.6f,
        targetValue = 2.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "swayAngle",
    )

    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(230.dp)
                .padding(horizontal = 20.dp)
        ) {
            val n = sticks
            val stickLen = size.height * 0.5f
            val stickW = stickLen * 0.16f
            val gap = min(stickLen * 1.04f, (size.width - stickW) / (n - 1).coerceAtLeast(1))
            val firstX = (size.width - gap * (n - 1)) / 2f
            val baseY = size.height * 0.8f

            // 地面
            drawLine(
                color = accent.copy(alpha = 0.28f),
                start = Offset(firstX - gap * 0.7f, baseY),
                end = Offset(firstX + gap * (n - 1) + gap * 0.7f, baseY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )

            val nextToFall = (elapsedFraction * n).toInt().coerceAtMost(n - 1)

            for (i in 0 until n) {
                val fallen = (reveal.value * elapsedFraction * n - i).coerceIn(0f, 1f)
                val cx = firstX + i * gap
                val sway = if (reveal.value >= 1f && elapsedFraction < 1f && i == nextToFall && fallen <= 0f) {
                    swayAngle
                } else {
                    0f
                }
                rotate(degrees = fallen * 76f + sway, pivot = Offset(cx, baseY)) {
                    drawRoundRect(
                        color = if (fallen > 0f) accent else accent.copy(alpha = 0.22f),
                        topLeft = Offset(cx - stickW / 2f, baseY - stickLen),
                        size = Size(stickW, stickLen),
                        cornerRadius = CornerRadius(stickW / 2f, stickW / 2f),
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Headline(event, numberSize = 44.sp)
        Spacer(Modifier.height(10.dp))
        val fallenCount = (elapsedFraction * sticks).roundToInt()
        Text(
            text = "木棍已倒下 $fallenCount / $sticks 根",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
