package com.example.social_rede_mobile

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.example.social_rede_mobile.data.UserViewModel
import com.example.social_rede_mobile.data.PostViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.navigation.NavController
import android.net.Uri
import coil.compose.rememberAsyncImagePainter

@Composable
fun SearchDetailsScreen(
    username: String,

    navController: NavController, // ✅ Add this
  //  userVieodel: UserViewModel = viewModel(),

    userViewModel: UserViewModel = viewModel(),
    postViewModel: PostViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("buzz_prefs", Context.MODE_PRIVATE) }
    val loggedInUsername = prefs.getString("username", "") ?: ""

    val userLiveData = userViewModel.getUserByUsername(username)
    val user by userLiveData.observeAsState()

    val userPosts by postViewModel.getPostsByUser(username).collectAsState(initial = emptyList())

    val followerCount by userViewModel.getFollowerCount(username).collectAsState(initial = 0)
    val followingCount by userViewModel.getFollowingCount(username).collectAsState(initial = 0)

    val isFollowing by userViewModel
        .isFollowing(loggedInUsername, username)
        .collectAsState(initial = false)

    var followState by remember { mutableStateOf(isFollowing) }

    // To keep UI in sync after DB update
    LaunchedEffect(isFollowing) {
        followState = isFollowing
    }

    user?.let {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // Profile Header
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val profilePainter = when {
                        !it.profileImageUri.isNullOrBlank() -> {
                            val uri = remember(it.profileImageUri) { android.net.Uri.parse(it.profileImageUri) }
                            rememberAsyncImagePainter(uri)
                        }
                        it.profileImageResId != null -> {
                            painterResource(id = it.profileImageResId)
                        }
                        else -> {
                            painterResource(id = R.drawable.default_pfp)
                        }
                    }

                    Image(
                        painter = profilePainter,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )


                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "@${it.username}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = it.bio ?: "No bio available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Follower / Following counts
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(40.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(followerCount.toString(), fontWeight = FontWeight.Bold)
                            Text("Followers", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(followingCount.toString(), fontWeight = FontWeight.Bold)
                            Text("Following", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Follow/Unfollow Button
                    if (username != loggedInUsername) {
                        Button(
                            onClick = {
                                followState = !followState
                                if (followState) {
                                    userViewModel.followUser(loggedInUsername, username)
                                } else {
                                    userViewModel.unfollowUser(loggedInUsername, username)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (followState) Color.LightGray else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = if (followState) "Unfollow" else "Follow",
                                color = if (followState) Color.Black else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Divider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Posts by @${it.username}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (userPosts.isEmpty()) {
                        Text(
                            text = "Nenhuma publicação ainda \uD83D\uDE36",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // User's Posts
            items(userPosts) { post ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        if (post.imageResId != null) {
                            Image(
                                painter = painterResource(id = post.imageResId),
                                contentDescription = "Post Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Text(
                            text = post.caption,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Red,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading user data...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
