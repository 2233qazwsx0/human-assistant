package com.humanassistant

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FriendBalanceAdapter : RecyclerView.Adapter<FriendBalanceAdapter.ViewHolder>() {

    private var items: List<FriendInfo> = emptyList()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun submitList(list: List<FriendInfo>) {
        mainHandler.post {
            items = list
            notifyDataSetChanged()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFriendName: TextView = view.findViewById(R.id.tvFriendName)
        val tvFriendBalance: TextView = view.findViewById(R.id.tvFriendBalance)

        fun bind(friend: FriendInfo) {
            tvFriendName.text = friend.name
            tvFriendBalance.text = "${friend.balance} tokens"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_balance, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.getOrNull(position) ?: return
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size
}
