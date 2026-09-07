package com.shiguang.app.ui.styles

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp
import com.shiguang.app.logic.EventUi

/** 经典样式：衬线大数字，入场时轻微缩放浮现 */
@Composable
fun ClassicStyle(event: EventUi, modifier: Modifier = Modifier) {
    val appear = remember(event.id) { Animatable(0f) }
    LaunchedEffect(event.id) {
        appear.animateTo(1f, tween(durationMillis = 550, easing = FastOutSlowInEasing))
    }
    Box(
        modifier.graphicsLayer {
            alpha = appear.value
            val scale = 0.94f + 0.06f * appear.value
            scaleX = scale
            scaleY = scale
        }
    ) {
        Headline(event, modifier = Modifier.fillMaxWidth(), numberSize = 88.sp)
    }
}
