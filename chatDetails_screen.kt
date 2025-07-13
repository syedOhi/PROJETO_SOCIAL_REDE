package com.example.social_rede_mobile

import android.app.Application
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.social_rede_mobile.data.ChatViewModel
import com.example.social_rede_mobile.data.User
import com.example.social_rede_mobile.data.UserViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.material.icons.filled.ArrowBack
import coil.compose.rememberAsyncImagePainter
import android.net.Uri

@Composable
fun ChatDetailScreen(user: String, navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("buzz_prefs", Context.MODE_PRIVATE)
    val currentUsername = prefs.getString("username", "") ?: return

    val chatViewModel: ChatViewModel = viewModel(
        factory = viewModelFactory { initializer { ChatViewModel(context.applicationContext as Application) } }
    )
    val userViewModel: UserViewModel = viewModel(
        factory = viewModelFactory { initializer { UserViewModel(context.applicationContext as Application) } }
    )

    val messages by chatViewModel.getMessages(currentUsername, user).collectAsState(initial = emptyList())
    val chatUser by userViewModel.getUserByUsername(user).observeAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var typingStatus by remember { mutableStateOf(false) }
    val typingJob = remember { mutableStateOf<Job?>(null) }

    var messageText by remember { mutableStateOf("") }
    var emojiPickerVisible by remember { mutableStateOf(false) }
    var selectedMessageIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        chatViewModel.syncMessagesWithFirestore(currentUsername, user)
        chatViewModel.observeTyping(user) { typingStatus = it }
    }

    LaunchedEffect(messages.size) {
        chatViewModel.markMessagesAsRead(sender = user, receiver = currentUsername)
        coroutineScope.launch {
            listState.animateScrollToItem(messages.size)
        }
    }

    Scaffold(
        topBar = {
            ChatTopBar(chatUser, navController, typingStatus)
        },
        bottomBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                AnimatedVisibility(visible = emojiPickerVisible && selectedMessageIndex != null) {
                    EmojiPicker { emoji ->
                        selectedMessageIndex?.let { index ->
                            messages.getOrNull(index)?.let {
                                chatViewModel.reactToMessage(it.id, emoji)
                            }
                        }
                        emojiPickerVisible = false
                        selectedMessageIndex = null
                    }
                }

                MessageInputBar(
                    value = messageText,
                    onValueChange = {
                        messageText = it
                        chatViewModel.updateTyping(currentUsername, it.isNotBlank())

                        typingJob.value?.cancel()
                        typingJob.value = coroutineScope.launch {
                            delay(3000)
                            chatViewModel.updateTyping(currentUsername, false)
                        }
                    },
                    onSend = {
                        if (messageText.isNotBlank()) {
                            chatViewModel.sendFirestoreMessage(currentUsername, user, messageText)
                            chatViewModel.updateTyping(currentUsername, false)
                            messageText = ""
                        }
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(messages) { index, msg ->
                ChatBubble(
                    isMe = msg.sender == currentUsername,
                    text = msg.message,
                    time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp)),
                    emoji = msg.emoji,
                    isVoice = msg.isVoice
                ) {
                    selectedMessageIndex = index
                    emojiPickerVisible = true
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)



@Composable
fun ChatTopBar(user: User?, navController: NavController, isTyping: Boolean) {
    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFCCCCCC),
                            Color(0x885E60CE),
                            Color(0x665E60CE)
                        )
                    )
                )
                .padding(vertical = 24.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    !user?.profileImageUri.isNullOrBlank() -> {
                        val uri = remember(user!!.profileImageUri) {
                            Uri.parse(user.profileImageUri)
                        }
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                        )
                    }
                    user?.profileImageResId != null -> {
                        Image(
                            painter = painterResource(id = user.profileImageResId),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                        )
                    }
                    else -> {
                        Image(
                            painter = painterResource(id = R.drawable.profile_icon),
                            contentDescription = "Default Profile Image",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = user?.fullName ?: "Unknown",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = if (isTyping) "Typing..." else "Online",
                    fontSize = 14.sp,
                    color = if (isTyping) Color.Magenta else Color(0xFFBBF7D0)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)

@Composable
fun ChatBubble(
    isMe: Boolean,
    text: String,
    time: String,
    emoji: String? = null,
    isVoice: Boolean = false,
    onLongPress: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = if (isMe)
                            Brush.horizontalGradient(listOf(Color(0xFF4CAF50), Color(0xFF81C784)))
                        else
                            Brush.horizontalGradient(listOf(Color(0xFFE0E0E0), Color(0xFFF5F5F5)))
                    )
                    .combinedClickable(onClick = {}, onLongClick = onLongPress)
                    .padding(12.dp)
                    .widthIn(max = 280.dp)
            ) {
                Text(text = text, color = if (isMe) Color.White else Color.Black, fontSize = 15.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                emoji?.let {
                    Text(text = it, fontSize = 18.sp, modifier = Modifier.padding(start = 6.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = time, fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun MessageInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                "Escreva uma mensagem...",
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = CircleShape,
                shadowElevation = 4.dp,
                color = if (value.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(42.dp)
            ) {
                IconButton(
                    onClick = onSend,
                    enabled = value.isNotBlank(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}


@Composable
fun EmojiPicker(onEmojiSelected: (String) -> Unit) {
    val emojis = listOf("😂", "🔥", "❤️", "👍", "👀", "😢", "🎉")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        emojis.forEach { emoji ->
            Text(
                text = emoji,
                fontSize = 26.sp,
                modifier = Modifier
                    .clickable { onEmojiSelected(emoji) }
                    .padding(4.dp)
            )
        }
    }
}