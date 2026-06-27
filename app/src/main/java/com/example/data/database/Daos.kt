package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdFlow(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY followersCount DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPostsFlow(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE mediaType = 'video' ORDER BY timestamp DESC")
    fun getAllVideoPostsFlow(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY timestamp DESC")
    fun getPostsByUserIdFlow(userId: String): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Long

    @Update
    suspend fun updatePost(post: PostEntity)

    @Delete
    suspend fun deletePost(post: PostEntity)
}

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories ORDER BY timestamp DESC")
    fun getAllStoriesFlow(): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Query("UPDATE stories SET isViewed = 1 WHERE id = :storyId")
    suspend fun markAsViewed(storyId: Long)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPostFlow(postId: Long): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getMessagesForThreadFlow(threadId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    suspend fun getAllMessages(): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Update
    suspend fun updateMessage(message: MessageEntity)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY timestamp DESC")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY timestamp DESC")
    fun getProductsByCategoryFlow(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchProductsFlow(query: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)
}

@Dao
interface UserInteractionDao {
    @Query("SELECT * FROM user_interactions ORDER BY timestamp DESC")
    fun getAllInteractionsFlow(): Flow<List<UserInteractionEntity>>

    @Query("SELECT * FROM user_interactions")
    suspend fun getAllInteractions(): List<UserInteractionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: UserInteractionEntity)
}
