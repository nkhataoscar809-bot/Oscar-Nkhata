package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.database.UserEntity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.widget.Toast
import com.example.ui.components.CustomAnalyticsChart
import com.example.ui.components.PostCard
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.*
import com.example.viewmodel.YovxViewModel

@Composable
fun ProfileScreen(
    viewModel: YovxViewModel,
    modifier: Modifier = Modifier
) {
    val viewedUser by viewModel.viewedUser.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allPosts by viewModel.allPosts.collectAsState()

    var activeTab by remember { mutableStateOf("Posts") } // "Posts" or "Analytics"
    var showEditDialog by remember { mutableStateOf(false) }

    val isOwnProfile = viewedUser?.id == "me" || (currentUser != null && viewedUser?.id == currentUser?.id)
    val viewedUserId = viewedUser?.id ?: "me"
    val userPosts = allPosts.filter { it.userId == viewedUserId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(YovxObsidian)
            .testTag("profile_screen")
    ) {
        // Cover Photo and Avatar Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                // Cover
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(viewedUser?.coverUrl ?: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Cover Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )

                // Back Button overlay for viewing others
                if (!isOwnProfile) {
                    IconButton(
                        onClick = { viewModel.navigateTo("Feed") },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 12.dp, top = 12.dp)
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 12.dp, top = 12.dp)
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .testTag("settings_gear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Avatar overlapping
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 4.dp)
                        .size(92.dp)
                        .border(3.dp, YovxObsidian, CircleShape)
                        .padding(2.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(viewedUser?.avatarUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                // Edit Profile Button or Interaction Buttons overlay
                if (isOwnProfile) {
                    Button(
                        onClick = { showEditDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = YovxGrey.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 10.dp)
                            .height(34.dp)
                            .testTag("edit_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Edit Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        var isFollowing by remember { mutableStateOf(false) }
                        Button(
                            onClick = { isFollowing = !isFollowing },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) YovxGrey.copy(alpha = 0.8f) else YovxPrimary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (isFollowing) "Following" else "Follow",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = {
                                viewedUser?.id?.let { uid ->
                                    viewModel.activeThreadId.value = uid
                                    viewModel.navigateTo("Messaging")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YovxGrey.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubble,
                                contentDescription = "Message User",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Profile Details (Name, Handles, Bio, Web link)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = viewedUser?.displayName ?: "Oscar Nkhata",
                        color = YovxTextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    VerificationBadge(isVerified = viewedUser?.isVerified ?: true, isCreator = true)
                }
                Text(
                    text = "@${viewedUser?.username ?: "oscar_builds"}",
                    color = YovxTextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = viewedUser?.bio ?: "Building the future of social networks on Android. Yovx Founder.",
                    color = YovxTextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // website link
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Website Link",
                        tint = YovxSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = viewedUser?.website ?: "https://yovx.com/oscar",
                        color = YovxSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats row (Followers, Following, Posts)
                val followersCountFormatted = when {
                    viewedUser != null && viewedUser!!.followersCount >= 1_000_000 -> {
                        String.format("%.1fM", viewedUser!!.followersCount / 1_000_000f)
                    }
                    viewedUser != null && viewedUser!!.followersCount >= 1_000 -> {
                        String.format("%.1fK", viewedUser!!.followersCount / 1_000f)
                    }
                    viewedUser != null -> viewedUser!!.followersCount.toString()
                    else -> "12.4K"
                }
                val followingCountFormatted = viewedUser?.followingCount?.toString() ?: "482"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProfileStatItem(count = followersCountFormatted, label = "Followers", modifier = Modifier.weight(1f))
                    ProfileStatItem(count = followingCountFormatted, label = "Following", modifier = Modifier.weight(1f))
                    ProfileStatItem(count = "${userPosts.size}", label = "Posts", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Horizontal tabs list (My Posts / Creator Analytics)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(YovxCharcoal)
                    .border(0.5.dp, YovxGrey)
            ) {
                ProfileTabItem(
                    title = if (isOwnProfile) "My Posts" else "Posts",
                    isActive = activeTab == "Posts",
                    onClick = { activeTab = "Posts" },
                    modifier = Modifier.weight(1f)
                )
                ProfileTabItem(
                    title = "Creator Analytics",
                    isActive = activeTab == "Analytics",
                    onClick = { activeTab = "Analytics" },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Tab Content rendering
        if (activeTab == "Posts") {
            if (userPosts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isOwnProfile) "You haven't posted anything yet." else "This user hasn't posted anything yet.",
                            color = YovxTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(userPosts) { post ->
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
        } else {
            // Creator Tools Dashboard Tab content (Reach metrics + Custom Canvas chart)
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Creator Tools Dashboard",
                        color = YovxTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Monetization details
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        colors = CardDefaults.cardColors(containerColor = YovxCharcoal),
                        border = BorderStroke(0.5.dp, YovxGrey)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Monetization Status", color = YovxTextSecondary, fontSize = 12.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(YovxGold.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "Active Creator", color = YovxGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = if (isOwnProfile) "$1,280.40" else "$0.00", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
                            Text(text = "Estimated Earnings (Last 30 Days)", color = YovxGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Render our gorgeous Canvas custom drawn chart
                    val points = if (isOwnProfile) listOf(1400f, 1800f, 1600f, 2200f, 2900f, 3400f) else listOf(120f, 230f, 180f, 400f, 320f, 600f)
                    val labels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")

                    CustomAnalyticsChart(points = points, labels = labels)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary analytic metrics cards
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AnalyticSmallCard(
                            title = "Reel Views",
                            value = if (isOwnProfile) "482.5K" else "12.8K",
                            percentage = "+14.2%",
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticSmallCard(
                            title = "Profile Visits",
                            value = if (isOwnProfile) "24.9K" else "1.2K",
                            percentage = "+8.5%",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Edit Profile dialog
    if (showEditDialog) {
        EditProfileDialog(
            user = currentUser,
            onSave = { name, uName, avatar, bioStr, web, bday ->
                viewModel.editProfile(name, uName, avatar, bioStr, web, bday)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
fun ProfileStatItem(
    count: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            color = YovxTextPrimary,
            fontWeight = FontWeight.Black,
            fontSize = 16.sp
        )
        Text(
            text = label,
            color = YovxTextSecondary,
            fontSize = 11.sp
        )
    }
}

@Composable
fun ProfileTabItem(
    title: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = if (isActive) YovxPrimary else YovxTextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(24.dp, 2.dp)
                        .background(YovxPrimary)
                )
            }
        }
    }
}

@Composable
fun AnalyticSmallCard(
    title: String,
    value: String,
    percentage: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = YovxCharcoal),
        border = BorderStroke(0.5.dp, YovxGrey)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, color = YovxTextSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = percentage, color = YovxGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun compressAndEncodeBitmap(bitmap: Bitmap): String {
    val maxSize = 250
    val width = bitmap.width
    val height = bitmap.height
    val ratio = width.toFloat() / height.toFloat()
    val (newWidth, newHeight) = if (ratio > 1) {
        Pair(maxSize, (maxSize / ratio).toInt())
    } else {
        Pair((maxSize * ratio).toInt(), maxSize)
    }
    val resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    val outputStream = ByteArrayOutputStream()
    resized.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    val bytes = outputStream.toByteArray()
    return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
}

private fun generateSimulatedAvatar(displayName: String, filterName: String): Bitmap {
    val size = 250
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint()
    
    val (colorBgStart, colorBgEnd, colorFg) = when (filterName) {
        "Neon Wave" -> Triple(0xFF120C1F.toInt(), 0xFF8A2387.toInt(), 0xFFE94057.toInt())
        "Gold Hour" -> Triple(0xFF2B1B17.toInt(), 0xFFF21A1D.toInt(), 0xFFFF9000.toInt())
        "Noir Mono" -> Triple(0xFF111111.toInt(), 0xFF333333.toInt(), 0xFFFFFFFF.toInt())
        "Cyber Punk" -> Triple(0xFF03001e.toInt(), 0xFF7303c0.toInt(), 0xFFec38bc.toInt())
        else -> Triple(0xFF1E1B29.toInt(), 0xFF6C5DD3.toInt(), 0xFF00C9A7.toInt())
    }
    
    val gradient = android.graphics.LinearGradient(
        0f, 0f, size.toFloat(), size.toFloat(),
        colorBgStart, colorBgEnd,
        android.graphics.Shader.TileMode.CLAMP
    )
    paint.shader = gradient
    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
    paint.shader = null
    
    paint.color = colorFg
    paint.alpha = 50
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 4f
    canvas.drawCircle(size / 2f, size / 2f, size * 0.4f, paint)
    canvas.drawCircle(size / 2f, size / 2f, size * 0.25f, paint)
    
    paint.alpha = 255
    paint.style = android.graphics.Paint.Style.FILL
    paint.textSize = size * 0.45f
    paint.textAlign = android.graphics.Paint.Align.CENTER
    paint.isAntiAlias = true
    paint.isFakeBoldText = true
    
    val initial = if (displayName.trim().isNotEmpty()) displayName.trim().take(1).uppercase() else "Y"
    val fontMetrics = paint.fontMetrics
    val yOffset = (fontMetrics.descent + fontMetrics.ascent) / 2f
    canvas.drawText(initial, size / 2f, (size / 2f) - yOffset, paint)
    
    return bitmap
}

@Composable
fun EditProfileDialog(
    user: UserEntity?,
    onSave: (String, String, String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var username by remember { mutableStateOf(user?.username ?: "") }
    var avatarUrl by remember { mutableStateOf(user?.avatarUrl ?: "") }
    var bio by remember { mutableStateOf(user?.bio ?: "") }
    var website by remember { mutableStateOf(user?.website ?: "") }
    var birthday by remember { mutableStateOf(user?.birthday ?: "") }

    val context = LocalContext.current
    var showCameraSim by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Neon Wave") }
    var showPresets by remember { mutableStateOf(false) }

    // Activity Launcher for native camera preview
    val systemCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val base64 = compressAndEncodeBitmap(bitmap)
            avatarUrl = base64
            Toast.makeText(context, "📸 Custom photo captured successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission request launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                systemCameraLauncher.launch(null)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to open camera app: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to take a real photo.", Toast.LENGTH_SHORT).show()
        }
    }

    val presetAvatars = listOf(
        "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
        "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
        "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150",
        "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            colors = CardDefaults.cardColors(containerColor = YovxCharcoal),
            border = BorderStroke(1.dp, YovxGrey)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "User Settings Panel",
                        color = YovxTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = YovxTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Modern Interactive Profile Picture Section
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(3.dp, YovxPrimary, CircleShape)
                            .background(YovxObsidian)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(if (avatarUrl.isNotEmpty()) avatarUrl else "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    // Camera overlay badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(YovxPrimary)
                            .border(2.dp, YovxCharcoal, CircleShape)
                            .clickable { showCameraSim = true }
                            .testTag("change_avatar_badge"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Profile Picture",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Beautiful Center Text triggers camera studio
                TextButton(
                    onClick = { showCameraSim = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(14.dp), tint = YovxPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Upload Profile Picture via Camera", color = YovxPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle for Presets & Custom URLs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPresets = !showPresets }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Or choose presets / enter URL",
                        color = YovxTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (showPresets) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Presets",
                        tint = YovxTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (showPresets) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        presetAvatars.forEach { url ->
                            val isSelected = avatarUrl == url
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(
                                        border = BorderStroke(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) YovxPrimary else Color.Transparent
                                        ),
                                        shape = CircleShape
                                    )
                                    .clickable { avatarUrl = url }
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(url)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Preset Avatar option",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = avatarUrl,
                        onValueChange = { avatarUrl = it },
                        label = { Text("Custom Profile Picture URL") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = YovxTextPrimary,
                            unfocusedTextColor = YovxTextPrimary,
                            focusedBorderColor = YovxPrimary,
                            unfocusedBorderColor = YovxGrey
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("edit_avatar_url")
                    )
                }

                HorizontalDivider(color = YovxGrey.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 12.dp))

                // Input fields
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = YovxTextPrimary,
                        unfocusedTextColor = YovxTextPrimary,
                        focusedBorderColor = YovxPrimary,
                        unfocusedBorderColor = YovxGrey
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("edit_display_name")
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = YovxTextPrimary,
                        unfocusedTextColor = YovxTextPrimary,
                        focusedBorderColor = YovxPrimary,
                        unfocusedBorderColor = YovxGrey
                    ),
                    prefix = { Text("@", color = YovxTextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("edit_username")
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio / Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = YovxTextPrimary,
                        unfocusedTextColor = YovxTextPrimary,
                        focusedBorderColor = YovxPrimary,
                        unfocusedBorderColor = YovxGrey
                    ),
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("edit_bio")
                )

                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("Website") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = YovxTextPrimary,
                        unfocusedTextColor = YovxTextPrimary,
                        focusedBorderColor = YovxPrimary,
                        unfocusedBorderColor = YovxGrey
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                )

                OutlinedTextField(
                    value = birthday,
                    onValueChange = { birthday = it },
                    label = { Text("Birthday") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = YovxTextPrimary,
                        unfocusedTextColor = YovxTextPrimary,
                        focusedBorderColor = YovxPrimary,
                        unfocusedBorderColor = YovxGrey
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Premium Save Settings Button
                Button(
                    onClick = { onSave(displayName, username, avatarUrl, bio, website, birthday) },
                    colors = ButtonDefaults.buttonColors(containerColor = YovxPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_profile_button")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Save Settings & Update Profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }

    // --- IMMERSIVE CAMERA VIEW FINDER & GENERATOR DIALOG ---
    if (showCameraSim) {
        Dialog(onDismissRequest = { showCameraSim = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = YovxObsidian),
                border = BorderStroke(1.dp, YovxPrimary.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = YovxPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Yovx Camera Studio",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        IconButton(onClick = { showCameraSim = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = YovxTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Simulated Camera Viewfinder
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .border(BorderStroke(1.5.dp, YovxPrimary), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val previewBitmap = remember(displayName, selectedFilter) {
                            generateSimulatedAvatar(displayName, selectedFilter)
                        }
                        
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(previewBitmap)
                                .crossfade(false)
                                .build(),
                            contentDescription = "Simulated Viewfinder",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Cool Viewfinder Overlay Grid Lines
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                                .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(40.dp)
                                    .border(BorderStroke(1.dp, YovxPrimary.copy(alpha = 0.6f)), RoundedCornerShape(2.dp))
                            )
                        }

                        // Filter Label indicator
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = selectedFilter.uppercase(),
                                color = YovxPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // REC pulsing indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Lens Filters Row
                    Text(
                        text = "Camera Filter Effect:",
                        color = YovxTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val filters = listOf("Neon Wave", "Gold Hour", "Noir Mono", "Cyber Punk")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filters.forEach { filter ->
                            val isSel = selectedFilter == filter
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) YovxPrimary.copy(alpha = 0.2f) else YovxCharcoal)
                                    .border(
                                        1.dp,
                                        if (isSel) YovxPrimary else YovxGrey,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedFilter = filter }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = filter,
                                    color = if (isSel) YovxPrimary else YovxTextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Action Buttons
                    Button(
                        onClick = {
                            val finalBitmap = generateSimulatedAvatar(displayName, selectedFilter)
                            val base64 = compressAndEncodeBitmap(finalBitmap)
                            avatarUrl = base64
                            showCameraSim = false
                            Toast.makeText(context, "✨ Custom simulated avatar applied!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YovxPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("capture_simulated_shutter_button")
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Capture", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Capture Frame & Set", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Native Camera Launcher Button
                    OutlinedButton(
                        onClick = {
                            showCameraSim = false
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        },
                        border = BorderStroke(1.dp, YovxPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = YovxPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("launch_native_camera_button")
                    ) {
                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Native Camera", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Launch Device Camera Sensor", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
