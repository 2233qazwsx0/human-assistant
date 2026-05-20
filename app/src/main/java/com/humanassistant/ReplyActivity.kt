package com.humanassistant

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ReplyActivity : AppCompatActivity() {

    companion object {
        private const val KEY_REQUEST_ID = "request_id"
        private const val KEY_REPLY_TEXT = "reply_text"
    }

    private lateinit var tvSender: TextView
    private lateinit var tvOriginalMessage: TextView
    private lateinit var etReply: EditText
    private lateinit var btnSend: Button

    private var requestId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reply)

        restoreInstanceState(savedInstanceState)
        initViews()
        loadIntentData()
        showKeyboard()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_REQUEST_ID, requestId)
        outState.putString(KEY_REPLY_TEXT, etReply.text.toString())
    }

    override fun onBackPressed() {
        checkForUnsavedChanges()
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            requestId = savedInstanceState.getString(KEY_REQUEST_ID)
        }
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
        
        if (requestId.isNullOrEmpty()) {
            Toast.makeText(this, R.string.msg_invalid_request, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val friendName = intent.getStringExtra("friend_name") ?: getString(R.string.msg_unknown_sender)
        val message = intent.getStringExtra("message") ?: getString(R.string.msg_no_message)

        tvSender.text = friendName
        tvOriginalMessage.text = message
    }

    private fun showKeyboard() {
        etReply.postDelayed({
            etReply.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etReply, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(etReply.windowToken, 0)
    }

    private fun checkForUnsavedChanges() {
        val reply = etReply.text.toString()
        if (reply.isNotEmpty()) {
            AlertDialog.Builder(this)
                .setTitle(R.string.msg_discard_reply)
                .setMessage(R.string.msg_discard_reply_message)
                .setPositiveButton(R.string.msg_discard_reply) { _, _ ->
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                .setNegativeButton(R.string.reply, null)
                .show()
        } else {
            setResult(Activity.RESULT_CANCELED)
            super.onBackPressed()
        }
    }

    private fun sendReply() {
        val reply = etReply.text.toString().trim()
        
        if (reply.isEmpty()) {
            etReply.error = getString(R.string.type_reply)
            return
        }

        if (requestId == null) {
            Toast.makeText(this, R.string.msg_invalid_request, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val serverService = ServerService.getInstance()
        if (serverService == null) {
            Toast.makeText(this, R.string.msg_service_not_available, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val httpServer = serverService.getHttpServer()
        if (httpServer == null) {
            Toast.makeText(this, R.string.msg_server_not_running, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        try {
            hideKeyboard()
            httpServer.completeRequest(requestId!!, reply)
            setResult(Activity.RESULT_OK)
            Toast.makeText(this, R.string.msg_send_success, Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.msg_error_send_failed, Toast.LENGTH_SHORT).show()
        }
    }
}
