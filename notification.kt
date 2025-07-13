package com.example.social_rede_mobile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NotificationType {
    Like, Follow, Comment, System, Reminder
}

data class NotificationItem(
    val title: String,
    val message: String,
    val timestamp: String,
    val type: NotificationType
)

@Composable
fun NotificationScreen() {
    val todayNotifications = remember {
        listOf(
            NotificationItem("Novo seguidor", "Alice started following you.", "2m ago", NotificationType.Follow),
            NotificationItem("Interação - Liked", "Bob liked your post.", "10m ago", NotificationType.Like)
        )
    }

    val earlierNotifications = remember {
        listOf(
            NotificationItem("Comment", "Charlie commented on your photo.", "Yesterday", NotificationType.Comment),
            NotificationItem("Sistema", "New version update is available.", "2 days ago", NotificationType.System),
            NotificationItem("Reminder", "Check out trending posts.", "3 days ago", NotificationType.Reminder)
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Notificações",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { SectionDivider(title = "Today") }
            items(todayNotifications) { NotificationCard(it) }

            item { SectionDivider(title = "Earlier") }
            items(earlierNotifications) { NotificationCard(it) }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationItem) {
    val (icon, color) = when (notification.type) {
        NotificationType.Like -> Icons.Default.FavoriteBorder to MaterialTheme.colorScheme.primary
        NotificationType.Follow -> Icons.Default.PersonAdd to MaterialTheme.colorScheme.secondary
        NotificationType.Comment -> Icons.Default.ChatBubbleOutline to MaterialTheme.colorScheme.tertiary
        NotificationType.System -> Icons.Default.Settings to MaterialTheme.colorScheme.outline
        NotificationType.Reminder -> Icons.Default.Info to MaterialTheme.colorScheme.inversePrimary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Notification Icon",
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = notification.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = notification.message, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = notification.timestamp, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
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
