package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val coverUrl: String,
    val bio: String,
    val website: String,
    val followersCount: Int,
    val followingCount: Int,
    val isVerified: Boolean = false,
    val isCreator: Boolean = false,
    val isBusiness: Boolean = false,
    val gender: String = "Not Specified",
    val birthday: String = "Jan 01, 2000"
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val username: String,
    val userDisplayName: String,
    val userAvatarUrl: String,
    val isUserVerified: Boolean = false,
    val content: String,
    val mediaUrl: String,
    val mediaType: String, // "text", "photo", "video", "poll"
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val isReposted: Boolean = false,
    // Simple poll storage (comma-separated string for options & votes)
    val pollOptionsJson: String? = null, // "Option A,Option B,Option C"
    val pollVotesJson: String? = null, // "12,4,45"
    val selectedPollOption: Int = -1 // User's chosen poll index (-1 for none)
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val userAvatarUrl: String,
    val mediaUrl: String,
    val isVideo: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val isViewed: Boolean = false,
    val caption: String? = null,
    val musicTitle: String? = null
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val username: String,
    val userAvatarUrl: String,
    val isVerified: Boolean = false,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val parentId: Long? = null
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val threadId: String, // "user1", "group_main", "ai_assistant"
    val senderId: String,
    val senderName: String,
    val senderAvatarUrl: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = false,
    val isRead: Boolean = false,
    val reaction: String? = null // Emoji reaction (e.g. "❤️", "👍")
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String, // "Electronics", "Fashion", "Real Estate", "Automotive", "Entertainment"
    val sellerName: String,
    val sellerAvatarUrl: String,
    val sellerRating: Float = 4.5f,
    val location: String = "San Francisco, CA",
    val timestamp: Long = System.currentTimeMillis(),
    val isSold: Boolean = false
)

@Entity(tableName = "user_interactions")
data class UserInteractionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val postUserId: String, // creator of the post
    val interactionType: String, // "view", "like", "comment", "share", "react"
    val scoreValue: Float = 1f, // predefined weight
    val timestamp: Long = System.currentTimeMillis()
)
