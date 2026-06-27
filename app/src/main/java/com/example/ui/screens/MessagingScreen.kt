package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.database.MessageEntity
import com.example.ui.theme.*
import com.example.viewmodel.YovxViewModel

@Composable
fun MessagingScreen(
    viewModel: YovxViewModel,
    modifier: Modifier = Modifier
) {
    val activeThreadId by viewModel.activeThreadId.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    val isChatTyping by viewModel.isChatTyping.collectAsState()

    var messageInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messages.size, isChatTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(YovxObsidian)
    ) {
        // Main Messaging Container
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Thread Title, Avatar, active status
            ChatHeader(
                threadId = activeThreadId,
                onBack = { /* Optional back if we have thread state, but here we stay split */ },
                viewModel = viewModel
            )

            HorizontalDivider(color = YovxGrey)

            // Horizontal active threads selector list (Side panel as Horizontal bar for mobile ergonomics)
            ActiveThreadsBar(
                activeThreadId = activeThreadId,
                onThreadSelected = { threadId -> viewModel.activeThreadId.value = threadId }
            )

            HorizontalDivider(color = YovxGrey)

            // Messages bubbles list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { message ->
                    MessageBubbleItem(
                        message = message,
                        onReact = { reaction -> viewModel.reactToMessage(message, reaction) }
                    )
                }

                // AI Typing indicator bubble
                if (isChatTyping) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(YovxCharcoal)
                                    .border(0.5.dp, YovxGrey, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        color = YovxAccent,
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 1.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (activeThreadId == "ai_assistant") "Yovx AI is thinking..." else "Sophia is typing...",
                                        color = YovxTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = YovxGrey)

            // Bottom message composer row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .background(YovxCharcoal)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = { Text("Write a message...", fontSize = 13.sp) },
                    maxLines = 4,
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
                        .testTag("chat_input"),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        viewModel.sendMessage(messageInput)
                        messageInput = ""
                    })
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        viewModel.sendMessage(messageInput)
                        messageInput = ""
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = YovxPrimary),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Chat Message",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatHeader(
    threadId: String,
    onBack: () -> Unit,
    viewModel: YovxViewModel
) {
    val (title, avatar, subtitle) = when (threadId) {
        "ai_assistant" -> Triple("Yovx Gemini AI", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150", "Built-in Assistant • Online")
        "sophia_wanderlust" -> Triple("Sophia Martinez", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150", "Creator • Active 5m ago")
        "group_main" -> Triple("Global Community Chat", "https://images.unsplash.com/photo-1614680376593-902f74fa0d41?w=150", "Active Group • 4 online")
        else -> Triple("Inbox", "", "")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(YovxCharcoal)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatar)
                .crossfade(true)
                .build(),
            contentDescription = "Thread Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .border(1.dp, YovxAccent, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = YovxTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (threadId == "ai_assistant") {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Gemini",
                        tint = YovxAccent,
                        modifier = Modifier.size(14.dp)
                    )
                } else if (threadId == "sophia_wanderlust") {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = YovxVerified,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Text(
                text = subtitle,
                color = if (threadId == "ai_assistant") YovxAccent else YovxTextSecondary,
                fontSize = 11.sp,
                fontWeight = if (threadId == "ai_assistant") FontWeight.Bold else FontWeight.Normal
            )
        }
        IconButton(onClick = { /* Simulated video calling as required by Messaging specs */ }) {
            Icon(
                imageVector = Icons.Default.VideoCall,
                contentDescription = "Video Call",
                tint = YovxTextPrimary
            )
        }
    }
}

@Composable
fun ActiveThreadsBar(
    activeThreadId: String,
    onThreadSelected: (String) -> Unit
) {
    val threads = listOf(
        Triple("ai_assistant", "Yovx AI ✨", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150"),
        Triple("sophia_wanderlust", "Sophia 🗺️", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150"),
        Triple("group_main", "Global Group 👥", "https://images.unsplash.com/photo-1614680376593-902f74fa0d41?w=150")
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(YovxCharcoal)
            .padding(vertical = 10.dp),
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(threads) { thread ->
            val isActive = activeThreadId == thread.first
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isActive) YovxPrimary.copy(alpha = 0.15f) else YovxGrey.copy(alpha = 0.5f))
                    .border(
                        width = 1.dp,
                        color = if (isActive) YovxPrimary else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onThreadSelected(thread.first) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("chat_thread_${thread.first}")
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(thread.third)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Thread avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = thread.second,
                    color = if (isActive) YovxPrimary else YovxTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MessageBubbleItem(
    message: MessageEntity,
    onReact: (String) -> Unit
) {
    val isMe = message.isFromMe
    var showReactionsRow by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        // Small Sender Label (only for group thread when not me)
        if (!isMe && message.threadId == "group_main") {
            Text(
                text = message.senderName,
                color = YovxTextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isMe) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(message.senderAvatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Sender Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .padding(end = 4.dp)
                )
            }

            // Message Bubble Text
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        )
                    )
                    .background(if (isMe) YovxPrimary.copy(alpha = 0.8f) else Color(0x1F252530))
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x05FFFFFF)))
                        ),
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        )
                    )
                    .clickable { showReactionsRow = !showReactionsRow }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .widthIn(max = 240.dp)
            ) {
                Column {
                    Text(
                        text = message.content,
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    // Display emoji reaction if exists
                    if (message.reaction != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = message.reaction, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Read receipts (double tick in green) for user's own sent messages
            if (isMe) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = "Read",
                    tint = YovxGreen,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Emoji Reaction Selector popup row
        AnimatedVisibility(
            visible = showReactionsRow,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .background(YovxCharcoal, RoundedCornerShape(12.dp))
                    .border(0.5.dp, YovxGrey, RoundedCornerShape(12.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("❤️", "👍", "😂", "😮", "😢", "🔥").forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .clickable {
                                onReact(emoji)
                                showReactionsRow = false
                            }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}
