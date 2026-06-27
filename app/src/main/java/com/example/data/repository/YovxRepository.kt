package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class YovxRepository(
    private val userDao: UserDao,
    private val postDao: PostDao,
    private val storyDao: StoryDao,
    private val commentDao: CommentDao,
    private val messageDao: MessageDao,
    private val productDao: ProductDao,
    private val userInteractionDao: UserInteractionDao
) {
    // Reactive Flows
    val allPosts: Flow<List<PostEntity>> = postDao.getAllPostsFlow()
    val videoPosts: Flow<List<PostEntity>> = postDao.getAllVideoPostsFlow()
    val allStories: Flow<List<StoryEntity>> = storyDao.getAllStoriesFlow()
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProductsFlow()
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsersFlow()
    val allInteractions: Flow<List<UserInteractionEntity>> = userInteractionDao.getAllInteractionsFlow()

    fun getUserById(userId: String): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)
    fun getPostsByUserId(userId: String): Flow<List<PostEntity>> = postDao.getPostsByUserIdFlow(userId)
    fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>> = commentDao.getCommentsForPostFlow(postId)
    fun getMessagesForThread(threadId: String): Flow<List<MessageEntity>> = messageDao.getMessagesForThreadFlow(threadId)

    suspend fun getAllInteractionsList(): List<UserInteractionEntity> = withContext(Dispatchers.IO) {
        userInteractionDao.getAllInteractions()
    }

    suspend fun insertInteraction(interaction: UserInteractionEntity) = withContext(Dispatchers.IO) {
        userInteractionDao.insertInteraction(interaction)
    }

    // Database Actions
    suspend fun insertUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    suspend fun insertPost(post: PostEntity) = withContext(Dispatchers.IO) {
        postDao.insertPost(post)
    }

    suspend fun updatePost(post: PostEntity) = withContext(Dispatchers.IO) {
        postDao.updatePost(post)
    }

    suspend fun insertStory(story: StoryEntity) = withContext(Dispatchers.IO) {
        storyDao.insertStory(story)
    }

    suspend fun markStoryAsViewed(storyId: Long) = withContext(Dispatchers.IO) {
        storyDao.markAsViewed(storyId)
    }

    suspend fun insertComment(comment: CommentEntity) = withContext(Dispatchers.IO) {
        commentDao.insertComment(comment)
    }

    suspend fun insertMessage(message: MessageEntity) = withContext(Dispatchers.IO) {
        messageDao.insertMessage(message)
    }

    suspend fun updateMessage(message: MessageEntity) = withContext(Dispatchers.IO) {
        messageDao.updateMessage(message)
    }

    suspend fun insertProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
    }

    suspend fun getProductsByCategory(category: String): Flow<List<ProductEntity>> {
        return if (category == "All") allProducts else productDao.getProductsByCategoryFlow(category)
    }

    fun searchProducts(query: String): Flow<List<ProductEntity>> {
        return productDao.searchProductsFlow(query)
    }

    /**
     * Initializes the database with highly visual, rich mock data if empty.
     */
    suspend fun initMockData() = withContext(Dispatchers.IO) {
        val existingUsers = userDao.getAllUsersFlow().firstOrNull() ?: emptyList()
        if (existingUsers.isNotEmpty()) return@withContext

        // 1. Insert Users
        val users = listOf(
            UserEntity(
                id = "me",
                username = "oscar_builds",
                displayName = "Oscar Nkhata",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800",
                bio = "Building the future of social networks on Android. Yovx Founder.",
                website = "https://yovx.com/oscar",
                followersCount = 12400,
                followingCount = 482,
                isVerified = true,
                isCreator = true
            ),
            UserEntity(
                id = "yovx_official",
                username = "yovx_official",
                displayName = "Yovx Ecosystem",
                avatarUrl = "https://images.unsplash.com/photo-1614680376593-902f74fa0d41?w=150",
                coverUrl = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=800",
                bio = "Welcome to Yovx.com! Enjoy stories, vertical Reels, an intelligent AI Chat companion, and localized Buy & Sell.",
                website = "https://yovx.com",
                followersCount = 1250000,
                followingCount = 10,
                isVerified = true,
                isCreator = true
            ),
            UserEntity(
                id = "sophia_travel",
                username = "sophia_wanderlust",
                displayName = "Sophia Martinez",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                coverUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800",
                bio = "Wandering explorer. Capturing remote corners of our gorgeous planet. Next up: Tokyo 🇯🇵",
                website = "https://sophia.travel",
                followersCount = 48290,
                followingCount = 310,
                isVerified = true,
                isCreator = true
            ),
            UserEntity(
                id = "chef_marcus",
                username = "chef_marcus",
                displayName = "Chef Marcus",
                avatarUrl = "https://images.unsplash.com/photo-1577219491135-ce391730fb2c?w=150",
                coverUrl = "https://images.unsplash.com/photo-1556910103-1c02745aae4d?w=800",
                bio = "Michelin star recipes demystified. Making culinary masterpieces simple for your kitchen. 🔪",
                website = "https://chefmarcus.com",
                followersCount = 289100,
                followingCount = 129,
                isVerified = true,
                isCreator = true,
                isBusiness = true
            ),
            UserEntity(
                id = "gadget_guy",
                username = "tech_review_daily",
                displayName = "Tech Review Daily",
                avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
                coverUrl = "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=800",
                bio = "Unboxing future tech, today. Fast, honest reviews of everything from smartphones to quantum accessories.",
                website = "https://youtube.com/techreview",
                followersCount = 562000,
                followingCount = 89,
                isVerified = false,
                isCreator = true
            )
        )
        users.forEach { userDao.insertUser(it) }

        // 2. Insert Stories
        val stories = listOf(
            StoryEntity(
                username = "sophia_wanderlust",
                userAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                mediaUrl = "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=400",
                caption = "Sunset sailing ⛵️🌅",
                musicTitle = "Ambient Solitude - Suren"
            ),
            StoryEntity(
                username = "chef_marcus",
                userAvatarUrl = "https://images.unsplash.com/photo-1577219491135-ce391730fb2c?w=150",
                mediaUrl = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400",
                caption = "Fresh firewood pizza is ready! 🍕🔥",
                musicTitle = "L'Italiano - Toto Cutugno"
            ),
            StoryEntity(
                username = "tech_review_daily",
                userAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
                mediaUrl = "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400",
                caption = "Unboxing a brand new smart wrist band! ⌚️⚡️",
                musicTitle = "Cyberpunk Beats - SynthX"
            ),
            StoryEntity(
                username = "yovx_official",
                userAvatarUrl = "https://images.unsplash.com/photo-1614680376593-902f74fa0d41?w=150",
                mediaUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400",
                caption = "Welcome to the future. Explore Reels, Chats & Market.",
                musicTitle = "Yovx Intro Theme"
            )
        )
        stories.forEach { storyDao.insertStory(it) }

        // 3. Insert Posts (Some with media, some text, one poll)
        val posts = listOf(
            PostEntity(
                userId = "yovx_official",
                username = "yovx_official",
                userDisplayName = "Yovx Ecosystem",
                userAvatarUrl = "https://images.unsplash.com/photo-1614680376593-902f74fa0d41?w=150",
                isUserVerified = true,
                content = "🚀 We are thrilled to welcome you to Yovx.com, the ultimate next-generation social ecosystem.\n\n" +
                        "This app converges three massive social worlds:\n" +
                        "1️⃣ The dynamic vertical Reel format of TikTok\n" +
                        "2️⃣ The curated visual storytelling and direct messaging of Instagram\n" +
                        "3️⃣ The community groups, business profiles, and marketplace of Facebook\n\n" +
                        "Feel free to explore and use the built-in Gemini Assistant to generate content or chat in direct messaging!",
                mediaUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800",
                mediaType = "photo",
                likesCount = 4252,
                commentsCount = 289,
                sharesCount = 1045
            ),
            PostEntity(
                userId = "sophia_travel",
                username = "sophia_wanderlust",
                userDisplayName = "Sophia Martinez",
                userAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                isUserVerified = true,
                content = "Climbed Mount Fuji last night just to catch the breathtaking sunrise 🗻✨. Words fail to express the sheer magic of being above the clouds as the world awakens. Worth every single step!",
                mediaUrl = "https://images.unsplash.com/photo-1491555103944-7c647fd85706?w=800",
                mediaType = "photo",
                likesCount = 8240,
                commentsCount = 420,
                sharesCount = 189
            ),
            // Video/Reels posts (TikTok style)
            PostEntity(
                userId = "sophia_travel",
                username = "sophia_wanderlust",
                userDisplayName = "Sophia Martinez",
                userAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                isUserVerified = true,
                content = "Cruising through the scenic streets of Kyoto in the rain. 🌧️🏯 There is nothing like looking at the glowing paper lanterns reflect on wet cobblestones. Pure therapy.",
                mediaUrl = "Kyoto Rain Reels", // Marked as custom visual label
                mediaType = "video",
                likesCount = 24500,
                commentsCount = 1380,
                sharesCount = 5820
            ),
            PostEntity(
                userId = "chef_marcus",
                username = "chef_marcus",
                userDisplayName = "Chef Marcus",
                userAvatarUrl = "https://images.unsplash.com/photo-1577219491135-ce391730fb2c?w=150",
                isUserVerified = true,
                content = "Sizzling Garlic Butter Shrimps 🦐🔥. Quick, simple, and packed with bold rustic flavors. Full step-by-step video outline in my Bio! What are you cooking tonight?",
                mediaUrl = "Gourmet Shrimp Reels",
                mediaType = "video",
                likesCount = 18900,
                commentsCount = 890,
                sharesCount = 3210
            ),
            PostEntity(
                userId = "gadget_guy",
                username = "tech_review_daily",
                userDisplayName = "Tech Review Daily",
                userAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
                isUserVerified = false,
                content = "Which upcoming consumer tech category are you most excited to purchase in 2026? Vote below! 👇",
                mediaUrl = "",
                mediaType = "poll",
                pollOptionsJson = "AR Smart Glasses,Neural Wearables,Foldable Triple-Screen Phones,AI Standalone Assistants",
                pollVotesJson = "412,189,321,58",
                likesCount = 120,
                commentsCount = 89,
                sharesCount = 15
            ),
            PostEntity(
                userId = "gadget_guy",
                username = "tech_review_daily",
                userDisplayName = "Tech Review Daily",
                userAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
                isUserVerified = false,
                content = "Hands on with the brand new CyberGlass XR headset! 🕶️⚡️ The pass-through latency is absolutely zero and gestures feel so intuitive. Check out this view!",
                mediaUrl = "https://images.unsplash.com/photo-1593508512255-86ab42a8e620?w=800",
                mediaType = "photo",
                likesCount = 1890,
                commentsCount = 210,
                sharesCount = 45
            )
        )
        posts.forEach { postDao.insertPost(it) }

        // 4. Insert Products (Marketplace)
        val products = listOf(
            ProductEntity(
                title = "Yovx Creator Pro Streaming Mic",
                description = "Ultra-premium USB-C studio condenser microphone with active noise suppression, integrated pop filter, and a custom multi-color glow ring. Perfect for Reels, Podcasts, and high-fidelity video voiceovers.",
                price = 129.99,
                imageUrl = "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=600",
                category = "Electronics",
                sellerName = "Yovx Merch",
                sellerAvatarUrl = "https://images.unsplash.com/photo-1614680376593-902f74fa0d41?w=150",
                sellerRating = 4.9f,
                location = "San Francisco, CA"
            ),
            ProductEntity(
                title = "Cyberpunk Carbon Leather Jacket",
                description = "Waterproof synthetic leather jacket with neon detailing along the sleeves and collar. Fits tailored and snug, size Medium. Extremely durable and perfect for street photography sessions.",
                price = 85.00,
                imageUrl = "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=600",
                category = "Fashion",
                sellerName = "Sophia Martinez",
                sellerAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                sellerRating = 4.8f,
                location = "Los Angeles, CA"
            ),
            ProductEntity(
                title = "Smart Plant Oasis Pod",
                description = "Self-watering aesthetic indoor garden planter with full-spectrum LED grow light, automatic soil moisture sensor, and companion app tracking. Ideal for busy desks and low-light kitchen corners.",
                price = 45.50,
                imageUrl = "https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=600",
                category = "Electronics",
                sellerName = "Marcus Kitchen",
                sellerAvatarUrl = "https://images.unsplash.com/photo-1577219491135-ce391730fb2c?w=150",
                sellerRating = 4.7f,
                location = "Seattle, WA"
            ),
            ProductEntity(
                title = "Retro-Styled Urban Fixie Bike",
                description = "Custom single-speed fixie bicycle with steel lugged frame, tan leather saddle and handlebar grips. Frame size 54cm. Barely used, pristine condition. Lightweight and extremely smooth commuter.",
                price = 320.00,
                imageUrl = "https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=600",
                category = "Automotive",
                sellerName = "Oscar Nkhata",
                sellerAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                sellerRating = 5.0f,
                location = "New York, NY"
            )
        )
        products.forEach { productDao.insertProduct(it) }

        // 5. Insert Messages (AI Companion & Group)
        val messages = listOf(
            MessageEntity(
                threadId = "ai_assistant",
                senderId = "ai_assistant",
                senderName = "Yovx AI",
                senderAvatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150",
                content = "Hello Oscar! I am your Yovx Gemini Assistant. I can help you compose viral captions, translate posts, or chat about anything! Try messaging me below.",
                isFromMe = false,
                isRead = true
            ),
            MessageEntity(
                threadId = "sophia_wanderlust",
                senderId = "sophia_travel",
                senderName = "Sophia Martinez",
                senderAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                content = "Hey Oscar! Loved your latest post about the design structure of Yovx. Are we still on track to host the live stream stream tomorrow?",
                isFromMe = false,
                isRead = false
            ),
            MessageEntity(
                threadId = "group_main",
                senderId = "yovx_official",
                senderName = "System",
                senderAvatarUrl = "https://images.unsplash.com/photo-1614680376593-902f74fa0d41?w=150",
                content = "Global Yovx Community Group Chat created. Be polite, share awesome ideas, and connect!",
                isFromMe = false,
                isRead = true
            ),
            MessageEntity(
                threadId = "group_main",
                senderId = "chef_marcus",
                senderName = "Chef Marcus",
                senderAvatarUrl = "https://images.unsplash.com/photo-1577219491135-ce391730fb2c?w=150",
                content = "Hey everyone! Glad to join the group. Sharing cooking reels soon! Let me know if you want any specific recipes 👨‍🍳",
                isFromMe = false,
                isRead = true
            )
        )
        messages.forEach { messageDao.insertMessage(it) }

        // 6. Insert Comments
        val sampleComments = listOf(
            CommentEntity(
                postId = 1,
                username = "sophia_wanderlust",
                userAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                isVerified = true,
                content = "This looks like an absolute game changer! Can't wait to migrate my full audience here! 🥳"
            ),
            CommentEntity(
                postId = 1,
                username = "tech_review_daily",
                userAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
                content = "The integration of vertical reels and marketplace is flawless. Clean design!"
            ),
            CommentEntity(
                postId = 2,
                username = "chef_marcus",
                userAvatarUrl = "https://images.unsplash.com/photo-1577219491135-ce391730fb2c?w=150",
                isVerified = true,
                content = "Mt. Fuji is definitely on my travel list! Breathtaking view, Sophia! 🗻"
            )
        )
        sampleComments.forEach { commentDao.insertComment(it) }
    }
}
