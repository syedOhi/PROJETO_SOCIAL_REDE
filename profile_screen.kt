package com.example.social_rede_mobile

import android.app.Application
import android.content.Context
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
import com.example.social_rede_mobile.data.PostViewModel
import com.example.social_rede_mobile.data.UserViewModel

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
    val userProfileImageMap = remember(allUsers) {
        allUsers.associate { it.username to it.profileImageResId }
    }

    val allPosts by postViewModel.posts.observeAsState(emptyList())
    val userPosts = allPosts.filter { it.username == currentUsername }


    val currentUser by userViewModel.getUserByUsername(currentUsername).observeAsState()

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
                        if (it.profileImageResId != null) {
                            Image(
                                painter = painterResource(id = it.profileImageResId),
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(Modifier.fillMaxSize().background(Color.Gray)) // fallback background
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

        // 📊 Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem("Posts", userPosts.size.toString())
            StatItem("Followers", "69.69k")
            StatItem("Following", "1")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "As Minhas Publicações",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 🔁 List of user's posts
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(userPosts, key = { it.id }) { post ->
                InstagramPostCard(post = post, profileImageMap = userProfileImageMap)
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
