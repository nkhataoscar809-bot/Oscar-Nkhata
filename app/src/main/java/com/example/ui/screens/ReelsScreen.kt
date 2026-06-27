package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.database.PostEntity
import com.example.data.network.GeminiService
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.*
import com.example.viewmodel.YovxViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReelsScreen(
    viewModel: YovxViewModel,
    modifier: Modifier = Modifier
) {
    val videoPosts by viewModel.videoPosts.collectAsState()
    val activePostIdForComments by viewModel.activePostIdForComments.collectAsState()
    val reelsFeedMode by viewModel.reelsFeedMode.collectAsState()
    var showMediaRecorderStudio by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(pageCount = { videoPosts.size })

    LaunchedEffect(pagerState.currentPage, videoPosts) {
        if (videoPosts.isNotEmpty() && pagerState.currentPage in videoPosts.indices) {
            val currentPost = videoPosts[pagerState.currentPage]
            viewModel.recordUserInteraction(currentPost.id, currentPost.userId, "view")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("reels_screen")
    ) {
        if (videoPosts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = YovxPrimary)
            }
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val post = videoPosts[page]
                ReelPageItem(
                    post = post,
                    viewModel = viewModel
                )
            }
        }

        // --- Top Feed Selector Toggle (Recommended vs Recent) ---
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Recommended" to "🔥 For You", "Recent" to "🕒 Recent").forEach { (mode, label) ->
                    val isSelected = reelsFeedMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) YovxPrimary else Color.Transparent)
                            .clickable {
                                viewModel.reelsFeedMode.value = mode
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- Floating RECORD Trigger Button at Top-Right ---
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16.dp, end = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(BorderStroke(1.5.dp, YovxPrimary), RoundedCornerShape(12.dp))
                    .clickable { showMediaRecorderStudio = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("record_studio_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse_dot")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = alpha))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Capture Reel",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RECORD",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- Comments overlay ---
        if (activePostIdForComments != null) {
            CommentsDialog(
                viewModel = viewModel,
                postId = activePostIdForComments!!,
                onDismiss = { viewModel.activePostIdForComments.value = null }
            )
        }

        // --- MediaRecorder Capture Studio Overlay Dialog ---
        if (showMediaRecorderStudio) {
            MediaRecorderStudioDialog(
                viewModel = viewModel,
                onDismiss = { showMediaRecorderStudio = false }
            )
        }
    }
}

