package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.database.CommentEntity
import com.example.data.database.PostEntity
import com.example.data.database.StoryEntity
import com.example.ui.components.PostCard
import com.example.ui.components.StoryBubble
import com.example.ui.theme.*
import com.example.viewmodel.YovxViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: YovxViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.allPosts.collectAsState()
    val stories by viewModel.allStories.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val activePostIdForComments by viewModel.activePostIdForComments.collectAsState()

    var activeStoryToView by remember { mutableStateOf<StoryEntity?>(null) }

    Box(modifier = modifier.fillMaxSize().background(YovxObsidian)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Main App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(YovxCharcoal)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .border(0.5.dp, Color.Transparent),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Yovx",
                    color = YovxPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.testTag("app_logo")
                )
                Row {
                    IconButton(
                        onClick = { viewModel.navigateTo("PostCreator") },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(YovxGrey.copy(alpha = 0.5f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Post",
                            tint = YovxTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo("Search") },
                        modifier = Modifier
                            .background(YovxGrey.copy(alpha = 0.5f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Users/Posts",
                            tint = YovxTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("feed_scroll")
            ) {
                // Header Post Box ("What's on your mind?" - Facebook Layout)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .clickable { viewModel.navigateTo("PostCreator") },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x1F252530)),
                        border = BorderStroke(
                            1.dp,
                            Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x05FFFFFF)))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(currentUser?.avatarUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "My Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(YovxGrey.copy(alpha = 0.6f))
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "What's on your mind, ${currentUser?.displayName?.split(" ")?.firstOrNull() ?: "Oscar"}?",
                                    color = YovxTextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Add Media",
                                tint = YovxGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Instagram Stories Layout (Horizontal)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(YovxCharcoal)
                            .padding(vertical = 12.dp)
                            .border(0.5.dp, YovxGrey)
                    ) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // My Story Creator Bubble
                            item {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clickable { viewModel.navigateTo("PostCreator") }
                                ) {
                                    Box(
                                        modifier = Modifier.size(72.dp),
                                        contentAlignment = Alignment.BottomEnd
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(currentUser?.avatarUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150")
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "My Story Avatar",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .border(2.dp, YovxGrey, CircleShape)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(YovxPrimary)
                                                .border(1.5.dp, YovxCharcoal, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Create story",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Your Story",
                                        color = YovxTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Dynamic list of user stories
                            items(stories) { story ->
                                StoryBubble(
                                    username = story.username,
                                    avatarUrl = story.userAvatarUrl,
                                    isViewed = story.isViewed,
                                    onClick = {
                                        viewModel.markStoryAsViewed(story.id)
                                        activeStoryToView = story
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Social Posts Feed
                if (posts.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = YovxPrimary)
                        }
                    }
                } else {
                    items(posts) { post ->
                        PostCard(
                            post = post,
                            onLike = { viewModel.toggleLike(post) },
                            onCommentToggle = { viewModel.activePostIdForComments.value = post.id },
                            onRepost = { viewModel.toggleRepost(post) },
                            onBookmark = { viewModel.toggleBookmark(post) },
                            onVote = { optionIndex -> viewModel.submitPollVote(post, optionIndex) },
                            onTranslate = { onResult ->
                                viewModel.translatePost(post, "English", onResult)
                            },
                            onUserClick = {
                                viewModel.selectedUserIdForView.value = post.userId
                                viewModel.navigateTo("Profile")
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp)) // padding for navigation bar
                }
            }
        }

        // --- Comments Slide-Up Sheet Dialog ---
        if (activePostIdForComments != null) {
            CommentsDialog(
                viewModel = viewModel,
                postId = activePostIdForComments!!,
                onDismiss = { viewModel.activePostIdForComments.value = null }
            )
        }

        // --- Immersive Cinematic Story Viewer ---
        if (activeStoryToView != null) {
            StoryViewerDialog(
                story = activeStoryToView!!,
                onDismiss = { activeStoryToView = null }
            )
        }
    }
}

/**
 * Helper to format comments relative timestamps
 */
fun formatCommentTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 0 -> "Just now"
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> {
            val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
    }
}

@Composable
fun CommentItem(
    comment: CommentEntity,
    isReply: Boolean = false,
    onReplyClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(comment.userAvatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Commenter Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(if (isReply) 28.dp else 34.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.username,
                        color = YovxTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    if (comment.isVerified) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Commenter",
                            tint = YovxVerified,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatCommentTimestamp(comment.timestamp),
                        color = YovxTextSecondary,
                        fontSize = 10.sp
                    )
                }

                if (!isReply) {
                    Text(
                        text = "Reply",
                        color = YovxAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onReplyClick() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = comment.content,
                color = YovxTextPrimary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * Comments sliding overlay dialog simulating a bottom sheet.
 */
@Composable
fun CommentsDialog(
    viewModel: YovxViewModel,
    postId: Long,
    onDismiss: () -> Unit
) {
    val comments by viewModel.activePostComments.collectAsState()
    var textInput by remember { mutableStateOf("") }
    var replyingToComment by remember { mutableStateOf<CommentEntity?>(null) }

    val parentComments = remember(comments) { comments.filter { it.parentId == null } }
    val repliesMap = remember(comments) { comments.filter { it.parentId != null }.groupBy { it.parentId } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .clickable(enabled = false) {}, // stop ripple propagation
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xE6101016)),
                border = BorderStroke(
                    1.dp,
                    Brush.verticalGradient(listOf(Color(0x40FFFFFF), Color(0x02FFFFFF)))
                )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "Comments (${comments.size})",
                            color = YovxTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Comments",
                                tint = YovxTextSecondary
                            )
                        }
                    }

                    HorizontalDivider(color = YovxGrey)

                    // Comments list supporting threaded replies
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        if (parentComments.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No comments yet. Be the first to share your thoughts!",
                                        color = YovxTextSecondary,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            parentComments.forEach { parent ->
                                // Parent Comment Item
                                item(key = "comment_${parent.id}") {
                                    CommentItem(
                                        comment = parent,
                                        onReplyClick = { replyingToComment = parent }
                                    )
                                }

                                // Nested Replies
                                val replies = repliesMap[parent.id] ?: emptyList()
                                if (replies.isNotEmpty()) {
                                    items(replies, key = { "reply_${it.id}" }) { reply ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                        ) {
                                            Spacer(modifier = Modifier.width(28.dp))
                                            // Subtle vertical indicator line for reply thread visual context
                                            Box(
                                                modifier = Modifier
                                                    .width(1.5.dp)
                                                    .height(36.dp)
                                                    .background(YovxGrey.copy(alpha = 0.4f))
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            CommentItem(
                                                comment = reply,
                                                isReply = true,
                                                onReplyClick = {}
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = YovxGrey)

                    // Active Reply Indicator Header Bar
                    if (replyingToComment != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(YovxCharcoal)
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Reply,
                                    contentDescription = "Replying to",
                                    tint = YovxAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Replying to @${replyingToComment?.username}",
                                    color = YovxTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton(
                                onClick = { replyingToComment = null },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel reply target",
                                    tint = YovxTextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    // Write Comment Input Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { 
                                Text(
                                    text = if (replyingToComment != null) "Reply to @${replyingToComment?.username}..." else "Add a comment...", 
                                    fontSize = 13.sp
                                ) 
                            },
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = YovxTextPrimary,
                                unfocusedTextColor = YovxTextPrimary,
                                focusedBorderColor = YovxPrimary,
                                unfocusedBorderColor = YovxGrey,
                                focusedContainerColor = YovxObsidian,
                                unfocusedContainerColor = YovxObsidian
                            ),
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("comment_input"),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (textInput.trim().isNotEmpty()) {
                                    viewModel.submitComment(postId, textInput, parentId = replyingToComment?.id)
                                    textInput = ""
                                    replyingToComment = null
                                }
                            })
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (textInput.trim().isNotEmpty()) {
                                    viewModel.submitComment(postId, textInput, parentId = replyingToComment?.id)
                                    textInput = ""
                                    replyingToComment = null
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = YovxPrimary),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Post Comment",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Immersive full screen story viewer.
 */
@Composable
fun StoryViewerDialog(
    story: StoryEntity,
    onDismiss: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(key1 = true) {
        val durationMs = 5000L
        val intervalMs = 50L
        val steps = durationMs / intervalMs
        for (i in 1..steps) {
            delay(intervalMs)
            progress = i.toFloat() / steps
        }
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Background visual story asset representation
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(story.mediaUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Active Story View",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Linear timeline progress bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    color = YovxPrimary,
                    trackColor = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))

                // User Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(story.userAvatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Story Creator",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = story.username,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (story.musicTitle != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = "Music Playing",
                                        tint = YovxPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = story.musicTitle,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Story",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Bottom Caption Overlay
            if (story.caption != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 0f
                            )
                        )
                        .navigationBarsPadding()
                        .padding(24.dp)
                ) {
                    Text(
                        text = story.caption,
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
