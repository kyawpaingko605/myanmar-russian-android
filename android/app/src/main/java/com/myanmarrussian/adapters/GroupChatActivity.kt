package com.myanmarrussian.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myanmarrussian.R
import com.myanmarrussian.models.GroupMessage

class GroupChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupChatAdapter: GroupChatAdapter
    private lateinit var etMessageInput: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnBack: ImageButton

    // 💡 စမ်းသပ်ရန် ယာယီ User ID (အစစ်အမှန်သုံးလျှင် Firebase Auth ID ထည့်ရပါမည်)
    private val currentUserId = "user123" 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        // UI Component များကို ချိတ်ဆက်ခြင်း
        recyclerView = findViewById(R.id.rv_group_messages)
        etMessageInput = findViewById(R.id.et_group_message_input)
        btnSend = findViewById(R.id.btn_group_send)
        btnBack = findViewById(R.id.btn_back)

        // RecyclerView Setup
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // စာအသစ်တက်လာလျှင် အောက်ခြေသို့ အလိုအလျောက် ရောက်နေစေရန်
        }
        groupChatAdapter = GroupChatAdapter(currentUserId)
        recyclerView.adapter = groupChatAdapter

        // Back Button လုပ်ဆောင်ချက်
        btnBack.setOnClickListener {
            finish()
        }

        // Send Button လုပ်ဆောင်ချက်
        btnSend.setOnClickListener {
            val messageText = etMessageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            }
        }

        // 💡 ဥပမာ ဒေတာထည့်သွင်းပြသခြင်း (ဒေတာဘေ့စ်နှင့် ချိတ်ဆက်လျှင် ဤနေရာတွင် ရေးရပါမည်)
        loadDummyMessages()
    }

    private fun sendMessage(text: String) {
        val newMessage = GroupMessage(
            senderId = currentUserId,
            senderName = "ကျွန်တော်",
            text = text,
            timestamp = System.currentTimeMillis()
        )
        groupChatAdapter.addMessage(newMessage)
        etMessageInput.text.clear()
        recyclerView.smoothScrollToPosition(groupChatAdapter.itemCount - 1)
    }

    private fun loadDummyMessages() {
        val dummyList = listOf(
            GroupMessage("user456", "အောင်အောင်", "မင်္ဂလာပါ ရုရှားစာ လေ့လာနေသူများခင်ဗျာ", System.currentTimeMillis()),
            GroupMessage("user789", "သီရိ", "Привет! အားလုံးပဲ မင်္ဂလာပါရှင်", System.currentTimeMillis()),
            GroupMessage("user123", "ကျွန်တော်", "ဟုတ်ကဲ့ ဗဟုသုတတွေ အတူတူမျှဝေကြရအောင်", System.currentTimeMillis())
        )
        groupChatAdapter.setMessages(dummyList)
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
