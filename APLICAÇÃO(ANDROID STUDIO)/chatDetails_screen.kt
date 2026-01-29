package com.example.social_rede_mobile

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.social_rede_mobile.data.ChatViewModel
import com.example.social_rede_mobile.network.RetrofitInstance
import com.example.social_rede_mobile.network.models.UserOut
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatDetailScreen(user: String, navController: NavController) {

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("buzz_prefs", Context.MODE_PRIVATE)
    val currentUsername = prefs.getString("username", "") ?: return

    val chatViewModel: ChatViewModel = viewModel(
        factory = viewModelFactory { initializer { ChatViewModel(context.applicationContext as Application) } }
    )

    val messages by chatViewModel.messages.collectAsState()
    var chatUser by remember { mutableStateOf<UserOut?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var typingStatus by remember { mutableStateOf(false) }
    val typingJob = remember { mutableStateOf<Job?>(null) }

    var messageText by remember { mutableStateOf("") }
    var emojiPickerVisible by remember { mutableStateOf(false) }
    var selectedMessageIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(user) {
        try {
            chatUser = RetrofitInstance.api.getUser(user)
        } catch (e: Exception) {
            chatUser = null
            e.printStackTrace()
        }

        chatViewModel.loadConversation(currentUsername, user)
        chatViewModel.observeTyping(user) { typingStatus = it }

        chatViewModel.listenForRealtimeMessages(currentUsername, user) {
            chatViewModel.loadConversation(currentUsername, user)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            chatViewModel.markMessagesAsRead(sender = user, receiver = currentUsername)
            coroutineScope.launch { listState.animateScrollToItem(messages.lastIndex) }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            BuzzChatTopBar(
                user = chatUser,
                isTyping = typingStatus,
                navController = navController
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(bottom = 10.dp)
            ) {
                AnimatedVisibility(visible = emojiPickerVisible && selectedMessageIndex != null) {
                    BuzzEmojiPicker(
                        onEmojiSelected = { _ ->
                            emojiPickerVisible = false
                            selectedMessageIndex = null
                        }
                    )
                }

                BuzzMessageComposer(
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
                            val text = messageText
                            messageText = ""
                            chatViewModel.updateTyping(currentUsername, false)

                            chatViewModel.sendMessage(
                                sender = currentUsername,
                                receiver = user,
                                text = text
                            )

                            chatViewModel.sendFirestoreMessage(
                                sender = currentUsername,
                                receiver = user,
                                text = text
                            )

                            chatViewModel.loadConversation(currentUsername, user)
                        }
                    }
                )
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF0B1220)))
                )
        ) {

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp)
                    .padding(top = 10.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(messages) { index, msg ->

                    val isMe = msg.sender == currentUsername
                    val time = SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(Date(msg.timestamp))

                    val prevSender = messages.getOrNull(index - 1)?.sender
                    val nextSender = messages.getOrNull(index + 1)?.sender

                    val startsGroup = prevSender != msg.sender
                    val endsGroup = nextSender != msg.sender

                    BuzzChatBubble(
                        isMe = isMe,
                        text = msg.message,
                        time = time,
                        emoji = msg.emoji,
                        startsGroup = startsGroup,
                        endsGroup = endsGroup,
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

/* ---------------------------
   TOP BAR (Buzz Dark Glass)
---------------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuzzChatTopBar(
    user: UserOut?,
    isTyping: Boolean,
    navController: NavController
) {
    Surface(
        color = Color(0xFF111827),
        tonalElevation = 0.dp,
        shadowElevation = 10.dp,
        shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            val profilePainter = when {
                !user?.profileImageUri.isNullOrBlank() ->
                    rememberAsyncImagePainter(Uri.parse(user.profileImageUri))
                user?.profileImageResId != null ->
                    painterResource(id = user.profileImageResId)
                else -> painterResource(id = R.drawable.profile_icon)
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A))
                    .border(2.dp, Color(0x336366F1), CircleShape)
            ) {
                Image(
                    painter = profilePainter,
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.fullName ?: user?.username ?: "Unknown",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (isTyping) "A escrever..." else "Online",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isTyping) Color(0xFFC7D2FE) else Color(0xFF34D399)
                )
            }

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isTyping) Color(0xFF6366F1) else Color(0xFF10B981))
            )

            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

/* ---------------------------
   CHAT BUBBLE (Buzz grouped)
---------------------------- */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BuzzChatBubble(
    isMe: Boolean,
    text: String,
    time: String,
    emoji: String? = null,
    startsGroup: Boolean,
    endsGroup: Boolean,
    onLongPress: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 330.dp),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {

            val shape = RoundedCornerShape(
                topStart = if (isMe) 18.dp else 10.dp,
                topEnd = if (isMe) 10.dp else 18.dp,
                bottomStart = if (isMe) 18.dp else 6.dp,
                bottomEnd = if (isMe) 6.dp else 18.dp
            )

            Surface(
                shape = shape,
                tonalElevation = 0.dp,
                shadowElevation = 10.dp,
                color = Color.Transparent,
                modifier = Modifier
                    .combinedClickable(onClick = {}, onLongClick = onLongPress)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = if (isMe)
                                Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF111827)))
                            else
                                Brush.linearGradient(listOf(Color(0x0FFFFFFF), Color(0x14111827)))
                        )
                        .border(
                            width = 1.dp,
                            color = if (isMe) Color(0x206366F1) else Color(0x1FFFFFFF),
                            shape = shape
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = text,
                        fontSize = 15.sp,
                        lineHeight = 19.sp,
                        color = if (isMe) Color.White else Color(0xFFE5E7EB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                emoji?.let {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0x0FFFFFFF),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier.border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(999.dp))
                    ) {
                        Text(
                            text = it,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = time,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

/* ---------------------------
   INPUT (Buzz floating dark)
---------------------------- */
@Composable
fun BuzzMessageComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 0.dp,
        shadowElevation = 14.dp,
        color = Color(0xFF111827),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(22.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0x0FFFFFFF), RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(18.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (value.isEmpty()) {
                            Text("Mensagem...", color = Color(0xFF9CA3AF))
                        }
                        inner()
                    }
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                shape = CircleShape,
                color = if (value.isNotBlank()) Color(0xFF6366F1) else Color(0x22374151),
                modifier = Modifier.size(46.dp)
            ) {
                IconButton(onClick = onSend, enabled = value.isNotBlank()) {
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

/* ---------------------------
   EMOJI PICKER (Buzz dark)
---------------------------- */
@Composable
fun BuzzEmojiPicker(onEmojiSelected: (String) -> Unit) {
    val emojis = listOf("😂", "🔥", "❤️", "👍", "👀", "😢", "🎉", "😍", "😡")

    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 0.dp,
        shadowElevation = 10.dp,
        color = Color(0xFF111827),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            emojis.forEach { e ->
                Text(
                    text = e,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .clickable { onEmojiSelected(e) }
                        .padding(4.dp)
                )
            }
        }
    }
}