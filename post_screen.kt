package com.example.social_rede_mobile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.social_rede_mobile.data.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

//@Composable
//fun InstagramPostCard(post: Post) {
//    val interactionSource = remember { MutableInteractionSource() }
//    val isPressed by interactionSource.collectIsPressedAsState()
//    var isLiked by remember { mutableStateOf(false) }
//
//    val backgroundColor by animateColorAsState(
//        targetValue = if (isPressed) MaterialTheme.colorScheme.secondaryContainer
//        else MaterialTheme.colorScheme.surfaceVariant,
//        label = "CardHoverAnimation"
//    )
//
//    Box(
//        modifier = Modifier
//            .padding(horizontal = 16.dp, vertical = 12.dp)
//            .heightIn(min = 240.dp)
//    ) {
//        Card(
//            modifier = Modifier
//                .matchParentSize()
//                .offset(x = (-6).dp, y = 4.dp),
//            colors = CardDefaults.cardColors(containerColor = backgroundColor),
//            elevation = CardDefaults.cardElevation(2.dp)
//        ) {}
//
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable(interactionSource = interactionSource, indication = null) {},
//            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//            elevation = CardDefaults.cardElevation(6.dp)
//        ) {
//            Column {
//                // 🔹 Top Row - Profile Info
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(12.dp)
//                ) {
//                    Surface(
//                        shape = CircleShape,
//                        modifier = Modifier.size(40.dp),
//                        shadowElevation = 2.dp
//                    ) {
//                        // Default fallback profile background
//                        Box(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.width(10.dp))
//
//                    Text(
//                        text = post.username,
//                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
//                        color = MaterialTheme.colorScheme.onSurface
//                    )
//                }
//
//                // 🔹 Middle Content - Either Text or Image
//                Text(
//                    text = post.caption,
//                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 12.dp, vertical = 10.dp),
//                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//
//                // 🔹 Like / Comment / Share Row
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 12.dp, vertical = 8.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
//                        contentDescription = "Like",
//                        tint = if (isLiked) Color(0xFF9C27B0) else MaterialTheme.colorScheme.onSurfaceVariant,
//                        modifier = Modifier.clickable { isLiked = !isLiked }
//                    )
//                    Spacer(modifier = Modifier.width(16.dp))
//                    Icon(
//                        imageVector = Icons.Default.ChatBubbleOutline,
//                        contentDescription = "Comment",
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Spacer(modifier = Modifier.width(16.dp))
//                    Icon(
//                        imageVector = Icons.Default.Send,
//                        contentDescription = "Share",
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//
//                // 🔹 Optional Styled Caption (if image was there originally)
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 12.dp, vertical = 8.dp)
//                ) {
//                    Text(
//                        text = "${post.username}: ",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Red
//                    )
//                    Text(
//                        text = post.caption,
//                        fontSize = 18.sp,
//                        color = MaterialTheme.colorScheme.onSurface
//                    )
//                }
//            }
//        }
//    }
//}
//
//// Optional: If you use remote image URLs
//@Composable
//fun RemoteImage(url: String, modifier: Modifier = Modifier) {
//    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
//
//    LaunchedEffect(url) {
//        bitmap = loadBitmapFromUrl(url)
//    }
//
//    bitmap?.let {
//        Image(
//            bitmap = it.asImageBitmap(),
//            contentDescription = null,
//            modifier = modifier
//        )
//    } ?: Box(
//        modifier = modifier,
//        contentAlignment = Alignment.Center
//    ) {
//        Text("Loading...")
//    }
//}
//
//suspend fun loadBitmapFromUrl(url: String): Bitmap? {
//    return withContext(Dispatchers.IO) {
//        try {
//            val stream = URL(url).openStream()
//            BitmapFactory.decodeStream(stream)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//}
