package com.example.social_rede_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.social_rede_mobile.ui.theme.Social_rede_mobileTheme
import androidx.compose.ui.graphics.Color
//for downloading image on project addditional imports(optional)

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.List
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.social_rede_mobile.data.UserViewModel
import com.example.social_rede_mobile.ui.screens.NotificationScreen

import com.google.firebase.FirebaseApp //websocket

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ✅ Initialize Firebase
        FirebaseApp.initializeApp(this)
        setContent {
            var isFabExpanded by remember { mutableStateOf(false) }
            val userViewModel: UserViewModel = viewModel()

            var isDarkTheme by remember { mutableStateOf(false) } // 🔥 manually toggle

            Social_rede_mobileTheme(useDarkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val listState = rememberLazyListState()
                val currentRouteFromBackStack = navController.currentBackStackEntryAsState().value?.destination?.route

                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                val fabVisible = currentRoute == "home" &&
                        listState.firstVisibleItemIndex == 0 &&
                        listState.firstVisibleItemScrollOffset < 10



                var showBottomBar by remember { mutableStateOf(false) }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(navController = navController)
                        }

                    },
                    floatingActionButton = {
                        if (shouldShowBottomBar(navController)) {
                            ExpandableFABMenu(
                                navController = navController,
                                expanded = isFabExpanded,
                                onExpandedChange = { isFabExpanded = it },
                                isDarkTheme = isDarkTheme
                            )

                        }
                    }
                    ,


                    floatingActionButtonPosition = FabPosition.Center
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("messages") { PlaceholderScreen("Messages") }
                        composable("notifications") { PlaceholderScreen("Notifications") }

                        composable("login") { LoginScreen(navController) }
                        composable("register") { RegisterScreen(navController) }
                        composable("home") {
                            HomeScreen(
                                navController = navController,
                                listState = listState,
                                onToggleTheme = { isDarkTheme = !isDarkTheme },
                                onCollapseFab = { isFabExpanded = false } // ✅ collapses FAB
                            )

                        }
                        composable("details_screen") {
                            DetailScreen(navController = navController)
                        }

                        composable("createPost") { PlaceholderScreen("Create Post") }
                        composable("search") {
                            SearchScreen(userViewModel = userViewModel, navController = navController)

                            // 👈 Fix here
                        }
                        composable("search_details/{username}") { backStackEntry ->
                            val username = backStackEntry.arguments?.getString("username") ?: ""
                            SearchDetailsScreen(username = username, navController = navController)
                        }



                        composable("stories") { PlaceholderScreen("Stories") }
                        // ✅ UPDATED - Real Message Screen
                        composable("messages") {
                            MessageScreen(navController = navController)
                        }

                        // ✅ NEW - Chat Detail Route
                        composable("chat/{user}") { backStackEntry ->
                            val user = backStackEntry.arguments?.getString("user") ?: ""
                            ChatDetailScreen(user = user, navController = navController)
                        }
                        composable("notifications") {  NotificationScreen() }
                        composable("profile") {
                            ProfileScreen(
                                navController = navController,
                                onToggleTheme = { isDarkTheme = !isDarkTheme }
                            )
                        }


                        composable("edit_profile") { EditProfileScreen(navController) }                    }
                }
            }
        }

    }
}





//Must showing theb FAB Button
@Composable
fun shouldShowFAB(navController: NavController): Boolean {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    return currentRoute == "home"
}


// list of icons inside the FAB
@Composable
fun shouldShowBottomBar(navController: NavController): Boolean {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    return currentRoute in listOf(
        "home", "search", "stories", "messages", "notifications", "profile"
    )
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$name Screen", style = MaterialTheme.typography.headlineMedium)
    }
}








data class StoryUser(
    val username: String,

    )


data class Post(
    val username: String, //storing username in a string
    val caption: String,//storing caption in a string
    val hasImage: Boolean = true,// checking if we have any image
    val imageResId: Int? = null, // Resource ID of the image like getting the id(path) of image from drawable
    val profileImageResId: Int? = null
)




// to expand the fab(floating action button) when it get clicked "+"
@Composable
fun ExpandableFABMenu(
    navController: NavController,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    isDarkTheme: Boolean
)
 {
    val navItems = listOf(
        "home" to Icons.Default.Home,
        "search" to Icons.Default.Search,
        "messages" to Icons.Default.Email,
        "notifications" to Icons.Default.Notifications,
        "profile" to Icons.Default.Person
    )

     val fabBackgroundColor = if (isDarkTheme) Color.White else Color.Black
     val iconTintColor = if (isDarkTheme) Color.Black else Color.White



     val fabScale by animateFloatAsState(
        targetValue = if (expanded) 0.85f else 1.1f,
        animationSpec = tween(300)
    )

    val fabOffsetY by animateFloatAsState(
        targetValue = if (expanded) (-24).dp.value else 0f,
        animationSpec = tween(300)
    )

    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(300)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (expanded) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItems.forEach { (route, icon) ->
                        val scale = remember { Animatable(1f) }
                        val interactionSource = remember { MutableInteractionSource() }

                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect {
                                scale.animateTo(1.2f, tween(100))
                                scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            }
                        }

                        SmallFloatingActionButton(
                            onClick = {
                                navController.navigate(route)
                                onExpandedChange(false)
                            },
                            containerColor = fabBackgroundColor,
                            interactionSource = interactionSource,
                            modifier = Modifier.scale(scale.value)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = route,
                                tint = iconTintColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            if (!expanded) {
                FloatingActionButton(
                    onClick = { onExpandedChange(true) },
                    containerColor = fabBackgroundColor,
                    modifier = Modifier
                        .scale(fabScale)
                        .offset(y = fabOffsetY.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Toggle Menu",
                        tint = iconTintColor,
                        modifier = Modifier
                            .rotate(rotationAngle)
                            .size(28.dp)
                    )
                }
            }


        }
    }
}









