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
import com.google.firebase.database.*
import com.myanmarrussian.R
import com.myanmarrussian.models.GroupMessage

class GroupChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupChatAdapter: GroupChatAdapter
    private lateinit var etMessageInput: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnBack: ImageButton

    // 💬 Firebase Database Reference
    private lateinit var databaseReference: DatabaseReference

    // 💡 စမ်းသပ်ရန် ယာယီ User ID (အစစ်အမှန်သုံးလျှင် Firebase Auth ID ထည့်ရပါမည်)
    private val currentUserId = "user123" 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        // 1. Firebase Realtime Database အား အသစ်ရရှိထားသော Singapore URL ဖြင့် ချိတ်ဆက်ခြင်း
        val database = FirebaseDatabase.getInstance("https://myanmar-russian-learner-ca61b-default-rtdb.asia-southeast1.firebasedatabase.app/")
        databaseReference = database.getReference("group_chat")

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

        // Firebase ထဲမှ စာများကို Real-time ဖတ်ယူခြင်း စတင်ရန်
        listenForMessages()

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
    }

    // 💬 Firebase သို့ စာပို့ပေးမည့် မူလ Function ကို ပြင်ဆင်ခြင်း
    private fun sendMessage(text: String) {
        val messageId = databaseReference.push().key ?: return
        val newMessage = GroupMessage(
            senderId = currentUserId,
            senderName = "ကျွန်တော်",
            text = text,
            timestamp = System.currentTimeMillis()
        )

        databaseReference.child(messageId).setValue(newMessage)
            .addOnSuccessListener {
                etMessageInput.text.clear()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "စာပို့ရန် အဆင်မပြေပါ- ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 🔄 Firebase မှ ဒေတာများကို အချိန်နဲ့တပြေးညီ နားထောင်ပြီး Adapter ထဲ ထည့်ပေးသည့် Function
    private fun listenForMessages() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messageList = ArrayList<GroupMessage>()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(GroupMessage::class.java)
                    if (message != null) {
                        messageList.add(message)
                    }
                }
                // မူလ Adapter ၏ setMessages ခေါ်ယူခြင်း
                groupChatAdapter.setMessages(messageList)
                
                // စာအသစ်ရှိလျှင် အောက်ဆုံးသို့ ရွှေ့ပေးခြင်း
                if (messageList.isNotEmpty()) {
                    recyclerView.smoothScrollToPosition(groupChatAdapter.itemCount - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GroupChatActivity, "ဒေတာဖတ်မရပါ- ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
