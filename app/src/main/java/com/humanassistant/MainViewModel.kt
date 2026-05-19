package com.humanassistant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

data class FriendInfo(
    val apiKey: String,
    val name: String,
    val balance: Int
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = application.getSharedPreferences("FriendBalance", android.content.Context.MODE_PRIVATE)
    
    private val validApiKeys = mapOf(
        "test-key" to "Test Friend",
        "friend1-key" to "Friend 1",
        "friend2-key" to "Friend 2"
    )
    
    private val _friends = MutableLiveData<List<FriendInfo>>()
    val friends: LiveData<List<FriendInfo>> = _friends
    
    init {
        loadFriends()
    }
    
    fun loadFriends() {
        val list = validApiKeys.map { (key, name) ->
            val balance = sharedPrefs.getInt(key, 100)
            FriendInfo(key, name, balance)
        }
        _friends.postValue(list)
    }
}
