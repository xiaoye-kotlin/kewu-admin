package com.kewu.admin

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import java.text.NumberFormat
import java.util.Locale

val AdminJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    prettyPrint = true
}

fun JsonObject?.str(key: String, fallback: String = "—"): String {
    val value = this?.get(key) as? JsonPrimitive ?: return fallback
    if (value is JsonNull) return fallback
    return value.contentOrNull?.takeIf { it.isNotBlank() } ?: fallback
}

fun JsonObject?.long(key: String, fallback: Long = 0L): Long {
    val primitive = this?.get(key) as? JsonPrimitive ?: return fallback
    return primitive.longOrNull ?: primitive.content.toLongOrNull()
        ?: primitive.doubleOrNull?.toLong() ?: fallback
}

fun JsonObject?.double(key: String, fallback: Double = 0.0): Double {
    val primitive = this?.get(key) as? JsonPrimitive ?: return fallback
    return primitive.doubleOrNull ?: primitive.content.toDoubleOrNull() ?: fallback
}

fun JsonObject?.bool(key: String, fallback: Boolean = false): Boolean {
    val primitive = this?.get(key) as? JsonPrimitive ?: return fallback
    return primitive.booleanOrNull ?: primitive.content.equals("true", true) || fallback
}

fun JsonObject?.obj(key: String): JsonObject? = this?.get(key) as? JsonObject

fun JsonObject?.array(key: String): List<JsonObject> =
    (this?.get(key) as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }

fun JsonElement?.asObjectList(): List<JsonObject> = when (this) {
    is JsonArray -> mapNotNull { it as? JsonObject }
    is JsonObject -> (this["items"] as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }
    else -> emptyList()
}

fun formatCount(value: Long): String = NumberFormat.getIntegerInstance(Locale.CHINA).format(value)

fun formatBytes(value: Long): String {
    if (value <= 0L) return "0 B"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var size = value.toDouble()
    var index = 0
    while (size >= 1024.0 && index < units.lastIndex) {
        size /= 1024.0
        index++
    }
    return if (index == 0) "${size.toLong()} ${units[index]}" else "%.1f %s".format(size, units[index])
}

fun formatPercent(value: Double): String = "${(value * 100).coerceIn(0.0, 100.0).toInt()}%"

fun roleLabel(role: String): String = when (role.uppercase()) {
    "ADMIN" -> "管理员"
    "TEACHER" -> "教师"
    "STUDENT" -> "学生"
    else -> role.ifBlank { "未知" }
}

fun statusLabel(status: String): String = when (status.uppercase()) {
    "ACTIVE" -> "正常"
    "DISABLED" -> "已禁用"
    "PENDING" -> "待批阅"
    "REVIEWED" -> "已批阅"
    "RETURNED" -> "已打回"
    "OPEN" -> "进行中"
    "CLOSED" -> "已结束"
    else -> status.ifBlank { "未知" }
}

data class PageResult(
    val items: List<JsonObject>,
    val total: Int,
    val page: Int,
    val size: Int,
)

data class AdminSession(
    val token: String,
    val username: String,
    val nickname: String,
)
