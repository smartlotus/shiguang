@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.shiguang.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shiguang.app.logic.EventUi
import com.shiguang.app.ui.styles.BarStyle
import com.shiguang.app.ui.styles.ClassicStyle
import com.shiguang.app.ui.styles.DominoStyle
import com.shiguang.app.ui.styles.RingStyle
import com.shiguang.app.ui.styles.accent

private val STYLE_NAMES = listOf("经典", "多米诺", "长条", "圆环")

/** 详情页：整页染色 + 大幅展示当前样式，底部可切换四种样式 */
@Composable
fun DetailScreen(
    event: EventUi,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onStyleChange: (Int) -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val bg by animateColorAsState(
        targetValue = event.accent.copy(alpha = if (isDark) 0.12f else 0.18f),
        label = "detailBg",
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "编辑")
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = event.styleIndex,
                label = "style",
                transitionSpec = { fadeIn(androidx.compose.animation.core.tween(350)) togetherWith fadeOut(androidx.compose.animation.core.tween(350)) },
            ) { styleIndex ->
                when (styleIndex) {
                    1 -> DominoStyle(event, Modifier.fillMaxWidth())
                    2 -> BarStyle(event, Modifier.fillMaxWidth())
                    3 -> RingStyle(event, Modifier.fillMaxWidth())
                    else -> ClassicStyle(event, Modifier.fillMaxWidth())
                }
            }
        }

        SingleChoiceSegmentedButtonRow(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
        ) {
            STYLE_NAMES.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = event.styleIndex == index,
                    onClick = { onStyleChange(index) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = STYLE_NAMES.size),
                ) { Text(label) }
            }
        }
    }
}