@Composable
fun ReelPageItem(
    post: PostEntity,
    viewModel: YovxViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    var showHeartAnimation by remember { mutableStateOf(false) }
    var heartOffset by remember { mutableStateOf(Offset(0f, 0f)) }

    // Floating reaction animations state
    var reactionsList by remember { mutableStateOf(listOf<FloatingReaction>()) }
    var showEmojiSelector by remember { mutableStateOf(false) }

    // Double tap heart gesture values
    var isMuted by remember { mutableStateOf(false) }

    // TikTok video settings simulation values
    var beautyLevel by remember { mutableFloatStateOf(0.8f) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var currentVoiceFilter by remember { mutableStateOf("None") }
    var showFilterPanel by remember { mutableStateOf(false) }

    // Continuous music disc rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "disc_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_rotation_degree"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        heartOffset = tapOffset
                        showHeartAnimation = true
                        if (!post.isLiked) {
                            viewModel.toggleLike(post)
                        }
                        coroutineScope.launch {
                            delay(800)
                            showHeartAnimation = false
                        }
                    },
                    onTap = {
                        isMuted = !isMuted
                    }
                )
            }
    ) {
        // 1. Immersive Video Simulation Background (Beautiful scenery image representing the reel)
        val mockBackground = when (post.mediaUrl) {
            "Kyoto Rain Reels" -> "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=800"
            "Gourmet Shrimp Reels" -> "https://images.unsplash.com/photo-1559742811-8241323f2678?w=800"
            else -> "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800"
        }

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(mockBackground)
                .crossfade(true)
                .build(),
            contentDescription = "Reels video stream simulation",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Simulated overlay filter effect indicator
        if (beautyLevel > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.15f * beautyLevel)),
                            radius = 1200f
                        )
                    )
            )
        }

        // Left Side AI Filter overlay banner indicator (TikTok spec)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Text(
                text = "Reels",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .clickable { showFilterPanel = !showFilterPanel }
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Simulated Effects",
                    tint = YovxGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "FX: Smooth 80% • ${playbackSpeed}x",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Right Side Social Interaction Menu (Avatar, Likes, Comments, Shares)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Avatar circle with small red follow plus button
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(50.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.userAvatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Creator Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(YovxPrimary)
                        .border(1.dp, Color.Black, CircleShape)
                        .align(Alignment.BottomCenter),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Follow Creator",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Likes/Heart
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { viewModel.toggleLike(post) },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .size(42.dp)
                ) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Filled.Favorite,
                        contentDescription = "Like Reel",
                        tint = if (post.isLiked) YovxPrimary else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "${post.likesCount}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Comments Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { viewModel.activePostIdForComments.value = post.id },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ModeComment,
                        contentDescription = "Open Comments",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "${post.commentsCount}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Bookmark Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { viewModel.toggleBookmark(post) },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = "Bookmark Reel",
                        tint = if (post.isBookmarked) YovxGold else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "Save",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Share / Repost Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { viewModel.toggleRepost(post) },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Reply,
                        contentDescription = "Share Reel",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "${post.sharesCount}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // React Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { showEmojiSelector = !showEmojiSelector },
                    modifier = Modifier
                        .background(
                            if (showEmojiSelector) YovxPrimary else Color.Black.copy(alpha = 0.4f), 
                            CircleShape
                        )
                        .size(42.dp)
                        .testTag("react_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mood,
                        contentDescription = "React with Emoji",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "React",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Spinning Vinyl Record Music Disk
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(36.dp)
                    .rotate(rotation)
                    .background(Color.DarkGray, CircleShape)
                    .border(3.dp, Color.Black, CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.userAvatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Music art disc",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(20.dp).clip(CircleShape)
                )
            }
        }

        // Bottom Details Panel (Username, Caption, Music Ticker)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                        startY = 0f
                    )
                )
                .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
        ) {
            // Username + Verification
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Text(
                    text = "@${post.username}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                VerificationBadge(isVerified = post.isUserVerified, isCreator = true)
            }

            // Machine Learning Recommendation Insight Chip
            val interactionsState = viewModel.allInteractions.collectAsState()
            val insight = remember(post, interactionsState.value) {
                viewModel.getRecommendationInsight(post)
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(YovxPrimary.copy(alpha = 0.15f))
                    .border(BorderStroke(1.dp, YovxPrimary.copy(alpha = 0.4f)), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "ML Recommendation",
                    tint = YovxPrimary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${insight.first}% Match • ${insight.second}",
                    color = YovxPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            // Caption
            Text(
                text = post.content,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Music Ticker Txt
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Scrolling Audio TRACK",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Original Sound - @${post.username}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Audio Mute Visual Indicator
        AnimatedVisibility(
            visible = isMuted,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeOff,
                    contentDescription = "Muted",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Double Tap Hearts Burst simulation
        if (showHeartAnimation) {
            Box(
                modifier = Modifier
                    .offset(
                        x = (heartOffset.x / 3).dp, // basic mapping coordinate representing viewport offset scaling
                        y = (heartOffset.y / 3).dp
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Heart burst",
                    tint = YovxPrimary,
                    modifier = Modifier
                        .size(80.dp)
                        .testTag("double_tap_heart")
                )
            }
        }

        // Display all active floating emoji reactions overlayed on the video
        reactionsList.forEach { reaction ->
            key(reaction.id) {
                FloatingReactionEffect(
                    emoji = reaction.emoji,
                    onAnimationEnd = {
                        reactionsList = reactionsList.filter { it.id != reaction.id }
                    }
                )
            }
        }

        // Horizontal Emoji Selector popup bar
        AnimatedVisibility(
            visible = showEmojiSelector,
            enter = fadeIn() + slideInHorizontally { it },
            exit = fadeOut() + slideOutHorizontally { it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 150.dp, end = 64.dp) // Anchored right next to the React button
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = YovxCharcoal.copy(alpha = 0.92f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val emojisList = listOf("❤️", "🔥", "😂", "😮", "👏", "🎉", "💯", "🙌")
                    emojisList.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    val newReaction = FloatingReaction(
                                        id = System.nanoTime(),
                                        emoji = emoji
                                    )
                                    reactionsList = reactionsList + newReaction
                                    viewModel.recordUserInteraction(post.id, post.userId, "react")
                                }
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 24.sp
                            )
                        }
                    }
                }
            }
        }

        // Interactive AI Filter Adjuster Panel
        if (showFilterPanel) {
            Dialog(onDismissRequest = { showFilterPanel = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    colors = CardDefaults.cardColors(containerColor = YovxCharcoal),
                    border = BorderStroke(1.dp, YovxGrey)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Reel FX Video Editor",
                                color = YovxTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            IconButton(onClick = { showFilterPanel = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Panel",
                                    tint = YovxTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Beauty smoothness bar
                        Text(
                            text = "Skin Smoothness AI Filter: ${(beautyLevel * 100).toInt()}%",
                            color = YovxTextSecondary,
                            fontSize = 12.sp
                        )
                        Slider(
                            value = beautyLevel,
                            onValueChange = { beautyLevel = it },
                            colors = SliderDefaults.colors(
                                thumbColor = YovxPrimary,
                                activeTrackColor = YovxPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Playback speed selector (0.5x, 1x, 2x)
                        Text(
                            text = "Playback Speed Speed Controller",
                            color = YovxTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(0.5f, 1.0f, 2.0f).forEach { speed ->
                                Button(
                                    onClick = { playbackSpeed = speed },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (playbackSpeed == speed) YovxPrimary else YovxGrey
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = "${speed}x", fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Voice changer selection
                        Text(
                            text = "Voice Changer AI Filter",
                            color = YovxTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("None", "Chipmunk", "Deep Bass").forEach { filter ->
                                Button(
                                    onClick = { currentVoiceFilter = filter },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (currentVoiceFilter == filter) YovxAccent else YovxGrey
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = filter, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun MediaRecorderStudioDialog(
    viewModel: YovxViewModel,
    onDismiss: () -> Unit
) {
    // MediaRecorder simulation states
    var recordingState by remember { mutableStateOf("IDLE") } // IDLE, RECORDING, PROCESSING, FINISHED
    var recordedSeconds by remember { mutableStateOf(0) }
    var activeFeedIndex by remember { mutableStateOf(0) }
    var activeFilter by remember { mutableStateOf("None") }
    var audioBitrate by remember { mutableFloatStateOf(256f) }
    var encodingFormat by remember { mutableStateOf("webm") }
    var zoomLevel by remember { mutableStateOf("1x") }
    var flashMode by remember { mutableStateOf("OFF") }
    var lensMode by remember { mutableStateOf("REAR") }
    var audioMeterValue by remember { mutableFloatStateOf(0.1f) }
    var frameTick by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(50)
            frameTick += 0.05f
            if (frameTick > 100f) frameTick = 0f
        }
    }
    
    // Caption & Gemini AI helper states
    var captionText by remember { mutableStateOf("") }
    var targetStyle by remember { mutableStateOf("Viral Reel Style") }
    var generatedCaption by remember { mutableStateOf("") }
    var isGeminiLoading by remember { mutableStateOf(false) }
    var suggestedHashtags by remember { mutableStateOf<List<String>>(emptyList()) }
    var isHashtagsLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val cameraFeeds = listOf(
        Triple("Kyoto Rain Reels", "Scenic streets of Kyoto in the rain, paper lanterns glowing, peaceful ambient look", "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=800"),
        Triple("Gourmet Shrimp Reels", "Sizzling Garlic Butter Shrimps cooking, Michelin star quality, steaming pan closeup", "https://images.unsplash.com/photo-1559742811-8241323f2678?w=800"),
        Triple("CyberGlass Review", "CyberGlass XR headset unboxing, futuristic neon lights, hands-on gesture demos", "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800"),
        Triple("Sunset Sailing", "Luxury yacht cruising through calm golden waters, gorgeous warm sky breeze", "https://images.unsplash.com/photo-1505080856163-3a909b77440b?w=800"),
        Triple("Tokyo Crossing", "Time-lapse of Tokyo Shibuya crossing at night, giant dynamic billboards", "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=800")
    )

    val currentFeed = cameraFeeds[activeFeedIndex]

    // Recording timer loop
    LaunchedEffect(recordingState) {
        if (recordingState == "RECORDING") {
            recordedSeconds = 0
            while (recordingState == "RECORDING" && recordedSeconds < 15) {
                delay(1000)
                if (recordingState == "RECORDING") {
                    recordedSeconds += 1
                    if (recordedSeconds >= 15) {
                        recordingState = "PROCESSING"
                    }
                }
            }
        }
    }

    // Audio input waveform meter loop
    LaunchedEffect(recordingState) {
        if (recordingState == "RECORDING") {
            while (recordingState == "RECORDING") {
                delay(100)
                audioMeterValue = Random.nextFloat() * (0.95f - 0.15f) + 0.15f
            }
        } else {
            audioMeterValue = 0.05f
        }
    }

    // Processing delay loop
    LaunchedEffect(recordingState) {
        if (recordingState == "PROCESSING") {
            delay(1800)
            recordingState = "FINISHED"
        }
    }

    Dialog(onDismissRequest = { 
        if (recordingState != "RECORDING" && recordingState != "PROCESSING") {
            onDismiss()
        }
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = YovxObsidian),
            border = BorderStroke(1.5.dp, YovxPrimary.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header with Studio Title and Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (recordingState == "RECORDING") Color.Red else YovxGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Yovx MediaRecorder Studio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        enabled = recordingState != "RECORDING" && recordingState != "PROCESSING"
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Recorder",
                            tint = YovxTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Viewfinder Card representing active camera lens view
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, YovxGrey), RoundedCornerShape(12.dp))
                        .background(Color.Black)
                ) {
                    // Camera feed imagery
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentFeed.third)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Active Lens Stream Viewfinder",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Real-time Canvas Filter Overlay mapping directly to Canvas API instructions!
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Drawing logic for our real-time video filters!
                        when (activeFilter) {
                            "Cinematic Gold" -> {
                                // 1. Warm Golden Hue Color Wash
                                drawRect(color = Color(0x2BFFD54F))

                                // 2. Dark vignette falloff
                                val vignetteBrush = Brush.radialGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                                    center = center,
                                    radius = size.maxDimension / 1.3f
                                )
                                drawRect(brush = vignetteBrush)

                                // 3. Vintage film noise / scratches
                                val rand = Random((frameTick * 1000).toInt().coerceAtLeast(1))
                                repeat(4) {
                                    val startX = rand.nextFloat() * size.width
                                    val startY = rand.nextFloat() * size.height
                                    val length = rand.nextFloat() * 22f + 8f
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.22f),
                                        start = Offset(startX, startY),
                                        end = Offset(startX + length * 0.15f, startY + length),
                                        strokeWidth = 1.2f
                                    )
                                }
                            }
                            "Cyber Neon" -> {
                                // 1. Cyan & Magenta Dual Hue Tint
                                drawRect(color = Color(0x1CE040FB))

                                // 2. Cyber grid lines (horizontal and vertical scanlines representing digital pixel mesh)
                                val gridSpacing = 48f
                                var curX = 0f
                                while (curX < size.width) {
                                    drawLine(
                                        color = Color(0x1200E5FF),
                                        start = Offset(curX, 0f),
                                        end = Offset(curX, size.height),
                                        strokeWidth = 1f
                                    )
                                    curX += gridSpacing
                                }

                                val scanlineSpacing = 14f
                                val offsetShift = (frameTick * 60) % scanlineSpacing
                                var curY = offsetShift
                                while (curY < size.height) {
                                    drawLine(
                                        color = Color(0x22E040FB),
                                        start = Offset(0f, curY),
                                        end = Offset(size.width, curY),
                                        strokeWidth = 1.5f
                                    )
                                    curY += scanlineSpacing
                                }

                                // 3. Random horizontal digital glitch flickering banners
                                val rand = Random((frameTick * 3000).toInt().coerceAtLeast(1))
                                if (rand.nextFloat() > 0.65f) {
                                    val glitchY = rand.nextFloat() * size.height
                                    val glitchH = rand.nextFloat() * 12f + 3f
                                    drawRect(
                                        color = Color(0x3300FFCC),
                                        topLeft = Offset(0f, glitchY),
                                        size = androidx.compose.ui.geometry.Size(size.width, glitchH)
                                    )
                                }
                            }
                            "Retro VHS" -> {
                                // 1. Analog CRT TV high-frequency scanline pattern
                                var yVhs = 0f
                                while (yVhs < size.height) {
                                    drawLine(
                                        color = Color.Black.copy(alpha = 0.18f),
                                        start = Offset(0f, yVhs),
                                        end = Offset(size.width, yVhs),
                                        strokeWidth = 1f
                                    )
                                    yVhs += 5f
                                }

                                // 2. Animated tracking error / horizontal snow line drifting from top to bottom
                                val trackingY = (frameTick * 110) % (size.height + 40) - 20
                                drawRect(
                                    color = Color.White.copy(alpha = 0.35f),
                                    topLeft = Offset(0f, trackingY),
                                    size = androidx.compose.ui.geometry.Size(size.width, 3.5f)
                                )

                                // Random salt & pepper grain noise particles around tracking line
                                val rand = Random((frameTick * 4000).toInt().coerceAtLeast(1))
                                repeat(35) {
                                    val noiseX = rand.nextFloat() * size.width
                                    val noiseY = trackingY + (rand.nextFloat() * 24f - 12f)
                                    if (noiseY >= 0 && noiseY <= size.height) {
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.7f),
                                            radius = rand.nextFloat() * 1.5f + 0.5f,
                                            center = Offset(noiseX, noiseY)
                                        )
                                    }
                                }

                                // VHS vignette tint wash
                                drawRect(color = Color(0x1800E5FF))
                            }
                            "Night Vision" -> {
                                // 1. Pulsing green monochrome night filter
                                val baseGreenAlpha = 0.28f + (kotlin.math.sin(frameTick * 4f).toFloat() * 0.05f)
                                drawRect(color = Color(0xFF00FF00).copy(alpha = baseGreenAlpha))

                                // 2. Concentric sonar/scope grid lines in the center of the viewfinder
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                
                                drawCircle(
                                    color = Color(0x3D00FF00),
                                    radius = 35f,
                                    center = Offset(centerX, centerY),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                                )
                                drawCircle(
                                    color = Color(0x2900FF00),
                                    radius = 90f,
                                    center = Offset(centerX, centerY),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                                )

                                // Scope Crosshairs
                                drawLine(
                                    color = Color(0x2900FF00),
                                    start = Offset(centerX - 120f, centerY),
                                    end = Offset(centerX + 120f, centerY),
                                    strokeWidth = 1f
                                )
                                drawLine(
                                    color = Color(0x2900FF00),
                                    start = Offset(centerX, centerY - 80f),
                                    end = Offset(centerX, centerY + 80f),
                                    strokeWidth = 1f
                                )

                                // 3. Dynamic grainy static noise particles distributed over full canvas
                                val rand = Random((frameTick * 5000).toInt().coerceAtLeast(1))
                                repeat(55) {
                                    val dotX = rand.nextFloat() * size.width
                                    val dotY = rand.nextFloat() * size.height
                                    drawCircle(
                                        color = Color(0x5E00FF00),
                                        radius = 1f,
                                        center = Offset(dotX, dotY)
                                    )
                                }
                            }
                            "Thermal Glow" -> {
                                // False-color thermal vision backdrop (Deep navy / dark indigo overlay)
                                drawRect(color = Color(0x3F0F172A))

                                // Draw simulated false-color heat blobs (glowing orange/yellow/red circles with translucent gradients)
                                val blobs = when (activeFeedIndex) {
                                    0 -> listOf( // Kyoto Lanterns - warm glow
                                        Triple(Offset(size.width * 0.35f, size.height * 0.35f), 45f, Color(0xFFFF5722)),
                                        Triple(Offset(size.width * 0.72f, size.height * 0.3f), 55f, Color(0xFFFF9800)),
                                        Triple(Offset(size.width * 0.52f, size.height * 0.55f), 35f, Color(0xFFFFC107))
                                    )
                                    1 -> listOf( // Shrimp - sizzling pan center
                                        Triple(Offset(size.width * 0.5f, size.height * 0.52f), 85f, Color(0xFFD32F2F)),
                                        Triple(Offset(size.width * 0.44f, size.height * 0.48f), 65f, Color(0xFFFF5722)),
                                        Triple(Offset(size.width * 0.54f, size.height * 0.56f), 55f, Color(0xFFFFEB3B))
                                    )
                                    2 -> listOf( // CyberGlass - headset glowing pinks
                                        Triple(Offset(size.width * 0.38f, size.height * 0.42f), 70f, Color(0xFF9C27B0)),
                                        Triple(Offset(size.width * 0.58f, size.height * 0.46f), 65f, Color(0xFF00BCD4)),
                                        Triple(Offset(size.width * 0.48f, size.height * 0.44f), 45f, Color(0xFFE91E63))
                                    )
                                    3 -> listOf( // Sunset Sailing - orange sails & sun heat
                                        Triple(Offset(size.width * 0.82f, size.height * 0.28f), 95f, Color(0xFFFF9800)),
                                        Triple(Offset(size.width * 0.48f, size.height * 0.62f), 75f, Color(0xFFFF5722)),
                                        Triple(Offset(size.width * 0.18f, size.height * 0.48f), 40f, Color(0xFFD32F2F))
                                    )
                                    else -> listOf( // Tokyo Crossing - car lights & billboards
                                        Triple(Offset(size.width * 0.22f, size.height * 0.68f), 45f, Color(0xFFFFEB3B)),
                                        Triple(Offset(size.width * 0.78f, size.height * 0.62f), 50f, Color(0xFFF44336)),
                                        Triple(Offset(size.width * 0.48f, size.height * 0.28f), 75f, Color(0xFFFF9800))
                                    )
                                }

                                blobs.forEach { (center, radius, color) ->
                                    drawCircle(
                                        color = color.copy(alpha = 0.15f),
                                        radius = radius * 1.8f,
                                        center = center
                                    )
                                    drawCircle(
                                        color = color.copy(alpha = 0.38f),
                                        radius = radius * 1.3f,
                                        center = center
                                    )
                                    drawCircle(
                                        color = color.copy(alpha = 0.68f),
                                        radius = radius * 0.8f,
                                        center = center
                                    )
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.6f),
                                        radius = radius * 0.35f,
                                        center = center
                                    )
                                }
                            }
                            else -> {}
                        }
                    }

                    // Overlay HUD elements (Simulated status overlay)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        // Top HUD (Flash, Zoom, Lens indicator)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = "Flash mode",
                                    tint = if (flashMode != "OFF") YovxGold else Color.White,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            flashMode = when (flashMode) {
                                                "OFF" -> "ON"
                                                "ON" -> "AUTO"
                                                else -> "OFF"
                                            }
                                        }
                                )
                                Text(text = flashMode, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "LENS: $lensMode",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                        .clickable {
                                            lensMode = if (lensMode == "REAR") "FRONT" else "REAR"
                                        }
                                )
                                Text(
                                    text = "ZOOM: $zoomLevel",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                        .clickable {
                                            zoomLevel = when (zoomLevel) {
                                                "1x" -> "2x"
                                                "2x" -> "5x"
                                                else -> "1x"
                                            }
                                        }
                                )
                            }
                        }

                        // Bottom-Left Status: REC indicator, active resolution, and timers
                        Column(
                            modifier = Modifier.align(Alignment.BottomStart)
                        ) {
                            if (recordingState == "RECORDING") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.Red)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "● REC  0:${if (recordedSeconds < 10) "0" else ""}$recordedSeconds / 0:15",
                                        color = Color.Red,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = "STANDBY [MediaRecorder READY]",
                                    color = YovxGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "1080p @ 60 FPS • WebM stream buffer",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 9.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Bottom-Right audio input sampling meter
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .width(60.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "AUDIO",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            // Simple responsive level meter bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.DarkGray)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(audioMeterValue)
                                        .background(
                                            if (audioMeterValue > 0.8f) Color.Red else if (audioMeterValue > 0.5f) YovxGold else YovxGreen
                                        )
                                )
                            }
                        }
                    }

                    // Processing Encoder overlay
                    if (recordingState == "PROCESSING") {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.82f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = YovxPrimary, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(text = "MediaRecorder encoding pipeline...", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Text(text = "Merging audio tracks & compiling WebM stream", color = YovxTextSecondary, fontSize = 9.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Selector tabs for active simulator feed (so users can choose what video to record!)
                Text(
                    text = "Select Video Feed Source to Capture:",
                    color = YovxTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(cameraFeeds.size) { index ->
                        val feed = cameraFeeds[index]
                        val isSelected = index == activeFeedIndex
                        Card(
                            modifier = Modifier
                                .width(90.dp)
                                .clickable(enabled = recordingState == "IDLE") { activeFeedIndex = index },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) YovxPrimary.copy(alpha = 0.25f) else YovxCharcoal
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) YovxPrimary else Color.Transparent
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                ) {
                                    AsyncImage(
                                        model = feed.third,
                                        contentDescription = feed.first,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = feed.first.replace(" Reels", ""),
                                    color = YovxTextPrimary,
                                    fontSize = 8.sp,
                                    maxLines = 1,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row for setting filters and MediaRecorder configuration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Filters Column
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = "Live Cam FX Filter:",
                            color = YovxTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val filtersList = listOf("None", "Cinematic Gold", "Cyber Neon", "Retro VHS", "Night Vision", "Thermal Glow")
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(YovxCharcoal)
                                    .border(0.5.dp, YovxGrey, RoundedCornerShape(8.dp))
                                    .clickable {
                                        val nextIdx = (filtersList.indexOf(activeFilter) + 1) % filtersList.size
                                        activeFilter = filtersList[nextIdx]
                                    }
                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "FX",
                                        tint = if (activeFilter != "None") YovxGold else YovxTextSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = activeFilter,
                                        color = YovxTextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // MediaRecorder Configuration Column
                    Column(modifier = Modifier.weight(1.8f)) {
                        Text(
                            text = "MediaRecorder Configuration:",
                            color = YovxTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Target format selector
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(YovxCharcoal)
                                    .clickable {
                                        encodingFormat = if (encodingFormat == "webm") "mp4" else "webm"
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ".${encodingFormat.uppercase()}",
                                    color = YovxAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Audio sample bitrate selector
                            Box(
                                modifier = Modifier
                                    .weight(1.4f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(YovxCharcoal)
                                    .clickable {
                                        audioBitrate = when (audioBitrate) {
                                            128f -> 256f
                                            256f -> 320f
                                            else -> 128f
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${audioBitrate.toInt()} kbps mic",
                                    color = YovxTextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // The Big Record Action Controller Button
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (recordingState == "IDLE") {
                        // Start recording circle button
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .border(BorderStroke(4.dp, Color.White), CircleShape)
                                    .clickable { recordingState = "RECORDING" }
                                    .testTag("start_recording_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "TAP TO RECORD", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (recordingState == "RECORDING") {
                        // Stop recording circle button
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black)
                                    .border(BorderStroke(4.dp, Color.Red), CircleShape)
                                    .clickable { recordingState = "PROCESSING" }
                                    .testTag("stop_recording_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color.Red)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "TAP TO STOP & ENCODE", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // If recording is finished, show the publish flow & Gemini AI caption workspace!
                if (recordingState == "FINISHED") {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = YovxGrey, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Step 2: Generate Video Caption & Tags (Gemini AI)",
                            color = YovxAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = targetStyle,
                                onValueChange = { targetStyle = it },
                                label = { Text("Tone / Theme Style (e.g. Chill Vlog, Energetic, Cyberpunk)", fontSize = 10.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = YovxTextPrimary,
                                    unfocusedTextColor = YovxTextPrimary,
                                    focusedBorderColor = YovxAccent,
                                    unfocusedBorderColor = YovxGrey
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            )

                            Button(
                                onClick = {
                                    isGeminiLoading = true
                                    coroutineScope.launch {
                                        val prompt = """
                                            You are Yovx Gemini, a professional AI social Media Creator. We just recorded a video reel using a browser-simulated MediaRecorder API.
                                            
                                            VIDEO SPECS:
                                            - Video Title: ${currentFeed.first}
                                            - Visual Content: ${currentFeed.second}
                                            - Target Reel Tone: $targetStyle
                                            - Capture Filters: $activeFilter
                                            - Encoding Format: $encodingFormat
                                            
                                            Write an incredibly engaging, high-retention caption for this Reel:
                                            1. Start with a hyper-viral hook.
                                            2. Add 2 short, punchy paragraphs with rich emojis.
                                            3. Add 4-5 trending video hashtags.
                                            4. Keep the final output concise and complete.
                                        """.trimIndent()
                                        val aiCaption = GeminiService.generateText(prompt)
                                        generatedCaption = aiCaption
                                        captionText = aiCaption
                                        isGeminiLoading = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = YovxPrimary),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("gemini_generate_reels_caption"),
                                enabled = !isGeminiLoading
                            ) {
                                if (isGeminiLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Spark", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Ask AI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = captionText,
                            onValueChange = { captionText = it },
                            placeholder = { Text("Write or generate custom caption details...", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = YovxTextPrimary,
                                unfocusedTextColor = YovxTextPrimary,
                                focusedBorderColor = YovxAccent,
                                unfocusedBorderColor = YovxGrey
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // --- NEW: AI Trending Hashtag Generator Feature ---
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(BorderStroke(1.dp, YovxGrey), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = YovxCharcoal.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Hashtags Generator",
                                            tint = YovxAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "AI Suggested Hashtags",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    
                                    if (suggestedHashtags.isNotEmpty()) {
                                        TextButton(
                                            onClick = {
                                                val existingTags = captionText.split(Regex("\\s+")).filter { it.startsWith("#") }.toSet()
                                                val tagsToAdd = suggestedHashtags.filter { it !in existingTags }
                                                if (tagsToAdd.isNotEmpty()) {
                                                    val spacer = if (captionText.isEmpty() || captionText.endsWith(" ")) "" else " "
                                                    captionText += spacer + tagsToAdd.joinToString(" ")
                                                }
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Add All", color = YovxAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                if (isHashtagsLoading) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            color = YovxPrimary,
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("AI analyzing content & simulating trends...", color = YovxTextSecondary, fontSize = 11.sp)
                                    }
                                } else if (suggestedHashtags.isEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "No hashtags suggested yet.",
                                            color = YovxTextSecondary,
                                            fontSize = 11.sp
                                        )
                                        Button(
                                            onClick = {
                                                isHashtagsLoading = true
                                                coroutineScope.launch {
                                                    val prompt = """
                                                        You are an expert social media analyst for Yovx. Analyze this newly recorded video reel content and suggest exactly 6 highly relevant, trending hashtags.
                                                        
                                                        VIDEO METADATA:
                                                        - Title: ${currentFeed.first}
                                                        - Visual description: ${currentFeed.second}
                                                        - Tone Style: $targetStyle
                                                        - Captures FX filter: $activeFilter
                                                        
                                                        Response format: Return ONLY the hashtags, separated by commas (e.g. #kyoto, #rainyday, #peaceful, #vibe). Do not include any other text, markdown formatting, or introduction.
                                                    """.trimIndent()
                                                    try {
                                                        val responseText = GeminiService.generateText(prompt)
                                                        val tags = responseText.split(",")
                                                            .map { it.trim().replace(Regex("[^#a-zA-Z0-9_]"), "") }
                                                            .filter { it.isNotEmpty() }
                                                            .map { if (it.startsWith("#")) it else "#$it" }
                                                        suggestedHashtags = if (tags.isNotEmpty()) tags else listOf("#${currentFeed.first.replace(" ", "")}", "#YovxReels", "#Trending", "#Viral")
                                                    } catch (e: Exception) {
                                                        suggestedHashtags = listOf("#YovxReels", "#Trending", "#Viral", "#Creator", "#Studio")
                                                    } finally {
                                                        isHashtagsLoading = false
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = YovxPrimary.copy(alpha = 0.2f), contentColor = YovxAccent),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp).testTag("gemini_suggest_hashtags_button")
                                        ) {
                                            Text("Generate Tags", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    // FlowRow showing suggestion chips!
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        suggestedHashtags.forEach { tag ->
                                            val isAlreadyAdded = captionText.contains(tag)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(if (isAlreadyAdded) YovxAccent.copy(alpha = 0.15f) else YovxCharcoal)
                                                    .border(
                                                        1.dp,
                                                        if (isAlreadyAdded) YovxAccent else YovxGrey,
                                                        RoundedCornerShape(16.dp)
                                                    )
                                                    .clickable {
                                                        if (!isAlreadyAdded) {
                                                            val spacer = if (captionText.isEmpty() || captionText.endsWith(" ")) "" else " "
                                                            captionText += spacer + tag
                                                        } else {
                                                            captionText = captionText.replace(tag, "").replace(Regex("\\s+"), " ").trim()
                                                        }
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = tag,
                                                        color = if (isAlreadyAdded) YovxAccent else YovxTextPrimary,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    if (isAlreadyAdded) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Added",
                                                            tint = YovxAccent,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (captionText.trim().isNotEmpty()) {
                                    viewModel.publishNewPost(
                                        content = captionText,
                                        mediaUrl = currentFeed.first,
                                        mediaType = "video"
                                    )
                                    viewModel.navigateTo("Reels")
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YovxGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("publish_simulated_reel_button"),
                            enabled = captionText.trim().isNotEmpty()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Publish", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Publish to Yovx Reels Stream", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class FloatingReaction(
    val id: Long,
    val emoji: String
)

@Composable
fun FloatingReactionEffect(
    emoji: String,
    onAnimationEnd: () -> Unit
) {
    val animProgress = remember { androidx.compose.animation.core.Animatable(0f) }
    
    // Random path deviations
    val randomXOffsetRange = remember { (Random.nextFloat() * 120f - 60f) }
    val randomSwayFrequency = remember { (Random.nextFloat() * 2f + 1f) }
    val randomDuration = remember { Random.nextInt(1500, 2500) }
    
    LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = randomDuration, 
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            )
        )
        onAnimationEnd()
    }
    
    val progress = animProgress.value
    val yOffset = -progress * 450f // Rises up 450 pixels
    val xOffset = kotlin.math.sin(progress * Math.PI * randomSwayFrequency).toFloat() * randomXOffsetRange
    val scale = if (progress < 0.15f) {
        progress / 0.15f // Scale in
    } else {
        (1f - (progress - 0.15f) / 0.85f).coerceIn(0f, 1f) // Scale / fade out
    }
    val alpha = (1f - progress).coerceIn(0f, 1f)
    val rotation = progress * randomXOffsetRange // Rotates as it floats

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = emoji,
            fontSize = 38.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 140.dp, end = 24.dp) // Starts near the React button
                .offset(x = xOffset.dp, y = yOffset.dp)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    alpha = alpha,
                    rotationZ = rotation
                )
        )
    }
}
