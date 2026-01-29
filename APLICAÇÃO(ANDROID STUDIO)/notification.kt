package com.example.social_rede_mobile.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.social_rede_mobile.data.Notification
import com.example.social_rede_mobile.data.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class NotificationType {
    Like, Follow, Comment, System, Reminder
}

@Composable
fun NotificationScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("buzz_prefs", Context.MODE_PRIVATE)
    val loggedInUsername = prefs.getString("username", "") ?: ""

    val viewModel: NotificationViewModel = viewModel(factory = viewModelFactory {
        initializer { NotificationViewModel(context.applicationContext as android.app.Application) }
    })

    LaunchedEffect(loggedInUsername) {
        if (loggedInUsername.isNotBlank()) {
            viewModel.refreshNotifications(loggedInUsername)
        }
    }

    val notifications by viewModel.notifications.collectAsState()

    val userNotifications = notifications.filter { it.targetUsername == loggedInUsername }

    val today = userNotifications.filter {
        val todayCal = Calendar.getInstance()
        val notifCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
        todayCal.get(Calendar.YEAR) == notifCal.get(Calendar.YEAR) &&
                todayCal.get(Calendar.DAY_OF_YEAR) == notifCal.get(Calendar.DAY_OF_YEAR)
    }
    val earlier = userNotifications - today

    // ---- Buzz Background ----
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF0B1220))))
            .padding(18.dp)
    ) {
        // ---- Main glass container ----
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ---- Header ----
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Notificações",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "@$loggedInUsername",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF9CA3AF)
                    )
                }

                Divider(color = Color(0x1FFFFFFF))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {

                    if (today.isNotEmpty()) {
                        item { SectionDividerStyled(title = "Hoje") }
                        items(today, key = { it.id }) { NotificationCardStyled(it, viewModel) }
                    }

                    if (earlier.isNotEmpty()) {
                        item { SectionDividerStyled(title = "Anteriores") }
                        items(earlier, key = { it.id }) { NotificationCardStyled(it, viewModel) }
                    }

                    if (today.isEmpty() && earlier.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxHeight()
                                    .padding(top = 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Sem notificações por agora",
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCardStyled(notification: Notification, viewModel: NotificationViewModel) {

    val icon = when (notification.type) {
        "Like" -> Icons.Default.FavoriteBorder
        "Follow" -> Icons.Default.PersonAdd
        "Comment" -> Icons.Default.ChatBubbleOutline
        "System" -> Icons.Default.Settings
        "Reminder" -> Icons.Default.Info
        else -> Icons.Default.Notifications
    }

    val accent = when (notification.type) {
        "Like" -> Color(0xFF6366F1)
        "Follow" -> Color(0xFF22C55E)
        "Comment" -> Color(0xFF06B6D4)
        "System" -> Color(0xFF94A3B8)
        "Reminder" -> Color(0xFFF59E0B)
        else -> Color(0xFFC7D2FE)
    }

    val container = if (notification.seen) Color(0x0FFFFFFF) else Color(0x14111827)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.markAsSeen(notification.id) },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1FFFFFFF)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0x196366F1), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "@${notification.username} ${notification.message}",
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        .format(Date(notification.timestamp)),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9CA3AF)
                )
            }

            if (!notification.seen) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF6366F1), RoundedCornerShape(99.dp))
                )
            }
        }
    }
}

@Composable
fun SectionDividerStyled(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(
            modifier = Modifier.weight(1f),
            color = Color(0x1FFFFFFF)
        )
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 10.dp),
            color = Color(0xFF9CA3AF),
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            fontSize = 15.sp
        )
        Divider(
            modifier = Modifier.weight(1f),
            color = Color(0x1FFFFFFF)
        )
    }
}