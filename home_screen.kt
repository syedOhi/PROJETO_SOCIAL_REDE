package com.example.social_rede_mobile

// 👇 ADD THESE IMPORTS
import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.social_rede_mobile.data.Post
import com.example.social_rede_mobile.data.PostViewModel
import kotlinx.coroutines.launch
import com.example.social_rede_mobile.data.UserViewModel
import androidx.compose.runtime.collectAsState
@Composable
fun HomeScreen(
    navController: NavController,
    listState: LazyListState,
    onToggleTheme: () -> Unit,
    onCollapseFab: () -> Unit
) {
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


    val posts by postViewModel.posts.observeAsState(emptyList())
    val allUsers by userViewModel.getAllUsers().collectAsState(initial = emptyList())

    val userProfileImageMap = remember(allUsers) {
        allUsers.associate { it.username to it.profileImageResId }
    }


    var showPostDialog by remember { mutableStateOf(false) }
    var postContent by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        if (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 10) {
            onCollapseFab()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onCollapseFab() }
        ) {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)) {

                // 🔹 Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.bz_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.width(70.dp),
                        contentScale = ContentScale.Fit
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("BuzzConnect", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { /* TODO */ }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                // 🔹 Posts Feed
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 🔹 Create Post Prompt
                    item {
                        val interactionSource = remember { MutableInteractionSource() }
                        val scale by animateFloatAsState(
                            targetValue = if (interactionSource.collectIsPressedAsState().value) 1.03f else 1f,
                            label = "scale"
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { scaleX = scale; scaleY = scale }
                                .clickable(interactionSource = interactionSource, indication = null) {
                                    showPostDialog = true
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("what's new?", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }

                    // 🔹 Dynamic Posts
                    items(posts, key = { it.id }) { post ->
                        InstagramPostCard(post = post, profileImageMap = userProfileImageMap)
                    }
                }
            }

            // 🔹 Post Creation Dialog
            var selectedImageResId by remember { mutableStateOf<Int?>(null) }

            AnimatedVisibility(visible = showPostDialog, enter = fadeIn(), exit = fadeOut()) {
                Dialog(onDismissRequest = { showPostDialog = false }) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 6.dp,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Create Post", style = MaterialTheme.typography.titleLarge)

                            OutlinedTextField(
                                value = postContent,
                                onValueChange = { postContent = it },
                                label = { Text("What's on your mind?") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text("Select an image:", style = MaterialTheme.typography.titleSmall)

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(R.drawable.image1, R.drawable.image2, R.drawable.image3).forEach { resId ->
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = "Option",
                                        modifier = Modifier
                                            .size(80.dp)
                                            .border(
                                                width = if (selectedImageResId == resId) 2.dp else 0.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = MaterialTheme.shapes.small
                                            )
                                            .clickable { selectedImageResId = resId },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showPostDialog = false }) {
                                    Text("Cancel")
                                }
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = {
                                    if (postContent.isNotBlank()) {
                                        postViewModel.insert(
                                            Post(
                                                username = currentUsername,
                                                caption = postContent,
                                                imageResId = selectedImageResId
                                            )
                                        )
                                        postContent = ""
                                        selectedImageResId = null
                                        showPostDialog = false
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Post published successfully")
                                        }
                                    }
                                }) {
                                    Text("Publish")
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
fun InstagramPostCard(
    post: Post,
    profileImageMap: Map<String, Int?> // 👈 Add this param
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isLiked by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "PostCardHover"
    )

    val profileImageResId = profileImageMap[post.username] ?: R.drawable.profile_icon // fallback

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .heightIn(min = 240.dp)
    ) {
        Card(
            modifier = Modifier
                .matchParentSize()
                .offset(x = (-6).dp, y = 4.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {}

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(interactionSource = interactionSource, indication = null) {},
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column {
                // 👤 Top Profile Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp),
                        shadowElevation = 2.dp
                    ) {
                        Image(
                            painter = painterResource(id = profileImageResId),
                            contentDescription = "Profile Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = post.username,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                post.imageResId?.let { resId ->
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = "Post Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color(0xFF9C27B0) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { isLiked = !isLiked }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${post.username}: ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    Text(
                        text = post.caption,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}



