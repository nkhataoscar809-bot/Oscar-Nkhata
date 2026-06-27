package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.*
import com.example.viewmodel.YovxViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCreatorScreen(
    viewModel: YovxViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiGeneratedCaption by viewModel.aiGeneratedCaption.collectAsState()

    var postContent by remember { mutableStateOf("") }
    var mediaType by remember { mutableStateOf("text") } // "text", "photo", "video", "poll"
    var mediaUrl by remember { mutableStateOf("") }

    // Poll options state
    var pollOption1 by remember { mutableStateOf("") }
    var pollOption2 by remember { mutableStateOf("") }
    var pollOption3 by remember { mutableStateOf("") }

    // Gemini AI helper state
    var showAiHelper by remember { mutableStateOf(false) }
    var aiTopicInput by remember { mutableStateOf("") }
    var aiStyleSelect by remember { mutableStateOf("Viral (TikTok)") }

    // Video studio local states
    var videoTitleInput by remember { mutableStateOf("Kyoto Rain Reels") }
    var videoContextInput by remember { mutableStateOf("Scenic streets of Kyoto in the rain, paper lanterns glowing, peaceful ambient look") }
    var audioVibeInput by remember { mutableStateOf("Lo-Fi Beat") }
    var targetAudienceInput by remember { mutableStateOf("Travel & Exploration") }
    var selectedVideoPresetIndex by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YovxObsidian)
    ) {
        // App Bar Creator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(YovxCharcoal)
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.navigateTo("Feed") }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel post creation", tint = YovxTextPrimary)
            }
            Text(
                text = "Create Post",
                color = YovxTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Button(
                onClick = {
                    val pollOptions = if (mediaType == "poll") {
                        listOf(pollOption1, pollOption2, pollOption3).filter { it.trim().isNotEmpty() }
                    } else emptyList()

                    viewModel.publishNewPost(
                        content = postContent,
                        mediaUrl = mediaUrl,
                        mediaType = mediaType,
                        pollOptions = pollOptions
                    )
                },
                enabled = postContent.trim().isNotEmpty() || mediaUrl.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = YovxPrimary, disabledContainerColor = YovxGrey),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(32.dp)
                    .testTag("publish_post_button")
            ) {
                Text(text = "Publish", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(YovxObsidian)
                .padding(16.dp)
        ) {
            // User Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(currentUser?.avatarUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150")
                        .crossfade(true)
                        .build(),
                    contentDescription = "My avatar picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = currentUser?.displayName ?: "Oscar Nkhata",
                        color = YovxTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    // Post type indicator badge
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(YovxGrey)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Sharing to Feed: ${mediaType.uppercase()}",
                            color = YovxTextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Text Input Box Editor
            OutlinedTextField(
                value = postContent,
                onValueChange = { postContent = it },
                placeholder = { Text("What's on your mind? Capture the spark...", fontSize = 14.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = YovxTextPrimary,
                    unfocusedTextColor = YovxTextPrimary,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                textStyle = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("post_text_input")
            )

            // Smart Sparkle AI Caption helper Button (Gemini Integration)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .clickable { showAiHelper = !showAiHelper },
                colors = CardDefaults.cardColors(containerColor = YovxAccent.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, YovxAccent.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Gemini AI",
                        tint = YovxAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Write Viral Caption with Gemini AI",
                            color = YovxTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Generate professional hook-captions with trending hashtags instantly.",
                            color = YovxTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Icon(
                        imageVector = if (showAiHelper) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = YovxAccent
                    )
                }
            }

            // AI Helper Expandable HUD Section
            AnimatedVisibility(visible = showAiHelper) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = YovxCharcoal),
                    border = BorderStroke(0.5.dp, YovxGrey)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Gemini AI Caption Assistant",
                            color = YovxTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = aiTopicInput,
                            onValueChange = { aiTopicInput = it },
                            placeholder = { Text("What is your post about? (e.g. Kyoto travel logs)", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = YovxTextPrimary,
                                unfocusedTextColor = YovxTextPrimary,
                                focusedBorderColor = YovxAccent,
                                unfocusedBorderColor = YovxGrey
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("ai_topic_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Style selector row
                        val styles = listOf("Viral (TikTok)", "Aesthetic (Instagram)", "Engaging (Facebook)")
                        Text(text = "Output Tone/Style:", color = YovxTextSecondary, fontSize = 11.sp)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            items(styles) { style ->
                                val isSelected = aiStyleSelect == style
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) YovxAccent else YovxGrey)
                                        .clickable { aiStyleSelect = style }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(text = style, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.generateAIPostCaption(aiTopicInput, aiStyleSelect) },
                            colors = ButtonDefaults.buttonColors(containerColor = YovxAccent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .testTag("generate_caption_button")
                        ) {
                            if (isAiLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 1.5.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Spark", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Generate Caption with Gemini", fontSize = 12.sp)
                                }
                            }
                        }

                        if (aiGeneratedCaption.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "AI Proposal output:",
                                color = YovxAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(YovxObsidian)
                                    .border(0.5.dp, YovxGrey, RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Text(text = aiGeneratedCaption, color = YovxTextPrimary, fontSize = 12.sp, lineHeight = 18.sp)
                            }
                            Button(
                                onClick = {
                                    postContent = aiGeneratedCaption
                                    showAiHelper = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = YovxGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp)
                            ) {
                                Text(text = "Apply Caption to Post", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Post Form Fields based on media type selected
            when (mediaType) {
                "photo" -> {
                    OutlinedTextField(
                        value = mediaUrl,
                        onValueChange = { mediaUrl = it },
                        label = { Text("Direct image URL link") },
                        placeholder = { Text("https://images.unsplash.com/...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = YovxTextPrimary,
                            unfocusedTextColor = YovxTextPrimary,
                            focusedBorderColor = YovxPrimary,
                            unfocusedBorderColor = YovxGrey
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
                "video" -> {
                    val videoPresets = listOf(
                        Pair("Kyoto Rain Reels", "Scenic streets of Kyoto in the rain, paper lanterns glowing, peaceful ambient look"),
                        Pair("Gourmet Shrimp Reels", "Sizzling Garlic Butter Shrimps cooking, Michelin star quality, steaming pan closeup"),
                        Pair("CyberGlass Review", "CyberGlass XR headset unboxing, futuristic neon lights, hands-on gesture demos"),
                        Pair("Tokyo Crossing", "Time-lapse of Tokyo Shibuya crossing at night, giant dynamic billboards"),
                        Pair("Sunset Sailing", "Luxury yacht cruising through calm golden waters, gorgeous warm sky breeze"),
                        Pair("Coffee Morning", "Fresh espresso shot dripping into a clear glass cup, morning sun beam flare")
                    )

                    val isVideoAiLoading by viewModel.isVideoAiLoading.collectAsState()
                    val videoAiGeneratedCaption by viewModel.videoAiGeneratedCaption.collectAsState()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, YovxGrey, RoundedCornerShape(12.dp))
                            .background(YovxCharcoal)
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Simulated Video Studio",
                                color = YovxTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(YovxGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Ready to Upload",
                                    color = YovxGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        // Video upload simulated drop-zone/selector card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.dp, YovxPrimary.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                                .background(YovxObsidian)
                                .clickable {
                                    // Cycles through presets on click as a simplified interaction
                                    val nextIndex = (selectedVideoPresetIndex + 1) % videoPresets.size
                                    selectedVideoPresetIndex = nextIndex
                                    mediaUrl = videoPresets[nextIndex].first
                                    videoTitleInput = videoPresets[nextIndex].first
                                    videoContextInput = videoPresets[nextIndex].second
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = "Upload",
                                    tint = YovxPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Video Selected: ${videoPresets[selectedVideoPresetIndex].first}",
                                    color = YovxTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap box to cycle sample videos",
                                    color = YovxTextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Video details & Gemini Caption workspace
                        Text(
                            text = "AI Caption Generator Service",
                            color = YovxAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = videoTitleInput,
                            onValueChange = { videoTitleInput = it },
                            label = { Text("Video Title / Hook Topic", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = YovxTextPrimary,
                                unfocusedTextColor = YovxTextPrimary,
                                focusedBorderColor = YovxAccent,
                                unfocusedBorderColor = YovxGrey
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = videoContextInput,
                            onValueChange = { videoContextInput = it },
                            label = { Text("Visual scene context", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = YovxTextPrimary,
                                unfocusedTextColor = YovxTextPrimary,
                                focusedBorderColor = YovxAccent,
                                unfocusedBorderColor = YovxGrey
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = audioVibeInput,
                                onValueChange = { audioVibeInput = it },
                                label = { Text("Music/Audio Vibe", fontSize = 10.sp) },
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
                            OutlinedTextField(
                                value = targetAudienceInput,
                                onValueChange = { targetAudienceInput = it },
                                label = { Text("Target Audience", fontSize = 10.sp) },
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
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                viewModel.generateAiVideoCaption(
                                    videoTitle = videoTitleInput,
                                    videoContext = videoContextInput,
                                    audioVibe = audioVibeInput,
                                    targetAudience = targetAudienceInput,
                                    style = "Viral & Polished Video Reel Format"
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YovxPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("generate_video_caption_button")
                        ) {
                            if (isVideoAiLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 1.5.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Spark", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Generate Caption & Chapters with Gemini", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (videoAiGeneratedCaption.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Gemini Video Output:",
                                color = YovxAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(YovxObsidian)
                                    .border(0.5.dp, YovxGrey, RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = videoAiGeneratedCaption,
                                    color = YovxTextPrimary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        postContent = videoAiGeneratedCaption
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = YovxGreen),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                ) {
                                    Text(text = "Apply Caption", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        viewModel.videoAiGeneratedCaption.value = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = YovxGrey),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                ) {
                                    Text(text = "Clear Output", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
                "poll" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, YovxGrey, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(text = "Define Poll Options", color = YovxTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = pollOption1,
                            onValueChange = { pollOption1 = it },
                            placeholder = { Text("Option 1", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = YovxTextPrimary,
                                unfocusedTextColor = YovxTextPrimary,
                                focusedBorderColor = YovxPrimary,
                                unfocusedBorderColor = YovxGrey
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .height(46.dp)
                        )
                        OutlinedTextField(
                            value = pollOption2,
                            onValueChange = { pollOption2 = it },
                            placeholder = { Text("Option 2", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = YovxTextPrimary,
                                unfocusedTextColor = YovxTextPrimary,
                                focusedBorderColor = YovxPrimary,
                                unfocusedBorderColor = YovxGrey
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .height(46.dp)
                        )
                        OutlinedTextField(
                            value = pollOption3,
                            onValueChange = { pollOption3 = it },
                            placeholder = { Text("Option 3 (Optional)", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = YovxTextPrimary,
                                unfocusedTextColor = YovxTextPrimary,
                                focusedBorderColor = YovxPrimary,
                                unfocusedBorderColor = YovxGrey
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .height(46.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Selector row for choosing post type (Text, Photo, Video, Poll)
            Text(text = "Post Format Media Type:", color = YovxTextSecondary, fontSize = 12.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val mediaFormats = listOf(
                    Triple("text", "Text", Icons.Default.Subject),
                    Triple("photo", "Photo", Icons.Default.Image),
                    Triple("video", "Video/Reel", Icons.Default.VideoLibrary),
                    Triple("poll", "Poll", Icons.Default.Poll)
                )

                mediaFormats.forEach { format ->
                    val isSelected = mediaType == format.first
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                mediaType = format.first
                                if (format.first == "video") {
                                    mediaUrl = "Kyoto Rain Reels"
                                } else if (format.first == "text" || format.first == "poll") {
                                    mediaUrl = ""
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) YovxPrimary.copy(alpha = 0.15f) else YovxCharcoal
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) YovxPrimary else YovxGrey
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = format.third,
                                contentDescription = format.second,
                                tint = if (isSelected) YovxPrimary else YovxTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = format.second,
                                color = if (isSelected) YovxPrimary else YovxTextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
