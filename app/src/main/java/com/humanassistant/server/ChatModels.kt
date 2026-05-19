package com.humanassistant.server

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean? = null
)

@Serializable
data class ChatCompletionChoice(
    val index: Int = 0,
    val message: ChatMessage,
    val finish_reason: String = "stop"
)

@Serializable
data class ChatCompletionResponse(
    val id: String,
    @SerialName("object")
    val objectType: String = "chat.completion",
    val created: Long = System.currentTimeMillis() / 1000,
    val model: String,
    val choices: List<ChatCompletionChoice>
)

@Serializable
data class ErrorResponse(
    val error: ErrorDetail
)

@Serializable
data class ErrorDetail(
    val message: String,
    val type: String,
    val code: String
)
