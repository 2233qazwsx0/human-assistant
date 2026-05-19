package com.humanassistant.data

import kotlinx.coroutines.CompletableDeferred

data class PendingRequest(
    val requestId: String,
    val apiKey: String,
    val friendName: String,
    val message: String,
    val deferred: CompletableDeferred<String>
)
