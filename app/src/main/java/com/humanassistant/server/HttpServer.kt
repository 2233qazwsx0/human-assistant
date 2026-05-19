package com.humanassistant.server

import android.content.Context
import android.content.SharedPreferences
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.util.UUID

class HttpServer(
    private val context: Context,
    private val onNewRequest: (PendingRequest) -> Unit,
    private val onRequestTimeout: (String) -> Unit
) {
    private var server: NettyApplicationEngine? = null
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("FriendBalance", Context.MODE_PRIVATE)
    private val pendingRequests = ConcurrentHashMap<String, PendingRequest>()
    
    private val _pendingRequestsFlow = MutableStateFlow<List<PendingRequest>>(emptyList())
    val pendingRequestsFlow: StateFlow<List<PendingRequest>> = _pendingRequestsFlow.asStateFlow()

    private val validApiKeys = mapOf(
        "test-key" to "Test Friend",
        "friend1-key" to "Friend 1",
        "friend2-key" to "Friend 2"
    )

    fun start(port: Int = 8080) {
        server = embeddedServer(Netty, port = port, host = "0.0.0.0") {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }

            routing {
                post("/v1/chat/completions") {
                    handleChatCompletion(call)
                }
            }
        }.start(wait = false)
    }

    fun stop() {
        server?.stop(1000, 2000)
        pendingRequests.clear()
        _pendingRequestsFlow.value = emptyList()
    }

    fun completeRequest(requestId: String, reply: String) {
        pendingRequests.remove(requestId)?.let {
            it.deferred.complete(reply)
            updatePendingRequestsFlow()
        }
    }

    fun cancelRequest(requestId: String) {
        pendingRequests.remove(requestId)?.let {
            it.deferred.cancel("Request cancelled")
            updatePendingRequestsFlow()
        }
    }

    private fun updatePendingRequestsFlow() {
        _pendingRequestsFlow.value = pendingRequests.values.toList()
    }

    private suspend fun handleChatCompletion(call: ApplicationCall) {
        val authHeader = call.request.header("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                ErrorDetail("Missing or invalid Authorization header", "invalid_request_error", "missing_auth")
            ))
            return
        }

        val apiKey = authHeader.removePrefix("Bearer ")
        val friendName = validApiKeys[apiKey] ?: run {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                ErrorDetail("Invalid API key", "invalid_request_error", "invalid_api_key")
            ))
            return
        }

        val currentBalance = getFriendBalance(apiKey)
        if (currentBalance <= 0) {
            call.respond(HttpStatusCode.PaymentRequired, ErrorResponse(
                ErrorDetail("余额不足啦！快去充值（开玩笑的，其实是我不想理你了🤪）", "insufficient_balance", "balance_zero")
            ))
            return
        }

        val request = try {
            call.receive<ChatCompletionRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                ErrorDetail("Invalid request body", "invalid_request_error", "invalid_body")
            ))
            return
        }

        val userMessage = request.messages.lastOrNull { it.role == "user" }?.content ?: run {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                ErrorDetail("No user message found", "invalid_request_error", "no_user_message")
            ))
            return
        }

        val requestId = UUID.randomUUID().toString()
        val deferred = CompletableDeferred<String>()

        val pendingRequest = PendingRequest(requestId, apiKey, friendName, userMessage, deferred)
        pendingRequests[requestId] = pendingRequest
        updatePendingRequestsFlow()
        onNewRequest(pendingRequest)

        try {
            withTimeout(60_000L) {
                val reply = deferred.await()
                deductFriendBalance(apiKey)
                call.respond(
                    ChatCompletionResponse(
                        id = "chatcmpl-${requestId.take(8)}",
                        model = request.model,
                        choices = listOf(
                            ChatCompletionChoice(
                                message = ChatMessage("assistant", reply)
                            )
                        )
                    )
                )
            }
        } catch (e: TimeoutCancellationException) {
            pendingRequests.remove(requestId)
            updatePendingRequestsFlow()
            onRequestTimeout(requestId)
            call.respond(HttpStatusCode.RequestTimeout, ErrorResponse(
                ErrorDetail("主人太忙了，没时间回复你😅", "request_timeout", "timeout")
            ))
        }
    }

    private fun getFriendBalance(apiKey: String): Int {
        val defaultBalance = 100
        return sharedPrefs.getInt(apiKey, defaultBalance)
    }

    private fun deductFriendBalance(apiKey: String) {
        val current = getFriendBalance(apiKey)
        sharedPrefs.edit().putInt(apiKey, current - 1).apply()
    }
}

data class PendingRequest(
    val requestId: String,
    val apiKey: String,
    val friendName: String,
    val message: String,
    val deferred: CompletableDeferred<String>
)
