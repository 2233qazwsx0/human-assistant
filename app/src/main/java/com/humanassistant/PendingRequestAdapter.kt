package com.humanassistant

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.humanassistant.server.PendingRequest

class PendingRequestAdapter(
    private val onItemClick: (PendingRequest) -> Unit
) : RecyclerView.Adapter<PendingRequestAdapter.ViewHolder>() {

    private var items: List<PendingRequest> = emptyList()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun submitList(list: List<PendingRequest>) {
        mainHandler.post {
            items = list
            notifyDataSetChanged()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRequestId: TextView = view.findViewById(R.id.tvRequestId)
        val tvRequestMessage: TextView = view.findViewById(R.id.tvRequestMessage)

        fun bind(request: PendingRequest) {
            tvRequestId.text = "Request ID: ${request.requestId.take(8)}..."
            tvRequestMessage.text = "${request.friendName}: ${request.message}"
            itemView.setOnClickListener { /* 点击事件由 adapter 处理 */ }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.getOrNull(position) ?: return
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
