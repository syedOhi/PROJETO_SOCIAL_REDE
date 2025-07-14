// --- 5. Notification Screen UI ---
package com.example.social_rede_mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.foundation.clickable

import android.content.Context
enum class NotificationType {
    Like, Follow, Comment, System, Reminder
}

@Composable
fun NotificationScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("buzz_prefs", Context.MODE_PRIVATE)
    val loggedInUsername = prefs.getString("username", "") ?: ""

    val viewModel: NotificationViewModel = viewModel(factory = viewModelFactory {
        initializer {
            NotificationViewModel(context.applicationContext as android.app.Application)
        }
    })

    val notifications by viewModel.notifications.collectAsState()

    // ✅ Only show notifications meant for the logged-in user
    val userNotifications = notifications.filter { it.targetUsername == loggedInUsername }

    val today = userNotifications.filter {
        val todayCal = Calendar.getInstance()
        val notifCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
        todayCal.get(Calendar.YEAR) == notifCal.get(Calendar.YEAR) &&
                todayCal.get(Calendar.DAY_OF_YEAR) == notifCal.get(Calendar.DAY_OF_YEAR)
    }
    val earlier = userNotifications - today

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(
            text = "Notificações",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (today.isNotEmpty()) {
                item { SectionDivider(title = "Today") }
                items(today, key = { it.id }) { NotificationCard(it, viewModel) }
            }
            if (earlier.isNotEmpty()) {
                item { SectionDivider(title = "Earlier") }
                items(earlier, key = { it.id }) { NotificationCard(it, viewModel) }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: Notification, viewModel: NotificationViewModel) {
    val (icon, color) = when (notification.type) {
        "Like" -> Icons.Default.FavoriteBorder to MaterialTheme.colorScheme.primary
        "Follow" -> Icons.Default.PersonAdd to MaterialTheme.colorScheme.secondary
        "Comment" -> Icons.Default.ChatBubbleOutline to MaterialTheme.colorScheme.tertiary
        "System" -> Icons.Default.Settings to MaterialTheme.colorScheme.outline
        "Reminder" -> Icons.Default.Info to MaterialTheme.colorScheme.inversePrimary
        else -> Icons.Default.Notifications to MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.markAsSeen(notification.id) },
        colors = CardDefaults.cardColors(
            containerColor = if (notification.seen) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("@${notification.username} ${notification.message}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(notification.timestamp)), fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun SectionDivider(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    }
}
