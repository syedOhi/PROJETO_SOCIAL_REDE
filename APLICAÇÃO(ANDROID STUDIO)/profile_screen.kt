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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.social_rede_mobile.data.PostViewModel
import com.example.social_rede_mobile.data.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, onToggleTheme: () -> Unit) {

    var menuExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("buzz_prefs", Context.MODE_PRIVATE)
    val currentUsername = prefs.getString("username", "") ?: ""

    val postViewModel: PostViewModel = viewModel(
        factory = viewModelFactory { initializer { PostViewModel(context.applicationContext as Application) } }
    )

    val userViewModel: UserViewModel = viewModel(
        factory = viewModelFactory { initializer { UserViewModel(context.applicationContext as Application) } }
    )

    // 🔥 Load user profile from API
    val userProfile by userViewModel.getUserFromApi(currentUsername).observeAsState()

    // 🔥 Load posts from API
    LaunchedEffect(currentUsername) {
        postViewModel.loadPostsFromApi(currentUsername)
    }

    val allApiPosts by postViewModel.apiPosts.collectAsState()
    val userPosts = allApiPosts.filter { it.username == currentUsername }

    // ---- Background ----
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF0B1220))
                )
            )
            .padding(18.dp)
    ) {

        Card(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ------------------ TOP HEADER ------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "O meu Perfil",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "@$currentUsername",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF9CA3AF)
                        )
                    }

                    // Settings menu
                    Box {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { menuExpanded = true }
                        )

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Profile") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate("edit_profile")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate("login") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = Color(0xFFC7D2FE),
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onToggleTheme() }
                    )
                }

                // ------------------ PROFILE CARD ------------------
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x0FFFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F172A))
                                .border(2.dp, Color(0x336366F1), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            userProfile?.let {
                                when {
                                    !it.profileImageUri.isNullOrBlank() -> {
                                        Image(
                                            painter = rememberAsyncImagePainter(Uri.parse(it.profileImageUri!!)),
                                            contentDescription = "Profile Picture",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                        )
                                    }

                                    it.profileImageResId != null -> {
                                        Image(
                                            painter = painterResource(id = it.profileImageResId!!),
                                            contentDescription = "Profile Picture",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                        )
                                    }

                                    else -> {
                                        Text("🙂", fontSize = 26.sp)
                                    }
                                }
                            } ?: Text("🙂", fontSize = 26.sp)
                        }

                        Spacer(Modifier.width(14.dp))

                        // Name + Bio
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userProfile?.fullName ?: "Full Name",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "@${userProfile?.username ?: "username"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF9CA3AF),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = userProfile?.bio ?: "Your bio will appear here.",
                                fontSize = 13.sp,
                                color = Color(0xFFE5E7EB),
                                lineHeight = 18.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // ------------------ SECTION TITLE ------------------
                Text(
                    text = "As Minhas Publicações",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // ------------------ POSTS LIST ------------------
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(userPosts, key = { it.id }) { post ->

                        var postMenuExpanded by remember(post.id) { mutableStateOf(false) }

                        Box(modifier = Modifier.fillMaxWidth()) {

                            // Your existing card
                            InstagramPostCard(
                                post = post,
                                userMap = emptyMap(),
                                onLikeToggle = { postId, liked, _ ->
                                    postViewModel.toggleLike(
                                        postId = postId,
                                        username = currentUsername,
                                        liked = liked
                                    )
                                    postViewModel.loadPostsFromApi(currentUsername)
                                },
                                onCommentClick = {}
                            )

                            // 3-dot menu button (top-right)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 8.dp)
                            ) {
                                IconButton(onClick = { postMenuExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Post options",
                                        tint = Color.White
                                    )
                                }

                                DropdownMenu(
                                    expanded = postMenuExpanded,
                                    onDismissRequest = { postMenuExpanded = false }
                                ) {

                                    // ✅ REPORT (any post)
                                    DropdownMenuItem(
                                        text = { Text("Report") },
                                        onClick = {
                                            postMenuExpanded = false
                                            postViewModel.reportPostApi(
                                                postId = post.id,
                                                reportedBy = currentUsername
                                            )
                                        }
                                    )

                                    // ✅ DELETE (only if this post belongs to logged-in user)
                                    if (post.username == currentUsername) {
                                        DropdownMenuItem(
                                            text = { Text("Delete") },
                                            onClick = {
                                                postMenuExpanded = false
                                                postViewModel.deletePostApi(
                                                    postId = post.id,
                                                    username = currentUsername
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}