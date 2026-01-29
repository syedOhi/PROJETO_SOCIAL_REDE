package com.example.social_rede_mobile

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.social_rede_mobile.data.ChatViewModel
import com.example.social_rede_mobile.network.models.UserOut
import androidx. compose. foundation. BorderStroke
@Composable
fun MessageScreen(navController: NavController) {

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("buzz_prefs", Context.MODE_PRIVATE)
    val currentUsername = prefs.getString("username", "") ?: return

    val chatViewModel: ChatViewModel = viewModel(
        factory = viewModelFactory { initializer { ChatViewModel(context.applicationContext as Application) } }
    )

    val conversationUsers by chatViewModel.conversationUsers.collectAsState()
    val chatRequests by chatViewModel.chatRequests.collectAsState()

    LaunchedEffect(Unit) {
        chatViewModel.loadConversationUsers(currentUsername)
        chatViewModel.loadChatRequests(currentUsername)
    }

    // ---- Buzz Background ----
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF0B1220)))
            )
            .padding(18.dp)
    ) {

        // ---- Main glass container ----
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ---- Header ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mensagens",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "@$currentUsername",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }

                Divider(color = Color(0x1FFFFFFF))

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {

                    // -----------------------------
                    // MESSAGE REQUESTS SECTION
                    // -----------------------------
                    if (chatRequests.isNotEmpty()) {
                        item {
                            Text(
                                text = "Pedidos de mensagem (${chatRequests.size})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                        }

                        items(chatRequests) { request ->
                            MessageRequestItemStyled(
                                username = request.sender,
                                onAccept = {
                                    chatViewModel.acceptChatRequest(
                                        sender = request.sender,
                                        receiver = currentUsername
                                    )
                                },
                                onIgnore = {
                                    chatViewModel.ignoreChatRequest(
                                        sender = request.sender,
                                        receiver = currentUsername
                                    )
                                }
                            )
                        }

                        item {
                            Divider(color = Color(0x1FFFFFFF), modifier = Modifier.padding(vertical = 6.dp))
                        }
                    }

                    // -----------------------------
                    // MESSAGES SECTION
                    // -----------------------------
                    if (conversationUsers.isEmpty() && chatRequests.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxHeight()
                                    .padding(top = 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Segue utilizadores para começares uma conversa",
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        items(conversationUsers) { user ->
                            MessageUserItemStyled(user = user) {
                                navController.navigate("chat/${user.username}")
                            }
                        }
                    }
                }
            }
        }
    }
}

/* -----------------------------
   MESSAGE USER ROW (Styled)
------------------------------ */
@Composable
private fun MessageUserItemStyled(
    user: UserOut,
    onClick: () -> Unit
) {
    val profilePainter = when {
        !user.profileImageUri.isNullOrBlank() ->
            rememberAsyncImagePainter(Uri.parse(user.profileImageUri))
        user.profileImageResId != null ->
            painterResource(id = user.profileImageResId)
        else -> painterResource(id = R.drawable.profile_icon)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x0FFFFFFF)),
        border = BorderStroke(1.dp, Color(0x1FFFFFFF)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A))
                    .border(2.dp, Color(0x336366F1), CircleShape)
            ) {
                Image(
                    painter = profilePainter,
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Toque para abrir o chat",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9CA3AF)
                )
            }

            Text(
                text = "›",
                color = Color(0xFFC7D2FE),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

/* -----------------------------
   MESSAGE REQUEST ROW (Styled)
------------------------------ */
@Composable
fun MessageRequestItemStyled(
    username: String,
    onAccept: () -> Unit,
    onIgnore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x0FFFFFFF)),
        border = BorderStroke(1.dp, Color(0x1FFFFFFF)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = username,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Quer iniciar conversa contigo",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9CA3AF)
                )
            }

            Button(
                onClick = onAccept,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1),
                    contentColor = Color.White
                )
            ) {
                Text("Aceitar", fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.width(8.dp))

            OutlinedButton(
                onClick = onIgnore,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                border = BorderStroke(1.dp, Color(0x22FFFFFF))
            ) {
                Text("Ignorar", fontWeight = FontWeight.Bold)
            }
        }
    }
}