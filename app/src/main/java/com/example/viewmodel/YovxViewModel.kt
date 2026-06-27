package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import com.example.data.network.GeminiService
import com.example.data.repository.YovxRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class YovxViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: YovxRepository

    // Screen navigation state
    private val _currentScreen = MutableStateFlow("Feed") // "Feed", "Reels", "Messaging", "Marketplace", "Profile", "Search", "PostCreator", "CreatorDashboard"
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Screen sub-states
    val activeThreadId = MutableStateFlow("ai_assistant") // active chat thread
    val searchCategory = MutableStateFlow("All") // Active marketplace category
    val searchQuery = MutableStateFlow("") // Search query
    val isAiLoading = MutableStateFlow(false) // For caption generation or translation loading
    val aiGeneratedCaption = MutableStateFlow("") // holds newly generated caption
    val isVideoAiLoading = MutableStateFlow(false) // For video caption generation loading
    val videoAiGeneratedCaption = MutableStateFlow("") // holds newly generated video caption
    val activePostIdForComments = MutableStateFlow<Long?>(null) // opened comment section for post
    val reelsFeedMode = MutableStateFlow("Recommended") // "Recommended" or "Recent"

    // State flows from Room
    val allPosts: StateFlow<List<PostEntity>>
    val videoPosts: StateFlow<List<PostEntity>>
    val allStories: StateFlow<List<StoryEntity>>
    val allProducts: StateFlow<List<ProductEntity>>
    val allUsers: StateFlow<List<UserEntity>>
    val currentUser: StateFlow<UserEntity?>
    val allInteractions: StateFlow<List<UserInteractionEntity>>

    // Selected user profile view flow
    val selectedUserIdForView = MutableStateFlow("me")
    val viewedUser: StateFlow<UserEntity?>

    // Active Chat flow
    val chatMessages: StateFlow<List<MessageEntity>>

    // Active Comments flow
    val activePostComments: StateFlow<List<CommentEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = YovxRepository(
            database.userDao(),
            database.postDao(),
            database.storyDao(),
            database.commentDao(),
            database.messageDao(),
            database.productDao(),
            database.userInteractionDao()
        )

        // Bind flows
        allPosts = repository.allPosts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allInteractions = repository.allInteractions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
        videoPosts = combine(
            repository.videoPosts,
            repository.allInteractions,
            reelsFeedMode
        ) { posts, interactions, mode ->
            if (mode == "Recommended") {
                scoreAndSortPosts(posts, interactions)
            } else {
                posts.sortedByDescending { it.timestamp }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allStories = repository.allStories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allProducts = repository.allProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allUsers = repository.allUsers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        currentUser = repository.getUserById("me").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        viewedUser = selectedUserIdForView.flatMapLatest { userId ->
            repository.getUserById(userId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        chatMessages = activeThreadId.flatMapLatest { threadId ->
            repository.getMessagesForThread(threadId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        activePostComments = activePostIdForComments.flatMapLatest { postId ->
            if (postId != null) repository.getCommentsForPost(postId) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Initialize mock data on startup
        viewModelScope.launch {
            repository.initMockData()
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // --- Interaction Methods ---

    fun toggleLike(post: PostEntity) {
        viewModelScope.launch {
            val updatedPost = post.copy(
                isLiked = !post.isLiked,
                likesCount = if (post.isLiked) post.likesCount - 1 else post.likesCount + 1
            )
            repository.updatePost(updatedPost)
            if (!post.isLiked) {
                recordUserInteraction(post.id, post.userId, "like")
            }
        }
    }

    fun toggleBookmark(post: PostEntity) {
        viewModelScope.launch {
            val updatedPost = post.copy(
                isBookmarked = !post.isBookmarked
            )
            repository.updatePost(updatedPost)
        }
    }

    fun toggleRepost(post: PostEntity) {
        viewModelScope.launch {
            val updatedPost = post.copy(
                isReposted = !post.isReposted,
                sharesCount = if (post.isReposted) post.sharesCount - 1 else post.sharesCount + 1
            )
            repository.updatePost(updatedPost)
            if (!post.isReposted) {
                recordUserInteraction(post.id, post.userId, "share")
            }
        }
    }

    fun submitComment(postId: Long, commentText: String, parentId: Long? = null) {
        if (commentText.trim().isEmpty()) return
        viewModelScope.launch {
            val me = currentUser.value
            val comment = CommentEntity(
                postId = postId,
                username = me?.username ?: "oscar_builds",
                userAvatarUrl = me?.avatarUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                isVerified = me?.isVerified ?: true,
                content = commentText,
                parentId = parentId
            )
            repository.insertComment(comment)

            // Update comment count on post
            val postList = allPosts.value
            val post = postList.find { it.id == postId }
            if (post != null) {
                repository.updatePost(post.copy(commentsCount = post.commentsCount + 1))
                recordUserInteraction(postId, post.userId, "comment")
            }
        }
    }

    fun submitPollVote(post: PostEntity, optionIndex: Int) {
        if (post.selectedPollOption != -1) return // Already voted
        viewModelScope.launch {
            val votes = post.pollVotesJson?.split(",")?.map { it.toIntOrNull() ?: 0 }?.toMutableList() ?: mutableListOf(0, 0, 0, 0)
            if (optionIndex in votes.indices) {
                votes[optionIndex] = votes[optionIndex] + 1
            }
            val updatedPost = post.copy(
                pollVotesJson = votes.joinToString(","),
                selectedPollOption = optionIndex
            )
            repository.updatePost(updatedPost)
        }
    }

    // --- Message Section & Gemini Chat Assistant ---

    val isChatTyping = MutableStateFlow(false)

    fun sendMessage(content: String) {
        if (content.trim().isEmpty()) return
        val currentThread = activeThreadId.value

        viewModelScope.launch {
            val me = currentUser.value
            val userMsg = MessageEntity(
                threadId = currentThread,
                senderId = "me",
                senderName = me?.displayName ?: "Oscar Nkhata",
                senderAvatarUrl = me?.avatarUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                content = content,
                isFromMe = true,
                isRead = true
            )
            repository.insertMessage(userMsg)

            // Auto reply Simulation or Gemini API Trigger
            if (currentThread == "ai_assistant") {
                isChatTyping.value = true
                
                // Call Gemini REST Service in the background
                val aiReplyContent = GeminiService.generateText(
                    prompt = content,
                    systemInstruction = "You are Yovx Gemini, a premium AI Assistant inside Yovx.com (the social app that converges features of Facebook, TikTok, and Instagram). Keep your answers friendly, engaging, concise, and related to social media, creative creation, design, or standard chat topics."
                )
                
                val aiMsg = MessageEntity(
                    threadId = currentThread,
                    senderId = "ai_assistant",
                    senderName = "Yovx AI",
                    senderAvatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150",
                    content = aiReplyContent,
                    isFromMe = false,
                    isRead = true
                )
                repository.insertMessage(aiMsg)
                isChatTyping.value = false
            } else if (currentThread == "sophia_wanderlust") {
                isChatTyping.value = true
                delay(1500) // simulated typing delay
                val replyText = when {
                    content.contains("live", ignoreCase = true) -> "Yes, definitely on track! 7 PM PST works perfectly for the Japan travel preview. I've prepared some fantastic drone photos!"
                    content.contains("hello", ignoreCase = true) || content.contains("hey", ignoreCase = true) -> "Hey there! How's your day going? Just editing some travel logs."
                    else -> "That sounds amazing! Let's definitely post that as a collaborative post on Yovx so both our followers can enjoy it!"
                }
                val mockMsg = MessageEntity(
                    threadId = currentThread,
                    senderId = "sophia_travel",
                    senderName = "Sophia Martinez",
                    senderAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                    content = replyText,
                    isFromMe = false,
                    isRead = true
                )
                repository.insertMessage(mockMsg)
                isChatTyping.value = false
            }
        }
    }

    fun reactToMessage(message: MessageEntity, reactionSymbol: String) {
        viewModelScope.launch {
            repository.updateMessage(message.copy(reaction = reactionSymbol))
        }
    }

    // --- Creator & Content Generation (Gemini Features) ---

    fun generateAIPostCaption(topic: String, style: String) {
        if (topic.trim().isEmpty()) return
        viewModelScope.launch {
            isAiLoading.value = true
            val stylePrompt = when (style) {
                "Viral (TikTok)" -> "vibrant, short, emojis, trendy tags, hook-focused"
                "Aesthetic (Instagram)" -> "minimal, clean, sophisticated spacing, artistic, warm tone"
                "Engaging (Facebook)" -> "conversational, longer outline, invites opinion/comments, thoughtful"
                else -> "friendly and professional"
            }
            val prompt = "Generate a social media caption about: '$topic'. Style: $stylePrompt. Include 3-4 trending hashtags. Keep it under 150 words."
            val caption = GeminiService.generateText(prompt)
            aiGeneratedCaption.value = caption
            isAiLoading.value = false
        }
    }

    fun generateAiVideoCaption(
        videoTitle: String,
        videoContext: String,
        audioVibe: String,
        targetAudience: String,
        style: String
    ) {
        viewModelScope.launch {
            isVideoAiLoading.value = true
            val prompt = """
                You are Yovx Gemini, a premium AI video assistant. Create a high-converting, extremely engaging caption for a video upload.
                
                VIDEO INFO:
                - Title/Theme: $videoTitle
                - Scene Details & Visuals: $videoContext
                - Music/Audio Vibe: $audioVibe
                - Target Audience: $targetAudience
                - Intended Platform Style: $style
                
                REQUIREMENTS:
                1. Create an attention-grabbing Hook in the first line.
                2. Write a compelling main body with creative spacing, appropriate emojis, and clear pacing.
                3. Propose 4-5 hyper-relevant trending hashtags.
                4. Include a simulated 'Segmented Timeline / Subtitles & Chapters' for the video (e.g. 0:01 - Intro, 0:05 - Visual climax, etc.) so creators can paste it as video sections.
                5. Keep the total output concise, elegant, and ready to go viral.
            """.trimIndent()
            
            val caption = GeminiService.generateText(prompt)
            videoAiGeneratedCaption.value = caption
            isVideoAiLoading.value = false
        }
    }

    fun translatePost(post: PostEntity, targetLanguage: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val prompt = "Translate the following social media post text into $targetLanguage. Keep the original formatting and emojis. Only return the translated text without explanations:\n\n'${post.content}'"
            val translation = GeminiService.generateText(prompt)
            onResult(translation)
        }
    }

    // --- Content Creation Methods ---

    fun publishNewPost(content: String, mediaUrl: String, mediaType: String, pollOptions: List<String> = emptyList()) {
        if (content.trim().isEmpty() && mediaUrl.isEmpty()) return
        viewModelScope.launch {
            val me = currentUser.value
            val pollOptionsStr = if (mediaType == "poll" && pollOptions.isNotEmpty()) pollOptions.joinToString(",") else null
            val pollVotesStr = if (mediaType == "poll" && pollOptions.isNotEmpty()) pollOptions.map { "0" }.joinToString(",") else null

            val newPost = PostEntity(
                userId = me?.id ?: "me",
                username = me?.username ?: "oscar_builds",
                userDisplayName = me?.displayName ?: "Oscar Nkhata",
                userAvatarUrl = me?.avatarUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                isUserVerified = me?.isVerified ?: true,
                content = content,
                mediaUrl = mediaUrl,
                mediaType = mediaType,
                pollOptionsJson = pollOptionsStr,
                pollVotesJson = pollVotesStr,
                timestamp = System.currentTimeMillis()
            )
            repository.insertPost(newPost)
            _currentScreen.value = "Feed" // Return to home
        }
    }

    fun publishNewStory(mediaUrl: String, caption: String) {
        if (mediaUrl.isEmpty()) return
        viewModelScope.launch {
            val me = currentUser.value
            val newStory = StoryEntity(
                username = me?.username ?: "oscar_builds",
                userAvatarUrl = me?.avatarUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                mediaUrl = mediaUrl,
                caption = caption,
                timestamp = System.currentTimeMillis()
            )
            repository.insertStory(newStory)
            _currentScreen.value = "Feed"
        }
    }

    fun markStoryAsViewed(storyId: Long) {
        viewModelScope.launch {
            repository.markStoryAsViewed(storyId)
        }
    }

    fun createMarketplaceListing(title: String, description: String, price: Double, category: String, imageUrl: String) {
        if (title.isEmpty() || price <= 0) return
        viewModelScope.launch {
            val me = currentUser.value
            val product = ProductEntity(
                title = title,
                description = description,
                price = price,
                imageUrl = imageUrl.ifEmpty { "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600" }, // default placeholder
                category = category,
                sellerName = me?.displayName ?: "Oscar Nkhata",
                sellerAvatarUrl = me?.avatarUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                sellerRating = 4.9f,
                location = "San Francisco, CA"
            )
            repository.insertProduct(product)
            _currentScreen.value = "Marketplace"
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun editProfile(displayName: String, username: String, avatarUrl: String, bio: String, website: String, birthday: String) {
        viewModelScope.launch {
            val current = currentUser.value
            if (current != null) {
                val updated = current.copy(
                    displayName = displayName,
                    username = username,
                    avatarUrl = avatarUrl,
                    bio = bio,
                    website = website,
                    birthday = birthday
                )
                repository.updateUser(updated)
            }
        }
    }

    // --- Recommendation and Machine Learning Engine ---

    private val STOP_WORDS = setOf(
        "the", "and", "a", "of", "to", "in", "is", "that", "it", "on", "for", "with", "as", "at", "by", "an", "be", "this", "are", "from", "or", "i", "you", "we", "they", "he", "she", "it", "my", "our", "your", "his", "her", "their", "me", "them", "us", "him", "here", "there", "when", "where", "how", "why", "who", "what", "which"
    )

    private fun extractKeywords(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^a-zA-Z0-9\\s]"), "") // remove punctuation
            .split(Regex("\\s+"))                  // split by spaces
            .filter { it.length > 2 && it !in STOP_WORDS }
    }

    fun scoreAndSortPosts(
        posts: List<PostEntity>,
        interactions: List<UserInteractionEntity>
    ): List<PostEntity> {
        // 1. Creator preference scores
        val creatorPref = mutableMapOf<String, Float>()
        // 2. Keyword preference scores
        val keywordPref = mutableMapOf<String, Float>()

        val postsMap = posts.associateBy { it.id }

        interactions.forEach { interaction ->
            val weight = when (interaction.interactionType) {
                "view" -> 1.0f
                "like" -> 5.0f
                "comment" -> 8.0f
                "share" -> 10.0f
                "react" -> 6.0f
                else -> 1.0f
            }
            
            // Creator preferences
            val creatorId = interaction.postUserId
            creatorPref[creatorId] = (creatorPref[creatorId] ?: 0f) + weight

            // Keyword preferences
            val post = postsMap[interaction.postId]
            if (post != null) {
                val keywords = extractKeywords(post.content)
                keywords.forEach { word ->
                    keywordPref[word] = (keywordPref[word] ?: 0f) + weight
                }
            }
        }

        // 3. Score candidate posts
        val scoredPosts = posts.map { post ->
            val baseScore = post.likesCount * 0.1f + post.commentsCount * 0.3f + post.sharesCount * 0.5f
            val creatorBoost = creatorPref[post.userId] ?: 0f
            
            val keywords = extractKeywords(post.content)
            var keywordBoost = 0f
            keywords.forEach { word ->
                keywordBoost += (keywordPref[word] ?: 0f) * 0.2f
            }

            val totalScore = baseScore + creatorBoost + keywordBoost
            post to totalScore
        }

        return scoredPosts.sortedByDescending { it.second }.map { it.first }
    }

    fun recordUserInteraction(postId: Long, postUserId: String, interactionType: String) {
        viewModelScope.launch {
            val interaction = UserInteractionEntity(
                postId = postId,
                postUserId = postUserId,
                interactionType = interactionType,
                scoreValue = when (interactionType) {
                    "view" -> 1.0f
                    "like" -> 5.0f
                    "comment" -> 8.0f
                    "share" -> 10.0f
                    "react" -> 6.0f
                    else -> 1.0f
                }
            )
            repository.insertInteraction(interaction)
        }
    }

    fun getRecommendationInsight(post: PostEntity): Pair<Int, String> {
        val interactions = allInteractions.value
        if (interactions.isEmpty()) {
            return Pair(55, "Trending Content")
        }

        val creatorPref = mutableMapOf<String, Float>()
        val keywordPref = mutableMapOf<String, Float>()
        val postsMap = videoPosts.value.associateBy { it.id }

        interactions.forEach { interaction ->
            val weight = when (interaction.interactionType) {
                "view" -> 1.0f
                "like" -> 5.0f
                "comment" -> 8.0f
                "share" -> 10.0f
                "react" -> 6.0f
                else -> 1.0f
            }
            val creatorId = interaction.postUserId
            creatorPref[creatorId] = (creatorPref[creatorId] ?: 0f) + weight

            val p = postsMap[interaction.postId]
            if (p != null) {
                val keywords = extractKeywords(p.content)
                keywords.forEach { word ->
                    keywordPref[word] = (keywordPref[word] ?: 0f) + weight
                }
            }
        }

        val baseScore = post.likesCount * 0.1f + post.commentsCount * 0.3f + post.sharesCount * 0.5f
        val creatorBoost = creatorPref[post.userId] ?: 0f
        
        val keywords = extractKeywords(post.content)
        var keywordBoost = 0f
        var topMatchedWord = ""
        var maxWordWeight = 0f
        keywords.forEach { word ->
            val w = keywordPref[word] ?: 0f
            keywordBoost += w * 0.2f
            if (w > maxWordWeight) {
                maxWordWeight = w
                topMatchedWord = word
            }
        }

        val totalScore = baseScore + creatorBoost + keywordBoost
        val percentage = (50 + (totalScore * 2f).toInt()).coerceIn(50, 99)

        val reason = when {
            creatorBoost > 0f && keywordBoost > 0f && topMatchedWord.isNotEmpty() -> 
                "Based on your interest in @${post.username} & \"$topMatchedWord\""
            creatorBoost > 0f -> 
                "You frequently watch @${post.username}"
            keywordBoost > 0f && topMatchedWord.isNotEmpty() -> 
                "Matched topic: \"$topMatchedWord\""
            else -> 
                "Popular on Yovx Ecosystem"
        }

        return Pair(percentage, reason)
    }
}
