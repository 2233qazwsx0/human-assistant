package com.humanassistant.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends")
    fun getAllFriends(): Flow<List<Friend>>

    @Query("SELECT * FROM friends WHERE apiKey = :apiKey")
    suspend fun getFriendByApiKey(apiKey: String): Friend?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: Friend)

    @Update
    suspend fun updateFriend(friend: Friend)
}
