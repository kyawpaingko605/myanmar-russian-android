package com.myanmarrussian.adapters

import android.content.Intent
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.myanmarrussian.databinding.ItemChatMessageBinding
import com.myanmarrussian.models.ChatMessage
import java.util.Locale

/**
 * ChatMessageAdapter - RecyclerView adapter for chat messages with intelligent bilingual TTS and direct voice data download redirect
 */
class ChatMessageAdapter(
    private val messages: MutableList<ChatMessage> = mutableListOf(),
    private val onPlayAudio: (ChatMessage) -> Unit = {} // Option အနေဖြင့် ချန်ထားပေးပါသည်
) : RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    inner class MessageViewHolder(
        private val binding: ItemChatMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            if (message.role == ChatMessage.MessageRole.USER) {
                // Show user message (right side)
                binding.userMessageContainer.visibility = View.VISIBLE
                binding.assistantMessageContainer.visibility = View.GONE
                binding.tvUserMessage.text = message.text
            } else {
                // Show assistant message (left side)
                binding.userMessageContainer.visibility = View.GONE
                binding.assistantMessageContainer.visibility = View.VISIBLE
                binding.tvAssistantMessage.text = message.text

                // AI ရဲ့ စာသားကို အသံထွက်စနစ်ဖြင့် စမတ်ကျကျ ဖတ်မည်
                binding.btnPlayAudio.setOnClickListener {
                    speakBilingualText(message.text, binding.root.context)
                    onPlayAudio(message)
                }
            }
        }
    }

    /**
     * ရုရှားစာနှင့် မြန်မာစာကို ခွဲခြားပြီး အသံထွက်ပီပြင်စွာ လှည့်လည်ဖတ်ပေးမည့် စနစ်
     */
    private fun speakBilingualText(text: String, context: android.content.Context) {
        if (!isTtsReady || tts == null) {
            Toast.makeText(context, "အသံထွက်စနစ် အဆင်သင့်မဖြစ်သေးပါ", Toast.LENGTH_SHORT).show()
            return
        }

        val hasRussian = text.any { it in '\u0400'..'\u04FF' }

        if (hasRussian) {
            // ရုရှားစာလုံးများ ပါဝင်နေပါက ရုရှား လေယူလေသိမ်းစစ်စစ်ဖြင့် အရင်ဖတ်ပေးမည်
            tts?.language = Locale("ru", "RU")
            tts?.setSpeechRate(0.85f)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "chat_tts")
        } else {
            // မြန်မာရှင်းလင်းချက် သီးသန့်ဖြစ်ပါက မြန်မာအသံစနစ်သို့ ပြောင်းလဲဖတ်ပေးမည်
            val result = tts?.setLanguage(Locale("my", "MM"))
            
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // 💡 မြန်မာ Voice Pack မရှိပါက ပေါ့ပ်အပ်ပြပြီး Google Voice Data ဒေါင်းလုဒ်စာမျက်နှာသို့ တိုက်ရိုက် ခုန်ကျော်ဖွင့်ပေးခြင်း
                Toast.makeText(context, "မြန်မာအသံဒေတာ မရှိသေးပါ။ ဒေါင်းလုဒ်စာမျက်နှာသို့ တိုက်ရိုက်ပို့ပေးနေပါသည်...", Toast.LENGTH_LONG).show()
                
                try {
                    // Correct implementation for direct intent launch
                    val intent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).apply {
                        setPackage("com.google.android.tts")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // အကယ်၍ Intent တိုက်ရိုက်သွားရခက်ပါက အထွေထွေ TTS Settings ကို ဖွင့်ပေးခြင်း
                    try {
                        val intent = Intent("com.android.settings.TTS_SETTINGS").apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (ex: Exception) {
                        val intent = Intent(Settings.ACTION_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                }
            } else {
                tts?.setSpeechRate(1.0f)
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "chat_tts")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        if (tts == null) {
            tts = TextToSpeech(parent.context.applicationContext, this)
        }

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

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
        }
    }

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun clearMessages() {
        messages.clear()
        notifyDataSetChanged()
    }

    // App ပိတ်သွားပါက Resource များ ပြန်ပိတ်ပေးခြင်း
    fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
    }
}
