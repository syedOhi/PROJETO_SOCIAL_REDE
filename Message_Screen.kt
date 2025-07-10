package com.example.social_rede_mobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.social_rede_mobile.R

data class MessageItem(
    val sender: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0,
    val icon: ImageVector = Icons.Default.Chat,
    val isVoice: Boolean = false,
    val imageResId: Int = R.drawable.profile_icon
)

@Composable
fun MessageScreen(navController: NavController) {
    val messages = remember {
        listOf(
            MessageItem("Danny Moore", "Hey! How was your day?", "10:32", 1, imageResId = R.drawable.pfp1),
            MessageItem("Nancy Clark", "What’s new in your project?", "11:01", imageResId = R.drawable.pfp2),
            MessageItem("Daniel Colon", "It suits you?", "18:12", 0, imageResId = R.drawable.pfp3),
            MessageItem("Janet Munoz", "I didn't mention this earlier, but…", "09:30", imageResId = R.drawable.pfp4),
            MessageItem("Marjorie Roberts", "Hope you join us!", "21:06", imageResId = R.drawable.pfp5),
            MessageItem("Jason Brown", "Can’t wait to meet!!!", "11:40", imageResId = R.drawable.story5)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 🔷 Top Rounded Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                        Icon(Icons.Default.Chat, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurface)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "STORY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 📸 Story section using your own Composable
                    StorySection()

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Messages",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 💬 Message List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(messages) { message ->
                    MessageCard(message = message) {
                        navController.navigate("chat/${message.sender}")
                    }
                }
            }
        }
    }
}

@Composable
fun MessageCard(message: MessageItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = message.imageResId),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.sender,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = message.time,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Text(
                text = message.lastMessage,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (message.unreadCount > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Badge(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Text(
                    text = message.unreadCount.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
