package com.kewu.admin.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.kewu.admin.AdminApi
import com.kewu.admin.AdminSession
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(api: AdminApi, onLogin: (AdminSession) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun submit() {
        if (username.isBlank() || password.isBlank()) {
            error = "请输入管理员账号和密码"
            return
        }
        scope.launch {
            loading = true
            error = null
            try {
                onLogin(api.login(username.trim(), password))
            } catch (e: Exception) {
                error = e.message ?: "登录失败，请稍后重试"
            } finally {
                loading = false
            }
        }
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(KewuDeepBlue, KewuBlue, Color(0xFF0E7E9B))),
        ),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.width(440.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 18.dp),
        ) {
            Column(Modifier.padding(40.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(52.dp).background(KewuBlue, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.AdminPanelSettings, null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("课悟总控端", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("KEWU ADMIN CONSOLE", color = AdminMuted, style = MaterialTheme.typography.labelMedium)
                    }
                }
                Spacer(Modifier.height(36.dp))
                Text("管理员登录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("集中监控服务状态并管理平台数据", color = AdminMuted, modifier = Modifier.padding(top = 6.dp))
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("管理员账号") },
                    leadingIcon = { Icon(Icons.Outlined.Person, null) },
                    singleLine = true,
                    enabled = !loading,
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("密码") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !loading,
                )
                if (error != null) {
                    Text(
                        error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = ::submit,
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text("进入管理控制台", fontWeight = FontWeight.SemiBold)
                }
                Text(
                    "仅限 ADMIN 角色账号 · 凭证仅保存在本次运行内存中",
                    color = AdminMuted,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 18.dp),
                )
            }
        }
    }
}
