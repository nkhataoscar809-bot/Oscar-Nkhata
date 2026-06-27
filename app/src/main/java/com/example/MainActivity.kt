package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.YovxViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: YovxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                YovxAppContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun YovxAppContainer(viewModel: YovxViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Determine if we should show the bottom navigation bar on this screen
    val showBottomNav = currentScreen in listOf("Feed", "Reels", "Messaging", "Marketplace", "Profile")

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                YovxBottomNavigationBar(
                    currentScreen = currentScreen,
                    onTabSelected = { screenName ->
                        if (screenName == "Profile") {
                            viewModel.selectedUserIdForView.value = "me"
                        }
                        viewModel.navigateTo(screenName)
                    }
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(YovxGlassBackgroundList))
        ) {
            // High quality visual transitions when switching tabs
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "screen_transitions"
            ) { screen ->
                val padModifier = if (showBottomNav) {
                    Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                } else {
                    Modifier
                }

                when (screen) {
                    "Feed" -> FeedScreen(viewModel = viewModel, modifier = padModifier)
                    "Reels" -> ReelsScreen(viewModel = viewModel, modifier = padModifier)
                    "Messaging" -> MessagingScreen(viewModel = viewModel, modifier = padModifier)
                    "Marketplace" -> MarketplaceScreen(viewModel = viewModel, modifier = padModifier)
                    "Profile" -> ProfileScreen(viewModel = viewModel, modifier = padModifier)
                    "Search" -> SearchScreen(viewModel = viewModel)
                    "PostCreator" -> PostCreatorScreen(viewModel = viewModel)
                    else -> FeedScreen(viewModel = viewModel, modifier = padModifier)
                }
            }
        }
    }
}

@Composable
fun YovxBottomNavigationBar(
    currentScreen: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xD9101014),
        tonalElevation = 0.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier
            .height(68.dp)
            .border(
                BorderStroke(
                    0.5.dp,
                    Brush.verticalGradient(listOf(Color(0x2BFFFFFF), Color(0x00FFFFFF)))
                ),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .testTag("bottom_nav_bar")
    ) {
        val navItems = listOf(
            Triple("Feed", Icons.Default.Home, Icons.Outlined.Home),
            Triple("Reels", Icons.Default.VideoLibrary, Icons.Outlined.VideoLibrary),
            Triple("Messaging", Icons.Default.ChatBubble, Icons.Outlined.ChatBubble),
            Triple("Marketplace", Icons.Default.Storefront, Icons.Outlined.Storefront),
            Triple("Profile", Icons.Default.Person, Icons.Outlined.Person)
        )

        navItems.forEach { (screen, filledIcon, outlinedIcon) ->
            val isActive = currentScreen == screen
            NavigationBarItem(
                selected = isActive,
                onClick = { onTabSelected(screen) },
                icon = {
                    Icon(
                        imageVector = if (isActive) filledIcon else outlinedIcon,
                        contentDescription = "$screen Tab",
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = screen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = YovxPrimary,
                    selectedTextColor = YovxPrimary,
                    unselectedIconColor = YovxTextSecondary,
                    unselectedTextColor = YovxTextSecondary,
                    indicatorColor = YovxGrey.copy(alpha = 0.4f)
                ),
                modifier = Modifier.testTag("nav_tab_$screen")
            )
        }
    }
}
