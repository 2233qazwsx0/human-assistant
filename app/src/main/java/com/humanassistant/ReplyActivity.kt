package com.humanassistant

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ReplyActivity : AppCompatActivity() {

    private lateinit var tvSender: TextView
    private lateinit var tvOriginalMessage: TextView
    private lateinit var etReply: EditText
    private lateinit var btnSend: Button

    private var requestId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reply)

        initViews()
        loadIntentData()
    }

    private fun initViews() {
        tvSender = findViewById(R.id.tvSender)
        tvOriginalMessage = findViewById(R.id.tvOriginalMessage)
        etReply = findViewById(R.id.etReply)
        btnSend = findViewById(R.id.btnSend)

        btnSend.setOnClickListener {
            sendReply()
        }
    }

    private fun loadIntentData() {
        requestId = intent.getStringExtra(ServerService.EXTRA_REQUEST_ID)
        val friendName = intent.getStringExtra("friend_name")
        val message = intent.getStringExtra("message")

        tvSender.text = friendName
        tvOriginalMessage.text = message
    }

    private fun sendReply() {
        val reply = etReply.text.toString()
        if (reply.isNotBlank() && requestId != null) {
            val serverService = ServerService.getInstance()
            serverService?.getHttpServer()?.completeRequest(requestId!!, reply)
            finish()
        }
    }
}
