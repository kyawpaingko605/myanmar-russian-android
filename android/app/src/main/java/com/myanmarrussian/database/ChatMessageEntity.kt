package com.myanmarrussian.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "user" သို့မဟုတ် "assistant"
    val text: String, // ပြောလိုက်သော စာသားများ
    val timestamp: Long = System.currentTimeMillis() // အစီအစဉ်အတိုင်း ပြန်စီရန် အချိန်မှတ်တမ်း
)
