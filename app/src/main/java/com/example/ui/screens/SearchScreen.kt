package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.database.UserEntity
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.*
import com.example.viewmodel.YovxViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: YovxViewModel,
    modifier: Modifier = Modifier
) {
    val users by viewModel.allUsers.collectAsState()
    var searchInput by remember { mutableStateOf("") }

    // Client-side search matching users by name or username
    val filteredUsers = users.filter { user ->
        searchInput.trim().isEmpty() ||
                user.displayName.contains(searchInput, ignoreCase = true) ||
                user.username.contains(searchInput, ignoreCase = true) ||
                user.bio.contains(searchInput, ignoreCase = true)
    }

    val trendingTags = listOf(
        "#YovxSocial",
        "#FujiSunrise",
        "#GarlicButterShrimps",
        "#KyotoRainReels",
        "#CyberGlassXR",
        "#MicUnboxing"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YovxObsidian)
    ) {
        // Search Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(YovxCharcoal)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo("Feed") }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go back to Feed",
                    tint = YovxTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it },
                placeholder = { Text("Search users, creators, businesses...", fontSize = 13.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = YovxTextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = YovxTextPrimary,
                    unfocusedTextColor = YovxTextPrimary,
                    focusedBorderColor = YovxPrimary,
                    unfocusedBorderColor = YovxGrey,
                    focusedContainerColor = YovxObsidian,
                    unfocusedContainerColor = YovxObsidian
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("global_search_input")
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (searchInput.trim().isEmpty()) {
                // Display Trending searches list when empty (TikTok style)
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 10.dp, bottom = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Trending",
                            tint = YovxPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Trending on Yovx",
                            color = YovxTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                items(trendingTags) { tag ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { searchInput = tag }
                            .padding(vertical = 12.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tag,
                            color = YovxSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${(10..99).random()}K views",
                            color = YovxTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    HorizontalDivider(color = YovxGrey, thickness = 0.5.dp)
                }
            } else {
                // Render Search Results
                item {
                    Text(
                        text = "Creators & Users (${filteredUsers.size})",
                        color = YovxTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                if (filteredUsers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No users found matching '$searchInput'.",
                                color = YovxTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(filteredUsers) { user ->
                        var isFollowingUser by remember { mutableStateOf(user.id == "me") }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    viewModel.selectedUserIdForView.value = user.id
                                    viewModel.navigateTo("Profile")
                                },
                            colors = CardDefaults.cardColors(containerColor = YovxCharcoal),
                            border = BorderStroke(0.5.dp, YovxGrey)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(user.avatarUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "${user.username} avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.displayName,
                                            color = YovxTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        VerificationBadge(isVerified = user.isVerified, isCreator = true)
                                    }
                                    Text(
                                        text = "@${user.username}",
                                        color = YovxTextSecondary,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = user.bio,
                                        color = YovxTextSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                if (user.id != "me") {
                                    Button(
                                        onClick = { isFollowingUser = !isFollowingUser },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isFollowingUser) YovxGrey else YovxPrimary
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .height(30.dp)
                                            .widthIn(min = 76.dp)
                                    ) {
                                        Text(
                                            text = if (isFollowingUser) "Following" else "Follow",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
