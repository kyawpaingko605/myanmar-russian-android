package com.myanmarrussian.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ChatDao {
    // စကားပြောသမိုင်းကြောင်းကို အချိန်အလိုက် အစဉ်လိုက် ပြန်ထုတ်ယူရန်
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    suspend fun getAllMessages(): List<ChatMessageEntity>

    // စကားလုံးအသစ်များကို database ထဲသို့ လှမ်းသိမ်းရန်
    @Insert
    suspend fun insertMessage(message: ChatMessageEntity)

    // စကားပြောသမိုင်းကြောင်း တစ်ခုလုံးကို ဖျက်ပစ်ရန် (လိုရီမယ်ရ သုံးနိုင်ရန်)
    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}
