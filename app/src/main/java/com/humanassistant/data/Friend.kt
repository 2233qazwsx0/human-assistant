package com.humanassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey val apiKey: String,
    val name: String,
    val balance: Int = 100
)
