package com.example.social_rede_mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val text: String = "",
    val isMe: Boolean,
    val timestamp: String,
    val emoji: String? = null,
    val isVoice: Boolean = false
)

@Composable
fun ChatDetailScreen(user: String, navController: NavController) {
    var messageText by remember { mutableStateOf("") }
    var emojiPickerVisible by remember { mutableStateOf(false) }
    var selectedMessageIndex by remember { mutableStateOf<Int?>(null) }

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage("Hey, how are you?", false, "13:20"),
                ChatMessage("I’m good! How about you?", true, "13:21"),
                ChatMessage("Doing fine, just working on the project.", false, "13:22"),
                ChatMessage("Same here, pushing last commits now.", true, "13:22"),
                ChatMessage(isMe = true, timestamp = "13:24", isVoice = true)
            )
        )
    }

    Scaffold(
        topBar = { ChatTopBar(user, navController) },
        bottomBar = {
            Column {
                if (emojiPickerVisible && selectedMessageIndex != null) {
                    EmojiPicker(onEmojiSelected = { emoji ->
                        messages = messages.mapIndexed { i, msg ->
                            if (i == selectedMessageIndex) msg.copy(emoji = emoji) else msg
                        }
                        emojiPickerVisible = false
                        selectedMessageIndex = null
                    })
                }
                MessageInputBar(
                    value = messageText,
                    onValueChange = { messageText = it },
                    onSend = {
                        if (messageText.isNotBlank()) {
                            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                            messages = messages + ChatMessage(text = messageText, isMe = true, timestamp = time)
                            messageText = ""
                        }
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(messages) { index, message ->
                AnimatedVisibility(visible = true) {
                    ChatBubble(
                        message = message,
                        onLongPress = {
                            selectedMessageIndex = index
                            emojiPickerVisible = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatTopBar(user: String, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Profile image + name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.story5),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = user,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Active now",
                            color = Color.Green,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            IconButton(onClick = { }) {
                Icon(Icons.Default.Send, contentDescription = "More", tint = Color.White)
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(message: ChatMessage, onLongPress: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isMe) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (message.isMe) Alignment.End else Alignment.Start) {
            if (message.isVoice) {
                VoiceMessageBubble(isMe = message.isMe, timestamp = message.timestamp)
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (message.isMe) Color(0xFFDCF8C6) else Color(0xFFF1F1F1))
                        .combinedClickable(onClick = {}, onLongClick = onLongPress)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .widthIn(max = 280.dp)
                ) {
                    Text(text = message.text, color = Color.Black, fontSize = 15.sp)
                }

                Text(
                    text = message.timestamp,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }

            message.emoji?.let {
                Text(text = it, fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp, top = 2.dp))
            }
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
fun VoiceMessageBubble(isMe: Boolean, timestamp: String, isPlaying: Boolean = false) {
    Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(if (isMe) Color(0xFFDCF8C6) else Color(0xFFF1F1F1))
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .widthIn(max = 280.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Send else Icons.Default.PlayArrow,
                contentDescription = "Play/Pause",
                tint = if (isMe) Color.Black else Color.DarkGray,
                modifier = Modifier.size(24.dp)
            )

            Canvas(modifier = Modifier.height(24.dp).width(140.dp)) {
                drawLine(
                    color = Color.DarkGray.copy(alpha = 0.4f),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 4f
                )
            }
        }
        Text(
            text = timestamp,
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
        )
    }
}

@Composable
fun EmojiPicker(onEmojiSelected: (String) -> Unit) {
    val emojis = listOf("😂", "🔥", "❤️", "👍", "👀", "😢", "🎉")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        emojis.forEach { emoji ->
            Text(
                text = emoji,
                fontSize = 24.sp,
                modifier = Modifier
                    .clickable { onEmojiSelected(emoji) }
                    .padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun MessageInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.weight(1f).padding(end = 12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text("Type a message...", style = TextStyle(color = MaterialTheme.colorScheme.outline))
                }
                innerTextField()
            }
        )

        IconButton(onClick = onSend, enabled = value.isNotBlank()) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = if (value.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
            )
        }
    }
}
