package com.myanmarrussian.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myanmarrussian.R
import com.myanmarrussian.models.GroupMessage

class GroupChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupChatAdapter: GroupChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        // XML ထဲက ID အမှန်ဖြစ်သော rv_group_messages သို့ ပြင်ဆင်ထားသည်
        recyclerView = findViewById(R.id.rv_group_messages)
        recyclerView.layoutManager = LinearLayoutManager(this)

        groupChatAdapter = GroupChatAdapter(currentUserId = "user123")
        recyclerView.adapter = groupChatAdapter
    }
}

class GroupChatAdapter(
    private val currentUserId: String
) : RecyclerView.Adapter<GroupChatAdapter.GroupMessageViewHolder>() {

    private val messages = mutableListOf<GroupMessage>()

    fun setMessages(newMessages: List<GroupMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    fun addMessage(message: GroupMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_message, parent, false)
        return GroupMessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupMessageViewHolder, position: Int) {
        holder.bind(messages[position], currentUserId)
    }

    override fun getItemCount(): Int = messages.size

    class GroupMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val otherMessageContainer: LinearLayout = itemView.findViewById(R.id.other_message_container)
        private val myMessageContainer: LinearLayout = itemView.findViewById(R.id.my_message_container)
        private val tvSenderName: TextView = itemView.findViewById(R.id.tv_sender_name)
        private val tvOtherMessage: TextView = itemView.findViewById(R.id.tv_other_message)
        private val tvMyMessage: TextView = itemView.findViewById(R.id.tv_my_message)

        fun bind(message: GroupMessage, currentUserId: String) {
            if (message.senderId == currentUserId || message.isStaticSender) {
                myMessageContainer.visibility = View.VISIBLE
                otherMessageContainer.visibility = View.GONE
                tvMyMessage.text = message.text
            } else {
                otherMessageContainer.visibility = View.VISIBLE
                myMessageContainer.visibility = View.GONE
                tvSenderName.text = message.senderName
                tvOtherMessage.text = message.text
            }
        }
    }
}
