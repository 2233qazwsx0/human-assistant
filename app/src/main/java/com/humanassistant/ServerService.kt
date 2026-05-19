package com.humanassistant

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.humanassistant.server.PendingRequest
import com.humanassistant.server.HttpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class ServerService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var httpServer: HttpServer? = null
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    
    private val binder = LocalBinder()
    
    inner class LocalBinder : Binder() {
        fun getService(): ServerService = this@ServerService
    }
    
    companion object {
        const val ACTION_START_SERVER = "com.humanassistant.START_SERVER"
        const val ACTION_STOP_SERVER = "com.humanassistant.STOP_SERVER"
        const val EXTRA_REQUEST_ID = "request_id"
        
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val CHANNEL_ID_FOREGROUND = "foreground_channel"
        private const val CHANNEL_ID_MESSAGE = "message_channel"
        
        private var requestIdCounter = 1000
        
        @Volatile
        private var instance: ServerService? = null
        
        fun getInstance(): ServerService? = instance
        
        fun startServer(context: Context) {
            val intent = Intent(context, ServerService::class.java).apply {
                action = ACTION_START_SERVER
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopServer(context: Context) {
            val intent = Intent(context, ServerService::class.java).apply {
                action = ACTION_STOP_SERVER
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }
    
    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVER -> {
                startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundNotification())
                startHttpServer()
            }
            ACTION_STOP_SERVER -> {
                stopHttpServer()
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        stopHttpServer()
        serviceScope.cancel()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val foregroundChannel = NotificationChannel(
                CHANNEL_ID_FOREGROUND,
                "Server Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground service for running HTTP server"
            }
            
            val messageChannel = NotificationChannel(
                CHANNEL_ID_MESSAGE,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_desc)
            }
            
            notificationManager.createNotificationChannels(listOf(foregroundChannel, messageChannel))
        }
    }

    private fun createForegroundNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID_FOREGROUND)
            .setContentTitle("Human Assistant Server")
            .setContentText("Server is running on port 8080")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun startHttpServer() {
        httpServer = HttpServer(
            context = this,
            onNewRequest = { request ->
                showMessageNotification(request)
            },
            onRequestTimeout = { requestId ->
                notificationManager.cancel(requestIdCounter++)
            }
        )
        httpServer?.start(8080)
    }

    private fun stopHttpServer() {
        httpServer?.stop()
        httpServer = null
    }

    private fun showMessageNotification(request: PendingRequest) {
        val replyIntent = Intent(this, ReplyActivity::class.java).apply {
            putExtra(EXTRA_REQUEST_ID, request.requestId)
            putExtra("api_key", request.apiKey)
            putExtra("friend_name", request.friendName)
            putExtra("message", request.message)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestIdCounter,
            replyIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_MESSAGE)
            .setContentTitle(getString(R.string.friend_reminder))
            .setContentText("${request.friendName}: ${request.message}")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(requestIdCounter++, notification)
    }

    fun getHttpServer(): HttpServer? = httpServer
}
