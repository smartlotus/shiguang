package com.shiguang.app.ui.styles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shiguang.app.logic.EventUi
import com.shiguang.app.ui.Formats
import com.shiguang.app.ui.theme.EventPalette

/** 事件对应的面板主色 */
val EventUi.accent: Color
    get() = EventPalette[colorIndex.coerceIn(0, EventPalette.lastIndex)]

/** “2026年10月1日 · 周四 · 每年重复” */
fun EventUi.dateLine(): String =
    Formats.fullDate(target) + if (repeatYearly) " · 每年重复" else ""

/** 共用的“大数字”版式：前缀 + 衬线大数字 + 天 + 日期 */
@Composable
fun Headline(
    event: EventUi,
    modifier: Modifier = Modifier,
    numberSize: TextUnit = 56.sp,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = event.prefix,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = event.headlineAbs.toString(),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Light,
                fontSize = numberSize,
                lineHeight = numberSize,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "天",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 5.dp, bottom = 9.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = event.dateLine(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (event.note.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = event.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
