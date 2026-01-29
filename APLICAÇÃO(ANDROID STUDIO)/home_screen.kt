package com.example.social_rede_mobile

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.social_rede_mobile.data.PostViewModel
import com.example.social_rede_mobile.data.User
import com.example.social_rede_mobile.data.UserViewModel
import com.example.social_rede_mobile.network.RetrofitInstance
import com.example.social_rede_mobile.network.models.CommentCreate
import com.example.social_rede_mobile.network.models.CommentOut
import com.example.social_rede_mobile.network.models.NotificationCreate
import com.example.social_rede_mobile.network.models.PostOut
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
// -------- Small helpers (same vibe as other screens) --------

@Composable
private fun BuzzBg(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF0B1220)))
            )
    ) { content() }
}

@Composable
private fun BuzzGlassCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    listState: LazyListState,
    onToggleTheme: () -> Unit,
    onCollapseFab: () -> Unit
) {
    var showCommentDialog by remember { mutableStateOf(false) }
    var selectedPostForComment by remember { mutableStateOf<PostOut?>(null) }
    var commentText by remember { mutableStateOf("") }
    var commentList by remember { mutableStateOf<List<CommentOut>>(emptyList()) }

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("buzz_prefs", Context.MODE_PRIVATE)
    val currentUsername = prefs.getString("username", "BuzzUser") ?: "BuzzUser"

    val postViewModel: PostViewModel = viewModel(
        factory = viewModelFactory { initializer { PostViewModel(context.applicationContext as Application) } }
    )

    // 🔥 Auto-refresh feed when coming back to HomeScreen
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                postViewModel.loadPostsFromApi(currentUsername)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Load posts when screen enters
    LaunchedEffect(currentUsername) {
        postViewModel.loadPostsFromApi(currentUsername)
    }

    // Posts from API
    val posts by postViewModel.apiPosts.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showPostDialog by remember { mutableStateOf(false) }
    var postContent by remember { mutableStateOf("") }
    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri.value = it
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) { }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->

        BuzzBg {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(18.dp)
            ) {

                // -------- HEADER (Glass) --------
                BuzzGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.bz_logo),
                                contentDescription = "Logo",
                                modifier = Modifier.width(64.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "BuzzConnect",
                                    fontSize = 18.sp,
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

                        // If you want: you can hook these buttons later
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "•",
                                color = Color(0xFFC7D2FE),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // -------- FEED CARD --------
                BuzzGlassCard(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {

                        // CREATE POST BUTTON (glass)
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showPostDialog = true },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0x0FFFFFFF)),
                                border = BorderStroke(1.dp, Color(0x1FFFFFFF)),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x196366F1)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add",
                                            tint = Color(0xFFC7D2FE)
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = "Create a post",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Share something with your followers…",
                                            color = Color(0xFF9CA3AF),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        // ---------- POSTS FROM API ----------
                        items(posts, key = { it.id }) { post ->
                            InstagramPostCard(
                                post = post,
                                userMap = emptyMap(),
                                onLikeToggle = { postId, liked, _ ->
                                    postViewModel.toggleLike(
                                        postId = postId,
                                        username = currentUsername,
                                        liked = liked
                                    )

                                    // SEND LIKE NOTIFICATION 🔥
                                    if (liked && post.username != currentUsername) {
                                        coroutineScope.launch {
                                            try {
                                                RetrofitInstance.api.sendNotification(
                                                    NotificationCreate(
                                                        id = java.util.UUID.randomUUID().toString(),
                                                        username = currentUsername,
                                                        message = "liked your post.",
                                                        type = "Like",
                                                        targetUsername = post.username,
                                                        timestamp = System.currentTimeMillis(),
                                                        seen = false
                                                    )
                                                )
                                            } catch (e: Exception) {
                                                android.util.Log.e("Notification", "Like error: ${e.message}")
                                            }
                                        }
                                    }

                                    // Refresh posts
                                    postViewModel.loadPostsFromApi(currentUsername)
                                },
                                onCommentClick = {
                                    selectedPostForComment = post
                                    showCommentDialog = true

                                    // Load comments from API
                                    coroutineScope.launch {
                                        commentList = RetrofitInstance.api.getComments(post.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // ---------- CREATE POST DIALOG (styled) ----------
            if (showPostDialog) {
                Dialog(onDismissRequest = { showPostDialog = false }) {
                    BuzzGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {

                        Text(
                            text = "Create Post",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        OutlinedTextField(
                            value = postContent,
                            onValueChange = { postContent = it },
                            label = { Text("What's on your mind?", color = Color(0xFF9CA3AF)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.SemiBold),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0x0FFFFFFF),
                                focusedContainerColor = Color(0x12FFFFFF),
                                unfocusedBorderColor = Color(0x22FFFFFF),
                                focusedBorderColor = Color(0x806366F1),
                                focusedLabelColor = Color(0xFFC7D2FE),
                                cursorColor = Color(0xFF6366F1),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x196366F1),
                                contentColor = Color(0xFFC7D2FE)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Select image", fontWeight = FontWeight.Black)
                        }

                        selectedImageUri.value?.let { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { showPostDialog = false }) {
                                Text("Cancel", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            }

                            Spacer(Modifier.width(10.dp))

                            Button(
                                onClick = {
                                    if (postContent.isNotBlank() && selectedImageUri.value != null) {

                                        postViewModel.createPostApi(
                                            username = currentUsername,
                                            caption = postContent,
                                            imageUri = selectedImageUri.value.toString()
                                        ) {
                                            postViewModel.loadPostsFromApi(currentUsername)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Publicado com sucesso!")
                                            }
                                        }

                                        postContent = ""
                                        selectedImageUri.value = null
                                        showPostDialog = false
                                    } else {
                                        Toast.makeText(context, "Fill caption + select image!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                            ) {
                                Text("Post", color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            // -------------------- COMMENT DIALOG (styled) --------------------
            if (showCommentDialog && selectedPostForComment != null) {
                Dialog(onDismissRequest = { showCommentDialog = false }) {
                    BuzzGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {

                        Text(
                            text = "Comments for @${selectedPostForComment!!.username}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            label = { Text("Write a comment…", color = Color(0xFF9CA3AF)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.SemiBold),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0x0FFFFFFF),
                                focusedContainerColor = Color(0x12FFFFFF),
                                unfocusedBorderColor = Color(0x22FFFFFF),
                                focusedBorderColor = Color(0x806366F1),
                                focusedLabelColor = Color(0xFFC7D2FE),
                                cursorColor = Color(0xFF6366F1),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showCommentDialog = false }) {
                                Text("Cancel", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            }

                            Spacer(Modifier.width(10.dp))

                            Button(
                                onClick = {
                                    if (commentText.isNotBlank()) {
                                        coroutineScope.launch {
                                            RetrofitInstance.api.createComment(
                                                CommentCreate(
                                                    postId = selectedPostForComment!!.id,
                                                    username = currentUsername,
                                                    text = commentText,
                                                    timestamp = System.currentTimeMillis()
                                                )
                                            )

                                            // SEND COMMENT NOTIFICATION 🔥
                                            if (selectedPostForComment!!.username != currentUsername) {
                                                coroutineScope.launch {
                                                    try {
                                                        RetrofitInstance.api.sendNotification(
                                                            NotificationCreate(
                                                                id = java.util.UUID.randomUUID().toString(),
                                                                username = currentUsername,
                                                                message = "commented on your post.",
                                                                type = "Comment",
                                                                targetUsername = selectedPostForComment!!.username,
                                                                timestamp = System.currentTimeMillis(),
                                                                seen = false
                                                            )
                                                        )
                                                    } catch (e: Exception) {
                                                        android.util.Log.e("Notification", "Comment error: ${e.message}")
                                                    }
                                                }
                                            }

                                            commentList = RetrofitInstance.api.getComments(selectedPostForComment!!.id)
                                            postViewModel.loadPostsFromApi(currentUsername)
                                            commentText = ""
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                            ) {
                                Text("Post", color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }

                        Divider(color = Color(0x1FFFFFFF))

                        Text(
                            text = "Comments",
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        if (commentList.isEmpty()) {
                            Text("No comments yet…", color = Color(0xFF94A3B8))
                        } else {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 320.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(commentList) { comment ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0x0FFFFFFF)),
                                        border = BorderStroke(1.dp, Color(0x1FFFFFFF)),
                                        elevation = CardDefaults.cardElevation(0.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    "@${comment.username}",
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White
                                                )
                                                Text(comment.text, color = Color(0xFFE5E7EB))
                                                Text(
                                                    java.text.SimpleDateFormat("HH:mm, dd MMM")
                                                        .format(java.util.Date(comment.timestamp)),
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF9CA3AF)
                                                )
                                            }

                                            if (comment.username == currentUsername ||
                                                selectedPostForComment!!.username == currentUsername
                                            ) {
                                                IconButton(onClick = {
                                                    coroutineScope.launch {
                                                        RetrofitInstance.api.deleteComment(comment.id)
                                                        commentList = RetrofitInstance.api.getComments(selectedPostForComment!!.id)
                                                        postViewModel.loadPostsFromApi(currentUsername)
                                                    }
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete comment",
                                                        tint = Color(0xFFEF4444)
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
        }
    }
}

@Composable
fun InstagramPostCard(
    post: PostOut,
    userMap: Map<String, User>,
    onLikeToggle: (postId: Int, liked: Boolean, delta: Int) -> Unit,
    onCommentClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    var isLiked by remember { mutableStateOf(post.isLiked) }
    var likeCount by remember { mutableStateOf(post.likeCount) }

    val cardBg by animateColorAsState(
        targetValue = if (isPressed) Color(0x14111827) else Color(0x0FFFFFFF),
        label = "card_bg"
    )

    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel(
        factory = viewModelFactory { initializer { UserViewModel(context.applicationContext as Application) } }
    )

    val userState = remember(post.username) { userViewModel.loadUserApi(post.username) }
    val user by userState.collectAsState()

    val profilePainter = when {
        !user?.profileImageUri.isNullOrBlank() ->
            rememberAsyncImagePainter(Uri.parse(user!!.profileImageUri))

        user?.profileImageResId != null ->
            painterResource(id = user!!.profileImageResId!!)

        else -> painterResource(id = R.drawable.profile_icon)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null) { },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, Color(0x1FFFFFFF)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {

            // 👤 Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F172A))
                        .border(2.dp, Color(0x336366F1), CircleShape)
                ) {
                    Image(
                        painter = profilePainter,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.username,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "BuzzConnect",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            // 📸 Post Image (rounded)
            val hasImage = !post.imageUri.isNullOrBlank() || post.imageResId != null
            if (hasImage) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(18.dp))
                ) {
                    when {
                        post.imageUri != null -> {
                            Image(
                                painter = rememberAsyncImagePainter(Uri.parse(post.imageUri)),
                                contentDescription = "Post Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        post.imageResId != null -> {
                            Image(
                                painter = painterResource(id = post.imageResId),
                                contentDescription = "Post Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            } else {
                Spacer(Modifier.height(6.dp))
            }

            // ❤️ Actions + counts
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    // Like
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color(0xFF6366F1) else Color(0xFF94A3B8),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable {
                                val toggled = !isLiked
                                val delta = if (toggled) 1 else -1
                                isLiked = toggled
                                likeCount += delta
                                onLikeToggle(post.id, toggled, delta)
                            }
                    )

                    Spacer(Modifier.width(18.dp))

                    // Comment
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onCommentClick() }
                    )

                    Spacer(Modifier.width(18.dp))

                    // Share
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Share",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "$likeCount likes",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9CA3AF)
                )

                if (post.commentCount > 0) {
                    Text(
                        text = "View all ${post.commentCount} comments",
                        color = Color(0xFFC7D2FE),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clickable { onCommentClick() }
                    )
                }
            }

            // 📝 Caption (clean + readable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "${post.username} ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = post.caption,
                    fontSize = 13.sp,
                    color = Color(0xFFE5E7EB)
                )
            }
        }
    }
}