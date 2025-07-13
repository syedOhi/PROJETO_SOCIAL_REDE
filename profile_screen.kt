package com.example.social_rede_mobile

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.social_rede_mobile.data.PostViewModel
import com.example.social_rede_mobile.data.User
import com.example.social_rede_mobile.data.UserViewModel
import android.content.Context
@Composable
fun ProfileScreen(navController: NavController, onToggleTheme: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("buzz_prefs", Context.MODE_PRIVATE)
    val currentUsername = prefs.getString("username", "BuzzUser") ?: "BuzzUser"

    val postViewModel: PostViewModel = viewModel(
        factory = viewModelFactory {
            initializer { PostViewModel(context.applicationContext as Application) }
        }
    )

    val userViewModel: UserViewModel = viewModel(
        factory = viewModelFactory {
            initializer { UserViewModel(context.applicationContext as Application) }
        }
    )

    val allUsers by userViewModel.getAllUsers().collectAsState(initial = emptyList())
    val userMap = remember(allUsers) { allUsers.associateBy { it.username } }

    val allPosts by postViewModel.posts.observeAsState(emptyList())
    val userPosts = allPosts.filter { it.username == currentUsername }

    val currentUser by userViewModel.getUserByUsername(currentUsername).observeAsState()

    val followerCount by userViewModel.getFollowerCount(currentUsername).collectAsState(initial = 0)
    val followingCount by userViewModel.getFollowingCount(currentUsername).collectAsState(initial = 0)

    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 👤 Top Info + Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(80.dp)
                ) {
                    currentUser?.let {
                        val imageUri = it.profileImageUri
                        when {
                            !imageUri.isNullOrBlank() -> {
                                val parsedUri = remember(imageUri) { Uri.parse(imageUri) }
                                Image(
                                    painter = rememberAsyncImagePainter(parsedUri),
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            it.profileImageResId != null -> {
                                Image(
                                    painter = painterResource(id = it.profileImageResId),
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            else -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Gray)
                                )
                            }
                        }
                    }
                }

                Column {
                    Text(
                        text = currentUser?.fullName ?: "Full Name",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "@${currentUser?.username ?: "username"}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = currentUser?.bio ?: "Your bio will appear here.",
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 20.sp
                    )

                    // 🔍 Debug URI (optional)
                    if (!currentUser?.profileImageUri.isNullOrBlank()) {
                        Text(
                            text = "URI: ${currentUser?.profileImageUri}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.Gray
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { menuExpanded = true }
                    )

                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Settings") },
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

                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = "Toggle Theme",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onToggleTheme() }
                )
            }
        }

        // 📊 Live Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem("Posts", userPosts.size.toString())
            StatItem("Followers", followerCount.toString())
            StatItem("Following", followingCount.toString())
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "As Minhas Publicações",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(userPosts, key = { it.id }) { post ->
                InstagramPostCard(
                    post = post,
                    userMap = userMap,
                    onLikeToggle = { postId, liked, delta ->
                        postViewModel.updateLikeStatus(postId, liked, delta)
                    },
                    onCommentClick = {
                        Toast.makeText(context, "Comment clicked for Post ${post.id}", Toast.LENGTH_SHORT).show()
                    }
                )
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
