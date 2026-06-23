package com.myanmarrussian.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myanmarrussian.databinding.ItemChatMessageBinding
import com.myanmarrussian.models.ChatMessage

/**
 * ChatMessageAdapter - RecyclerView adapter for chat messages
 * Equivalent to iOS ForEach with ChatBubble view
 */
class ChatMessageAdapter(
    private val messages: MutableList<ChatMessage> = mutableListOf(),
    private val onPlayAudio: (ChatMessage) -> Unit
) : RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {

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

                binding.btnPlayAudio.setOnClickListener {
                    onPlayAudio(message)
                }
            }
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
}
