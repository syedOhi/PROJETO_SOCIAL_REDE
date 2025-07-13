package com.example.social_rede_mobile

import android.app.Application
import android.content.Context
import android.net.Uri
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.social_rede_mobile.data.ChatViewModel
import com.example.social_rede_mobile.data.User
import com.example.social_rede_mobile.data.UserViewModel

@Composable
fun MessageScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("buzz_prefs", Context.MODE_PRIVATE)
    val currentUsername = prefs.getString("username", "") ?: return

    val userViewModel: UserViewModel = viewModel(
        factory = viewModelFactory {
            initializer { UserViewModel(context.applicationContext as Application) }
        }
    )

    val chatViewModel: ChatViewModel = viewModel(
        factory = viewModelFactory {
            initializer { ChatViewModel(context.applicationContext as Application) }
        }
    )

    val allUsers by userViewModel.getAllUsers().collectAsState(initial = emptyList())
    val chatUsernames by chatViewModel.getChatUsers(currentUsername).collectAsState(initial = emptyList())
    val followedUsers by userViewModel.getFollowedUsers(currentUsername).collectAsState(initial = emptyList())

    val allRelevantUsernames = remember(chatUsernames, followedUsers) {
        (followedUsers.map { it.username } + chatUsernames).distinct()
    }

    val chatUsers = allRelevantUsernames.mapNotNull { username ->
        allUsers.find { it.username == username }
    }

    val suggestedUsers = allUsers.filter { it.username != currentUsername }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (followedUsers.isEmpty()) {
                        Text("Utilizadores sugeridos", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            suggestedUsers.forEach { user ->
                                val profilePainter = when {
                                    !user.profileImageUri.isNullOrBlank() -> {
                                        val uri = remember(user.profileImageUri) { Uri.parse(user.profileImageUri) }
                                        rememberAsyncImagePainter(uri)
                                    }
                                    user.profileImageResId != null -> painterResource(id = user.profileImageResId)
                                    else -> painterResource(id = R.drawable.profile_icon)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Image(
                                        painter = profilePainter,
                                        contentDescription = "Suggested User",
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .clickable {
                                                navController.navigate("search_details/${user.username}")
                                            }
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(user.username, fontSize = 12.sp, maxLines = 1)
                                }
                            }
                        }
                    } else {
                        Text("Amigos", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Você tem ${followedUsers.size} amigo${if (followedUsers.size == 1) "" else "s"}", fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            followedUsers.forEach { user ->
                                val profilePainter = when {
                                    !user.profileImageUri.isNullOrBlank() -> {
                                        val uri = remember(user.profileImageUri) { Uri.parse(user.profileImageUri) }
                                        rememberAsyncImagePainter(uri)
                                    }
                                    user.profileImageResId != null -> painterResource(id = user.profileImageResId)
                                    else -> painterResource(id = R.drawable.profile_icon)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Image(
                                        painter = profilePainter,
                                        contentDescription = "Friend Profile",
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .clickable {
                                                navController.navigate("search_details/${user.username}")
                                            }
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(user.username, fontSize = 12.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Mensagens", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (chatUsers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "***Siga utilizadores para começar uma conversa***",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(chatUsers) { user ->
                        val isFollowed = followedUsers.any { it.username == user.username }
                        val unreadCount by chatViewModel.getUnreadCount(currentUsername, user.username)
                            .collectAsState(initial = 0)

                        FollowedUserCard(
                            user = user,
                            showUnfollowedIcon = !isFollowed,
                            unreadCount = unreadCount
                        ) {
                            navController.navigate("chat/${user.username}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FollowedUserCard(
    user: User,
    showUnfollowedIcon: Boolean = false,
    unreadCount: Int = 0,
    onClick: () -> Unit
) {
    val profilePainter = when {
        !user.profileImageUri.isNullOrBlank() -> {
            val uri = remember(user.profileImageUri) { Uri.parse(user.profileImageUri) }
            rememberAsyncImagePainter(uri)
        }
        user.profileImageResId != null -> painterResource(id = user.profileImageResId)
        else -> painterResource(id = R.drawable.profile_icon)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = profilePainter,
            contentDescription = "Profile Image",
            modifier = Modifier.size(52.dp).clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.username,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (showUnfollowedIcon) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Not Followed",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text(
                            text = unreadCount.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = "Toque para conversar..",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
