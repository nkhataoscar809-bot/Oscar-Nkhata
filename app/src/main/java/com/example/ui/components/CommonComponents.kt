package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.database.PostEntity
import com.example.ui.theme.*

@Composable
fun VerificationBadge(
    isVerified: Boolean,
    isCreator: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (isVerified) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Verified Badge",
            tint = if (isCreator) YovxGold else YovxVerified,
            modifier = modifier
                .size(16.dp)
                .testTag("verification_badge")
        )
    }
}

@Composable
fun StoryBubble(
    username: String,
    avatarUrl: String,
    isViewed: Boolean,
    onClick: () -> Unit
) {
    val ringColor = if (isViewed) YovxGrey else YovxPrimary
    val borderStroke = if (isViewed) 1.5.dp else 2.5.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable { onClick() }
            .testTag("story_bubble_$username")
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .border(borderStroke, ringColor, CircleShape)
                .padding(4.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "$username's story",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = username,
            color = YovxTextSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(70.dp)
        )
    }
}

@Composable
fun PostCard(
    post: PostEntity,
    onLike: () -> Unit,
    onCommentToggle: () -> Unit,
    onRepost: () -> Unit,
    onBookmark: () -> Unit,
    onVote: (Int) -> Unit,
    onTranslate: ((String) -> Unit) -> Unit, // triggers translation
    onUserClick: () -> Unit = {}
) {
    var isTranslated by remember { mutableStateOf(false) }
    var translatedText by remember { mutableStateOf("") }
    var translating by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("post_card_${post.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x1F252530)),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x05FFFFFF)))
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Avatar, Name, Timestamp, Options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUserClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.userAvatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "User avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.userDisplayName,
                            color = YovxTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        VerificationBadge(isVerified = post.isUserVerified, isCreator = true)
                    }
                    Text(
                        text = "@${post.username}",
                        color = YovxTextSecondary,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = { /* More options */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Post Options",
                        tint = YovxTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body content
            Text(
                text = if (isTranslated) translatedText else post.content,
                color = YovxTextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            // Translation badge indicator
            if (isTranslated) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(YovxAccent.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Translated",
                        tint = YovxAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Translated via Gemini AI",
                        color = YovxAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Media attachment based on type
            when (post.mediaType) {
                "photo" -> {
                    if (post.mediaUrl.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(post.mediaUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Post media",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(0.5.dp, YovxGrey, RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
                "video" -> {
                    // Video player simulation box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .border(0.5.dp, YovxGrey, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Blurred video poster or generic video asset representation
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("https://images.unsplash.com/photo-1536240478700-b869070f9279?w=600")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Video Thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().alpha(0.4f)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.PlayCircle,
                                contentDescription = "Play Reel Video",
                                tint = YovxPrimary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Reels Preview: Click to Open in Reels",
                                color = YovxTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                "poll" -> {
                    if (post.pollOptionsJson != null && post.pollVotesJson != null) {
                        val options = post.pollOptionsJson.split(",")
                        val votes = post.pollVotesJson.split(",").map { it.toIntOrNull() ?: 0 }
                        val totalVotes = votes.sum().coerceAtLeast(1)
                        val hasVoted = post.selectedPollOption != -1

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, YovxGrey, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Poll",
                                color = YovxTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            options.forEachIndexed { index, option ->
                                val voteCount = votes.getOrElse(index) { 0 }
                                val percentage = (voteCount.toFloat() / totalVotes * 100).toInt()
                                val isMyVote = post.selectedPollOption == index

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isMyVote) YovxPrimary.copy(alpha = 0.15f) else YovxGrey.copy(alpha = 0.3f))
                                        .border(
                                            width = if (isMyVote) 1.dp else 0.5.dp,
                                            color = if (isMyVote) YovxPrimary else YovxGrey,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { onVote(index) }
                                ) {
                                    // Animated progress bar background
                                    if (hasVoted) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(fraction = voteCount.toFloat() / totalVotes)
                                                .background(if (isMyVote) YovxPrimary.copy(alpha = 0.25f) else YovxGrey.copy(alpha = 0.6f))
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = option,
                                                color = if (isMyVote) YovxPrimary else YovxTextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = if (isMyVote) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (isMyVote) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Voted",
                                                    tint = YovxPrimary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        if (hasVoted) {
                                            Text(
                                                text = "$percentage% ($voteCount)",
                                                color = YovxTextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            // Divider line
            HorizontalDivider(color = YovxGrey, thickness = 0.5.dp)

            Spacer(modifier = Modifier.height(8.dp))

            // Footer Actions: Like, Comment, Repost, Bookmark, Translate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onLike() }
                        .padding(4.dp)
                        .testTag("like_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLiked) YovxPrimary else YovxTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.likesCount}",
                        color = if (post.isLiked) YovxPrimary else YovxTextSecondary,
                        fontSize = 13.sp
                    )
                }

                // Comment Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onCommentToggle() }
                        .padding(4.dp)
                        .testTag("comment_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ModeComment,
                        contentDescription = "Comment",
                        tint = YovxTextSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.commentsCount}",
                        color = YovxTextSecondary,
                        fontSize = 13.sp
                    )
                }

                // Repost / Share Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onRepost() }
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Repeat,
                        contentDescription = "Repost",
                        tint = if (post.isReposted) YovxGreen else YovxTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.sharesCount}",
                        color = if (post.isReposted) YovxGreen else YovxTextSecondary,
                        fontSize = 13.sp
                    )
                }

                // Bookmark Button
                IconButton(
                    onClick = { onBookmark() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (post.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (post.isBookmarked) YovxGold else YovxTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Translate button (Gemini AI Feature)
                IconButton(
                    onClick = {
                        if (isTranslated) {
                            isTranslated = false
                        } else {
                            translating = true
                            onTranslate { result ->
                                translatedText = result
                                isTranslated = true
                                translating = false
                            }
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    if (translating) {
                        CircularProgressIndicator(
                            color = YovxAccent,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Translate Post",
                            tint = if (isTranslated) YovxAccent else YovxTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom line chart drawn using Compose Canvas for profile analytics.
 */
@Composable
fun CustomAnalyticsChart(
    points: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(YovxObsidian, RoundedCornerShape(12.dp))
            .border(0.5.dp, YovxGrey, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile Reach Trend",
                color = YovxTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "+24.8% This Month",
                color = YovxGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val spacing = width / (points.size - 1)

                val maxVal = points.maxOrNull() ?: 1f
                val minVal = points.minOrNull() ?: 0f
                val range = (maxVal - minVal).coerceAtLeast(1f)

                val path = Path()
                points.forEachIndexed { index, value ->
                    val x = index * spacing
                    val y = height - ((value - minVal) / range) * (height * 0.8f) - (height * 0.1f)

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                // Draw gradient underneath the curve
                val gradientPath = Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = gradientPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(YovxAccent.copy(alpha = 0.3f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw outline stroke
                drawPath(
                    path = path,
                    color = YovxAccent,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Draw circles on data points
                points.forEachIndexed { index, value ->
                    val x = index * spacing
                    val y = height - ((value - minVal) / range) * (height * 0.8f) - (height * 0.1f)
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = YovxAccent,
                        radius = 2.5.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Draw Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    color = YovxTextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
