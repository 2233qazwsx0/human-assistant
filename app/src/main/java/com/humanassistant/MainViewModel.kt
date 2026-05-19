package com.humanassistant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.humanassistant.data.AppDatabase

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val friends = database.friendDao().getAllFriends().asLiveData()
}
