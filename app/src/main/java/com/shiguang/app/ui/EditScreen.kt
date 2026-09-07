@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.shiguang.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.shiguang.app.logic.EventUi
import com.shiguang.app.ui.theme.EventPalette
import com.shiguang.app.ui.theme.PaletteNames
import java.time.LocalDate

/** 新建 / 编辑一个日子 */
@Composable
fun EditScreen(
    existing: EventUi?,
    onSave: (
        title: String,
        targetEpochDay: Long,
        startEpochDay: Long?,
        repeatYearly: Boolean,
        colorIndex: Int,
        note: String,
    ) -> Unit,
    onDelete: (() -> Unit)?,
    onBack: () -> Unit,
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var target by remember { mutableStateOf(existing?.target ?: LocalDate.now()) }
    var start by remember { mutableStateOf(existing?.startEpochDay?.let(LocalDate::ofEpochDay)) }
    var repeat by remember { mutableStateOf(existing?.repeatYearly ?: false) }
    var colorIndex by remember { mutableIntStateOf(existing?.colorIndex ?: 0) }
    var note by remember { mutableStateOf(existing?.note ?: "") }

    var showTargetPicker by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "添加日子" else "编辑日子") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            SectionLabel("名字")
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("例如：和小陈在一起 / 生日 / 考试日") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            SectionLabel("哪一天")
            FieldRow(
                text = Formats.fullDate(target),
                hint = if (repeat) "每年重复" else if (target >= LocalDate.now()) "倒计时" else "纪念日",
                onClick = { showTargetPicker = true },
            )

            SectionLabel("进度的起点（可选）")
            if (start == null) {
                FieldRow(
                    text = "从添加当天开始",
                    hint = "设置",
                    onClick = { showStartPicker = true },
                )
            } else {
                FieldRow(
                    text = Formats.fullDate(start!!),
                    hint = "清除",
                    hintIcon = Icons.Default.Close,
                    onClick = { start = null },
                )
            }
            Text(
                text = "长条与圆环样式会从这个日子开始计算进度。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 6.dp, top = 6.dp),
            )

            SectionLabel("颜色")
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                EventPalette.forEachIndexed { index, color ->
                    val selected = colorIndex == index
                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(color)
                            .semantics { contentDescription = PaletteNames[index] }
                            .then(
                                if (selected) Modifier.border(
                                    2.dp, MaterialTheme.colorScheme.onBackground, CircleShape
                                ) else Modifier
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("每年重复", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "生日、纪念日这类日子，每年都会轮到",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = repeat, onCheckedChange = { repeat = it })
                }
            }

            SectionLabel("备注（可选）")
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("一句话，写在日子里") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(28.dp))
            Button(
                onClick = {
                    onSave(
                        title.trim(),
                        target.toEpochDay(),
                        start?.toEpochDay(),
                        repeat,
                        colorIndex,
                        note.trim(),
                    )
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text(if (existing == null) "保存" else "保存修改", style = MaterialTheme.typography.titleMedium)
            }

            if (onDelete != null) {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("删除这个日子", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showTargetPicker) {
        DatePickerController(
            initial = target,
            title = "选择目标日期",
            onDismiss = { showTargetPicker = false },
            onConfirm = {
                target = it
                showTargetPicker = false
            },
        )
    }
    if (showStartPicker) {
        DatePickerController(
            initial = start ?: LocalDate.now(),
            title = "选择进度起点",
            onDismiss = { showStartPicker = false },
            onConfirm = {
                start = it
                showStartPicker = false
            },
        )
    }
    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除「${existing?.title}」？") },
            text = { Text("删除后无法恢复，对应的小组件也会变回未选择状态。") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun DatePickerController(
    initial: LocalDate,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = Formats.toMillis(initial))
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                Formats.fromMillis(state.selectedDateMillis)?.let { onConfirm(it) } ?: onDismiss()
            }) { Text("确定") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    ) {
        DatePicker(
            state = state,
            title = {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                )
            },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 6.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun FieldRow(
    text: String,
    hint: String,
    onClick: () -> Unit,
    hintIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            Modifier.padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            if (hintIcon != null) {
                Icon(hintIcon, contentDescription = hint, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(hint, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
