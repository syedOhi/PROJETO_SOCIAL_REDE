package com.example.social_rede_mobile

// 👇 ADD THESE IMPORTS
import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import com.example.social_rede_mobile.data.CommentViewModel
import com.example.social_rede_mobile.data.Comment
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import com.example.social_rede_mobile.data.User
import coil.compose.rememberAsyncImagePainter


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
    val commentViewModel: CommentViewModel = viewModel(
        factory = viewModelFactory {
            initializer { CommentViewModel(context.applicationContext as Application) }
        }
    )

    val posts by postViewModel.posts.observeAsState(emptyList())
    val allUsers by userViewModel.getAllUsers().collectAsState(initial = emptyList())
    val userMap = remember(allUsers) { allUsers.associateBy { it.username } }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showPostDialog by remember { mutableStateOf(false) }
    var postContent by remember { mutableStateOf("") }
    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri.value = it
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    var showCommentDialog by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var selectedPostForComment by remember { mutableStateOf<Post?>(null) }

    // 💬 Comment Dialog
    if (showCommentDialog && selectedPostForComment != null) {
        val comments by commentViewModel
            .getComments(selectedPostForComment!!.id)
            .collectAsState(initial = emptyList())

        Dialog(onDismissRequest = { showCommentDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Comment on @${selectedPostForComment!!.username}",
                        style = MaterialTheme.typography.headlineSmall)

                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { if (it.length <= 200) commentText = it },
                        placeholder = { Text("Escreva o seu comentário...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCommentDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            if (commentText.isNotBlank()) {
                                commentViewModel.insert(
                                    Comment(
                                        postId = selectedPostForComment!!.id,
                                        username = currentUsername,
                                        text = commentText
                                    )
                                )
                                commentText = ""
                                Toast.makeText(context, "Comment posted!", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("Post")
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("Comments", fontWeight = FontWeight.Bold)

                    if (comments.isEmpty()) {
                        Text("Nenhum comentário ainda...", color = Color.Gray)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                            items(comments) { comment ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("@${comment.username}", fontWeight = FontWeight.SemiBold)
                                        Text(comment.text)
                                        Text(
                                            java.text.SimpleDateFormat("HH:mm, dd MMM")
                                                .format(java.util.Date(comment.timestamp)),
                                            color = Color.Gray,
                                            fontSize = 11.sp
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
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                        IconButton(onClick = { /* TODO: notifications */ }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                                Text("então, esqueceste de me?", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }

                    items(posts, key = { it.id }) { post ->
                        InstagramPostCard(
                            post = post,
                            userMap = userMap,
                            onLikeToggle = { postId, liked, delta ->
                                postViewModel.updateLikeStatus(postId, liked, delta)
                            },
                            onCommentClick = {
                                selectedPostForComment = post
                                showCommentDialog = true
                            }
                        )
                    }
                }
            }

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

                            Button(onClick = {
                                imagePickerLauncher.launch("image/*")
                            }) {
                                Text("Select Image from Device")
                            }

                            selectedImageUri.value?.let { uri ->
                                Image(
                                    painter = rememberAsyncImagePainter(uri),
                                    contentDescription = "Selected Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { showPostDialog = false }) {
                                    Text("Cancel")
                                }
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = {
                                    if (postContent.isNotBlank() && selectedImageUri.value != null) {
                                        postViewModel.insert(
                                            Post(
                                                username = currentUsername,
                                                caption = postContent,
                                                imageUri = selectedImageUri.value.toString()
                                            )
                                        )
                                        postContent = ""
                                        selectedImageUri.value = null
                                        showPostDialog = false
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("foi publicada com sucesso")
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
    userMap: Map<String, User>,
    onLikeToggle: (postId: Int, liked: Boolean, delta: Int) -> Unit,
    onCommentClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    var isLiked by remember { mutableStateOf(post.isLiked) }
    var likeCount by remember { mutableStateOf(post.likeCount) }

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed)
            MaterialTheme.colorScheme.secondaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        label = "PostCardHover"
    )

    val user = userMap[post.username]

    val profilePainter = when {
        !user?.profileImageUri.isNullOrEmpty() -> {
            val uri = Uri.parse(user!!.profileImageUri)
            rememberAsyncImagePainter(uri)
        }
        user?.profileImageResId != null -> {
            painterResource(id = user.profileImageResId)
        }
        else -> painterResource(id = R.drawable.profile_icon)
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .heightIn(min = 240.dp)
    ) {
        // Background shadow card
        Card(
            modifier = Modifier
                .matchParentSize()
                .offset(x = (-6).dp, y = 4.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {}

        // Main content card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(interactionSource = interactionSource, indication = null) {},
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column {
                // 👤 Profile Header
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
                            painter = profilePainter,
                            contentDescription = "Profile Picture",
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

                // 🖼 Post Image from URI or drawable
                when {
                    post.imageUri != null -> {
                        val uri = Uri.parse(post.imageUri)
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Post Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .padding(bottom = 8.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    post.imageResId != null -> {
                        Image(
                            painter = painterResource(id = post.imageResId),
                            contentDescription = "Post Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .padding(bottom = 8.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // ❤️ Reactions Row
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color(0xFF9C27B0) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable {
                                val toggled = !isLiked
                                val delta = if (toggled) 1 else -1
                                isLiked = toggled
                                likeCount += delta
                                onLikeToggle(post.id, toggled, delta)
                            }
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comentar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable { onCommentClick() }
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "$likeCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // 📝 Caption
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



