package com.humanassistant

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.humanassistant.data.PendingRequest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var serverService: ServerService? = null
    private var isServiceBound = false
    private var isServerRunning = false
    
    private lateinit var tvServerStatus: TextView
    private lateinit var tvServerAddress: TextView
    private lateinit var btnToggleServer: Button
    private lateinit var rvPendingRequests: RecyclerView
    private lateinit var rvFriendBalance: RecyclerView
    
    private lateinit var pendingRequestAdapter: PendingRequestAdapter
    private lateinit var friendBalanceAdapter: FriendBalanceAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Notification permission required", Toast.LENGTH_SHORT).show()
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ServerService.LocalBinder
            serverService = binder.getService()
            isServiceBound = true
            observePendingRequests()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
            serverService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerViews()
        checkNotificationPermission()
        observeData()
    }

    override fun onStart() {
        super.onStart()
        // Try to bind to the service if it's already running
        val intent = Intent(this, ServerService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    private fun initViews() {
        tvServerStatus = findViewById(R.id.tvServerStatus)
        tvServerAddress = findViewById(R.id.tvServerAddress)
        btnToggleServer = findViewById(R.id.btnToggleServer)
        rvPendingRequests = findViewById(R.id.rvPendingRequests)
        rvFriendBalance = findViewById(R.id.rvFriendBalance)

        btnToggleServer.setOnClickListener {
            if (isServerRunning) {
                stopServer()
            } else {
                startServer()
            }
        }
    }

    private fun setupRecyclerViews() {
        pendingRequestAdapter = PendingRequestAdapter { request ->
            openReplyActivity(request)
        }
        rvPendingRequests.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = pendingRequestAdapter
        }

        friendBalanceAdapter = FriendBalanceAdapter()
        rvFriendBalance.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = friendBalanceAdapter
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun observeData() {
        viewModel.friends.observe(this) { friends ->
            friendBalanceAdapter.submitList(friends)
        }
    }

    private fun observePendingRequests() {
        serverService?.getHttpServer()?.let { server ->
            lifecycleScope.launch {
                server.pendingRequestsFlow.collect { requests ->
                    pendingRequestAdapter.submitList(requests)
                }
            }
        }
    }

    private fun startServer() {
        ServerService.startServer(this)
        isServerRunning = true
        updateUI()
    }

    private fun stopServer() {
        ServerService.stopServer(this)
        isServerRunning = false
        pendingRequestAdapter.submitList(emptyList())
        updateUI()
    }

    private fun updateUI() {
        if (isServerRunning) {
            tvServerStatus.text = getString(R.string.server_running)
            tvServerStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            btnToggleServer.text = getString(R.string.stop_server)
            tvServerAddress.text = "http://${getIpAddress()}:8080"
        } else {
            tvServerStatus.text = getString(R.string.server_stopped)
            tvServerStatus.setTextColor(getColor(android.R.color.holo_red_dark))
            btnToggleServer.text = getString(R.string.start_server)
            tvServerAddress.text = "-"
        }
    }

    private fun getIpAddress(): String {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress
        return String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xFF,
            ipAddress shr 8 and 0xFF,
            ipAddress shr 16 and 0xFF,
            ipAddress shr 24 and 0xFF
        )
    }

    private fun openReplyActivity(request: PendingRequest) {
        val intent = Intent(this, ReplyActivity::class.java).apply {
            putExtra(ServerService.EXTRA_REQUEST_ID, request.requestId)
            putExtra("api_key", request.apiKey)
            putExtra("friend_name", request.friendName)
            putExtra("message", request.message)
        }
        startActivity(intent)
    }
}
