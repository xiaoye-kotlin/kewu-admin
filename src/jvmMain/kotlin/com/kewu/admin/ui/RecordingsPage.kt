package com.kewu.admin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kewu.admin.AdminApi
import com.kewu.admin.PageResult
import com.kewu.admin.double
import com.kewu.admin.roleLabel
import com.kewu.admin.str
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import java.awt.Desktop
import java.net.URI

@Composable
fun RecordingsPage(api: AdminApi, notify: (String) -> Unit) {
    var result by remember { mutableStateOf(PageResult(emptyList(), 0, 1, 20)) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var page by remember { mutableStateOf(1) }
    var keyword by remember { mutableStateOf("") }
    var classId by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<JsonObject?>(null) }
    var deleteTarget by remember { mutableStateOf<JsonObject?>(null) }
    var working by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun reload() {
        loading = true
        error = null
        try {
            result = api.page(
                "/api/admin/recordings",
                mapOf("page" to page.toString(), "size" to "20", "keyword" to keyword, "classId" to classId, "role" to role),
            )
        } catch (e: Exception) {
            error = e.message ?: "录音列表加载失败"
        } finally {
            loading = false
        }
    }
    LaunchedEffect(page, role) { reload() }

    Column(Modifier.fillMaxSize()) {
        PageHeader("录音资源", "查看课堂录音质量，试听、下载或软删除录音") {
            OutlinedButton(onClick = { scope.launch { reload() } }) { Icon(Icons.Outlined.Refresh, null); Spacer(Modifier.width(6.dp)); Text("刷新") }
        }
        Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SearchField(keyword, { keyword = it }, "搜索学生姓名", Modifier.weight(1f))
            SearchField(classId, { classId = it }, "班级 / 课堂 ID", Modifier.weight(1f))
            OptionSelector(role, listOf("" to "全部来源", "teacher" to "教师端", "student" to "学生端")) { role = it; page = 1 }
            Button(onClick = { page = 1; scope.launch { reload() } }, modifier = Modifier.height(48.dp)) { Text("查询") }
        }
        when {
            loading && result.items.isEmpty() -> LoadingBlock()
            error != null && result.items.isEmpty() -> ErrorBlock(error ?: "加载失败") { scope.launch { reload() } }
            result.items.isEmpty() -> EmptyBlock("暂无录音", "上传并落库的录音会显示在这里")
            else -> DataTable(
                result.items,
                listOf(TableColumn("录音 ID", 1.4f), TableColumn("学生 / 设备", 1.2f), TableColumn("班级", 1f), TableColumn("来源", .7f), TableColumn("质量分", .7f), TableColumn("时长", .7f), TableColumn("上传时间", 1.2f), TableColumn("操作", .6f)),
                Modifier.weight(1f),
                key = { it.str("id", it.str("recordingId", "")) },
                onRowClick = { selected = it },
            ) { recording, column ->
                when (column.title) {
                    "录音 ID" -> TableText(recording.str("id", recording.str("recordingId")), bold = true)
                    "学生 / 设备" -> TableText(recording.str("studentName", recording.str("deviceId")))
                    "班级" -> TableText(recording.str("classId", recording.str("courseId")))
                    "来源" -> StatusPill(roleLabel(recording.str("role", "")), if (recording.str("role", "") == "teacher") StatusTone.INFO else StatusTone.NEUTRAL)
                    "质量分" -> TableText("${(recording.double("score") * if (recording.double("score") <= 1.0) 100 else 1).toInt()} 分")
                    "时长" -> TableText("${recording.double("durationSec").toInt()} 秒")
                    "上传时间" -> TableText(recording.str("createdAt", recording.str("storedAt")))
                    else -> TextButton(onClick = { selected = recording }) { Text("详情") }
                }
            }
        }
        PaginationBar(result.page, result.size, result.total) { page = it }
    }

    selected?.let { item ->
        RecordingDetailDialog(api, item, onDismiss = { selected = null }, onDelete = { deleteTarget = item })
    }
    deleteTarget?.let { item ->
        ConfirmDialog("删除录音", "确定删除录音 ${item.str("id", item.str("recordingId"))} 吗？列表将隐藏该记录，OBS 原文件保留以便追溯。", "确认删除", true,
            onConfirm = {
                scope.launch {
                    working = true
                    try {
                        api.remove("/api/admin/recordings/${item.str("id", item.str("recordingId"))}")
                        deleteTarget = null
                        selected = null
                        notify("录音已删除")
                        reload()
                    } catch (e: Exception) {
                        notify(e.message ?: "删除失败")
                    } finally {
                        working = false
                    }
                }
            },
            onDismiss = { if (!working) deleteTarget = null },
        )
    }
}

@Composable
private fun RecordingDetailDialog(api: AdminApi, item: JsonObject, onDismiss: () -> Unit, onDelete: () -> Unit) {
    var detail by remember { mutableStateOf(item) }
    val id = item.str("id", item.str("recordingId"))
    LaunchedEffect(id) {
        runCatching { api.detail("/api/admin/recordings/$id") }.onSuccess { detail = it }
    }
    val url = detail.str("playUrl", detail.str("url", detail.str("storedUrl", "")))
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("录音详情") },
        text = {
            Column {
                DetailRow("录音 ID", detail.str("id", detail.str("recordingId")))
                DetailRow("学生", detail.str("studentName"))
                DetailRow("班级 / 课程", "${detail.str("classId")} / ${detail.str("courseId")}")
                DetailRow("会话 ID", detail.str("sessionId"))
                DetailRow("设备", detail.str("deviceId"))
                DetailRow("来源", roleLabel(detail.str("role", "")))
                DetailRow("质量分", "${detail.double("score")}")
                DetailRow("平均电平", "${detail.str("avgLevelDb")} dB")
                DetailRow("信噪比", "${detail.str("snrDb")} dB")
                DetailRow("时长", "${detail.double("durationSec")} 秒")
                DetailRow("格式", detail.str("format"))
                DetailRow("上传时间", detail.str("createdAt", detail.str("storedAt")))
                if (url.isNotBlank()) {
                    Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { openUrl(url) }) { Text("试听") }
                        OutlinedButton(onClick = {
                            scope.launch {
                                val downloadUrl = runCatching {
                                    api.detail("/api/admin/recordings/$id/download").str("url", url)
                                }.getOrDefault(url)
                                if (downloadUrl.isNotBlank()) openUrl(downloadUrl)
                            }
                        }) { Text("下载") }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
        dismissButton = { TextButton(onClick = onDelete) { Text("删除录音", color = MaterialTheme.colorScheme.error) } },
    )
}

private fun openUrl(url: String) {
    runCatching {
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(URI(url))
    }
}
