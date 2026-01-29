package com.example.social_rede_mobile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.social_rede_mobile.R
import com.example.social_rede_mobile.data.PostViewModel
import com.example.social_rede_mobile.data.UserViewModel
import com.example.social_rede_mobile.network.RetrofitInstance
import com.example.social_rede_mobile.network.models.NotificationCreate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun SearchDetailsScreen(
    username: String,
    navController: NavController,
    userViewModel: UserViewModel = viewModel(),
    postViewModel: PostViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("buzz_prefs", Context.MODE_PRIVATE) }
    val loggedInUsername = prefs.getString("username", "") ?: ""

    LaunchedEffect(username) {
        userViewModel.fetchUserFromApi(username)
        userViewModel.loadFollowerCount(username)
        userViewModel.loadFollowingCount(username)
        userViewModel.loadIsFollowing(loggedInUsername, username)
        postViewModel.loadUserPosts(username)
    }

    val user by userViewModel.apiUser.collectAsState()
    val followerCount by userViewModel.followerCount.collectAsState()
    val followingCount by userViewModel.followingCount.collectAsState()
    val posts by postViewModel.userApiPosts.collectAsState()

    val isFollowingFlow = remember(loggedInUsername, username) {
        userViewModel.loadIsFollowing(loggedInUsername, username)
    }
    val isFollowing by isFollowingFlow.collectAsState(initial = false)

    var followState by remember { mutableStateOf(false) }
    LaunchedEffect(isFollowing) { followState = isFollowing }

    if (user == null) {
        BuzzBackground {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF6366F1))
            }
        }
        return
    }

    BuzzBackground {

        // Whole page list (inside background)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 18.dp)
        ) {

            // MAIN PROFILE CARD
            item {
                BuzzCard {

                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Avatar + name/bio
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        val imagePainter =
                            if (!user!!.profileImageUri.isNullOrBlank())
                                rememberAsyncImagePainter(Uri.parse(user!!.profileImageUri))
                            else painterResource(id = R.drawable.default_pfp)

                        Image(
                            painter = imagePainter,
                            contentDescription = null,
                            modifier = Modifier
                                .size(112.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F172A))
                                .border(2.dp, Color(0x336366F1), CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "@${user!!.username}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = user!!.bio ?: "No bio available",
                            color = Color(0xFF9CA3AF),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(14.dp))

                        // KPI row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatPill(title = "Followers", value = followerCount.toString(), modifier = Modifier.weight(1f))
                            StatPill(title = "Following", value = followingCount.toString(), modifier = Modifier.weight(1f))
                        }

                        Spacer(Modifier.height(14.dp))

                        // Follow button
                        if (username != loggedInUsername) {
                            val isUnfollow = followState

                            Button(
                                onClick = {
                                    followState = !followState

                                    if (followState) {
                                        userViewModel.followUserApi(loggedInUsername, username)

                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                RetrofitInstance.api.sendNotification(
                                                    NotificationCreate(
                                                        id = UUID.randomUUID().toString(),
                                                        username = loggedInUsername,
                                                        message = "started following you.",
                                                        type = "Follow",
                                                        targetUsername = username,
                                                        timestamp = System.currentTimeMillis(),
                                                        seen = false
                                                    )
                                                )
                                            } catch (e: Exception) {
                                                Log.e("Notification", "Failed: ${e.message}")
                                            }
                                        }

                                    } else {
                                        userViewModel.unfollowUserApi(loggedInUsername, username)
                                    }

                                    CoroutineScope(Dispatchers.IO).launch {
                                        userViewModel.loadIsFollowing(loggedInUsername, username)
                                        userViewModel.loadFollowerCount(username)
                                        userViewModel.loadFollowingCount(username)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isUnfollow) Color(0x1FFFFFFF) else Color(0xFF6366F1),
                                    contentColor = if (isUnfollow) Color.White else Color.White
                                ),
                                border = if (isUnfollow) BorderStroke(1.dp, Color(0x22FFFFFF)) else null
                            ) {
                                Text(
                                    text = if (isUnfollow) "Unfollow" else "Follow",
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        Divider(color = Color(0x22FFFFFF))

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "Posts",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // POSTS LIST
            items(posts) { post ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x0FFFFFFF)),
                    border = BorderStroke(1.dp, Color(0x1FFFFFFF))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {

                        if (!post.imageUri.isNullOrBlank()) {
                            Image(
                                painter = rememberAsyncImagePainter(Uri.parse(post.imageUri)),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        Text(
                            text = post.caption,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = Color(0x14FFFFFF), // soft glass
                shape = RoundedCornerShape(14.dp)
            )
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = title,
            color = Color(0xFF9CA3AF),
            fontWeight = FontWeight.SemiBold
        )
    }
}