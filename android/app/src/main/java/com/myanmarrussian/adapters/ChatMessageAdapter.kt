package com.myanmarrussian.adapters

import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.myanmarrussian.databinding.ItemChatMessageBinding
import com.myanmarrussian.models.ChatMessage
import java.net.URLEncoder

/**
 * ChatMessageAdapter - Node.js Backend TTS နှင့် ချိတ်ဆက်ထားသော ဗားရှင်း
 */
class ChatMessageAdapter(
    private val messages: MutableList<ChatMessage> = mutableListOf(),
    private val onPlayAudio: (ChatMessage) -> Unit = {}
) : RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null

    inner class MessageViewHolder(
        private val binding: ItemChatMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            if (message.role == ChatMessage.MessageRole.USER) {
                binding.userMessageContainer.visibility = View.VISIBLE
                binding.assistantMessageContainer.visibility = View.GONE
                binding.tvUserMessage.text = message.text
            } else {
                binding.userMessageContainer.visibility = View.GONE
                binding.assistantMessageContainer.visibility = View.VISIBLE
                binding.tvAssistantMessage.text = message.text

                // AI ရဲ့ စာသားကို ဖွင့်ရန် ကိုယ်ပိုင် Backend TTS စနစ်အား ခေါ်မည်
                binding.btnPlayAudio.setOnClickListener {
                    playBilingualAudio(message.text, binding.root.context)
                    onPlayAudio(message)
                }
            }
        }
    }

    /**
     * ရုရှားစာနှင့် မြန်မာစာကို ခွဲခြားပြီး ကိုယ်ပိုင် Backend နှင့် ချိတ်ဆက်အသံထွက်မည့်စနစ်
     */
    private fun playBilingualAudio(text: String, context: android.content.Context) {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            val hasRussian = text.any { it in '\u0400'..'\u04FF' }
            val encodedText = URLEncoder.encode(text, "UTF-8")
            
            // 💡 နေရာဒေသအလိုက် သို့မဟုတ် တင်ထားသည့် Node.js Backend Server URL ကို ထည့်ပေးရန်
            // (ဥပမာ Local စမ်းသပ်နေပါက "http://10.0.2.2:3000" သို့မဟုတ် Render URL ထည့်ပါ)
            val backendBaseUrl = "http://10.0.2.2:3000" 
            
            val audioUrl = if (hasRussian) {
                "https://translate.google.com/translate_tts?ie=UTF-8&tl=ru&client=tw-ob&q=$encodedText"
            } else {
                "$backendBaseUrl/api/tts?text=$encodedText"
            }

            Toast.makeText(context, "အသံဖိုင် ဖွင့်နေပါသည်...", Toast.LENGTH_SHORT).show()

            mediaPlayer = MediaPlayer().apply {
                // Android 9 အထက် ဗားရှင်းများတွင် Stream ပိတ်မသွားစေရန် Header ထည့်သွင်းခြင်း
                val headers = HashMap<String, String>()
                headers["User-Agent"] = "Mozilla/5.0"
                setDataSource(context, android.net.Uri.parse(audioUrl), headers)
                
                setAudioStreamType(android.media.AudioManager.STREAM_MUSIC)

                setOnPreparedListener { 
                    start() 
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayer", "Error playing audio: what=$what, extra=$extra")
                    Toast.makeText(context, "အသံဖွင့်ရန် အင်တာနက် သို့မဟုတ် Backend ချိတ်ဆက်မှု လိုအပ်ပါသည်", Toast.LENGTH_SHORT).show()
                    true
                }
                prepareAsync() 
            }

        } catch (e: Exception) {
            Log.e("AudioPlayer", "Exception: ${e.message}")
            Toast.makeText(context, "အသံထွက်စနစ် ချို့ယွင်းချက်ရှိနေပါသည်", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun clearMessages() {
        messages.clear()
        notifyDataSetChanged()
    }

    fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
