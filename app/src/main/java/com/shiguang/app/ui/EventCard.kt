package com.shiguang.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiguang.app.logic.EventUi
import com.shiguang.app.ui.styles.accent

/** 首页列表卡片：色点 + 标题 + 大数字 + 迷你进度条 */
@Composable
fun EventCard(event: EventUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val accent = event.accent
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = 12.sp, color = onSurfaceVariant)) {
                            append(if (event.isToday) "今天 " else "${event.shortPrefix} ")
                        }
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Medium,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        ) { append(event.headlineAbs.toString()) }
                        withStyle(SpanStyle(fontSize = 12.sp, color = onSurfaceVariant)) { append(" 天") }
                    },
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = Formats.fullDate(event.target) + if (event.repeatYearly) " · 每年" else "",
                style = MaterialTheme.typography.labelSmall,
                color = onSurfaceVariant,
            )
            if (event.note.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = event.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(14.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(accent.copy(alpha = 0.16f))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(event.displayFraction.coerceIn(0.03f, 1f))
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(accent)
                )
            }
        }
    }
}
