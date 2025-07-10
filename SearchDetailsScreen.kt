package com.example.social_rede_mobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.social_rede_mobile.data.PostViewModel
import com.example.social_rede_mobile.data.UserViewModel
import com.example.social_rede_mobile.data.Post // make sure you import your Post data class
import com.example.social_rede_mobile.R

@Composable
fun SearchDetailsScreen(
    username: String,
    userViewModel: UserViewModel,
    postViewModel: PostViewModel
) {
    val user by userViewModel.getUserByUsername(username).observeAsState()
    val allPosts by postViewModel.getAllPosts().collectAsState(initial = emptyList())

    val userPosts = allPosts.filter { it.username == username }

    if (user == null) {
        Text("Loading user...", modifier = Modifier.padding(16.dp))
        return
    }

    Column(modifier = Modifier.padding(16.dp)) {
        val imageResId = user?.profileImageResId ?: R.drawable.default_pfp
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("@${user!!.username}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(user!!.fullName ?: "No full name", fontSize = 16.sp)
        Text(user!!.bio ?: "No bio available", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(20.dp))

        Text("Posts by this user:", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

        LazyColumn {
            items(userPosts) { post: Post ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(post.caption)
                        post.imageResId?.let { imageId ->
                            Image(
                                painter = painterResource(id = imageId),
                                contentDescription = "Post Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
