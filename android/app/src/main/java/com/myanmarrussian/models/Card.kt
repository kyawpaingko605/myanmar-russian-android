package com.myanmarrussian.models

/**
 * Card model - Equivalent to iOS Card struct
 * Represents a flashcard with Myanmar and Russian vocabulary
 */
data class Card(
    val id: Int,
    val myanmar: String,
    val russian: String,
    val pronunciation: String,
    val category: String
)

/**
 * Default flashcard data - same as iOS FlashcardsView
 */
object CardData {
    val defaultCards = listOf(
        Card(1, "မင်္ဂလာပါ", "Привет", "Privet", "Greetings"),
        Card(2, "ကောင်းပါတယ်", "Спасибо", "Spasibo", "Greetings"),
        Card(3, "ကျေးဇူးတင်ပါတယ်", "Пожалуйста", "Pozhaluysta", "Greetings"),
        Card(4, "တစ်", "Один", "Odin", "Numbers"),
        Card(5, "နှစ်", "Два", "Dva", "Numbers"),
        Card(6, "သုံး", "Три", "Tri", "Numbers"),
        Card(7, "ကျွန်တော် ရုရှားဘာသာ သင်ချင်ပါတယ်", "Я хочу учить русский язык", "Ya khochu uchit russkiy yazyk", "Common"),
        Card(8, "ဒါကို ရုရှားလို ဘယ်လိုပြောလဲ?", "Как это сказать по-русски?", "Kak eto skazat po-russki?", "Common")
    )
}
