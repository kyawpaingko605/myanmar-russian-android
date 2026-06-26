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
 * ChatMessageAdapter - Cross-device stable version using Network-based TTS for 100% Myanmar Voice support
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

                // AI ရဲ့ စာသားကို ဖုန်းတိုင်းမှာ အသံထွက်နိုင်ရန် Network TTS ဖြင့် ဖွင့်မည်
                binding.btnPlayAudio.setOnClickListener {
                    playBilingualAudio(message.text, binding.root.context)
                    onPlayAudio(message)
                }
            }
        }
    }

    /**
     * ရုရှားစာနှင့် မြန်မာစာကို API သုံး၍ ဖုန်းတိုင်းတွင် အသံထွက်စေမည့် စနစ်
     */
    private fun playBilingualAudio(text: String, context: android.content.Context) {
        try {
            // လက်ရှိ ဖွင့်နေတဲ့ အသံရှိရင် အရင်ရပ်ပေးမည်
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            val hasRussian = text.any { it in '\u0400'..'\u04FF' }
            val encodedText = URLEncoder.encode(text, "UTF-8")
            
            // 💡 ဖုန်းထဲက TTS အစား အွန်လိုင်း API URL တစ်ခုခုကို ပြောင်းသုံးခြင်း (ဥပမာ Google TTS Public API)
            val audioUrl = if (hasRussian) {
                "https://translate.google.com/translate_tts?ie=UTF-8&tl=ru&client=tw-ob&q=$encodedText"
            } else {
                "https://translate.google.com/translate_tts?ie=UTF-8&tl=my&client=tw-ob&q=$encodedText"
            }

            Toast.makeText(context, "အသံဖိုင် ဖွင့်နေပါသည်...", Toast.LENGTH_SHORT).show()

            // MediaPlayer ဖြင့် အသံဖိုင်လှမ်းဖွင့်ခြင်း
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                setOnPreparedListener { 
                    start() 
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayer", "Error playing audio: what=$what, extra=$extra")
                    Toast.makeText(context, "အသံဖွင့်ရန် အင်တာနက် လိုအပ်ပါသည်", Toast.LENGTH_SHORT).show()
                    true
                }
                prepareAsync() // Network သုံးထားသဖြင့် နောက်ကွယ်မှ အလုပ်လုပ်စေရန် Async သုံးရမည်
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

    // App ပိတ်သွားပါက Resource များကို ပြန်သိမ်းဆည်းခြင်း
    fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
