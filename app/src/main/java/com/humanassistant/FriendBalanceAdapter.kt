package com.humanassistant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.humanassistant.data.Friend

class FriendBalanceAdapter : RecyclerView.Adapter<FriendBalanceAdapter.ViewHolder>() {

    private var items: List<Friend> = emptyList()

    fun submitList(list: List<Friend>) {
        items = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFriendName: TextView = view.findViewById(R.id.tvFriendName)
        val tvFriendBalance: TextView = view.findViewById(R.id.tvFriendBalance)

        fun bind(friend: Friend) {
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
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
