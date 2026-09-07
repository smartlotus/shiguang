package com.shiguang.app.ui.styles

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.shiguang.app.logic.EventUi

/** 经典样式：一枚安静的衬线大数字 */
@Composable
fun ClassicStyle(event: EventUi, modifier: Modifier = Modifier) {
    Headline(event, modifier = modifier, numberSize = 88.sp)
}
