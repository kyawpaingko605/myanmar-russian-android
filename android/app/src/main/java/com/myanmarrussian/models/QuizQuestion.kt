package com.myanmarrussian.models

/**
 * QuizQuestion model - Equivalent to iOS QuizQuestion struct
 */
data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int
)

/**
 * Default quiz questions - same as iOS QuizView
 */
object QuizData {
    val defaultQuestions = listOf(
        QuizQuestion(
            id = 1,
            question = "မြန်မာ 'မင်္ဂလာပါ' ကို ရုရှားလို ဘယ်လိုပြောလဲ?",
            options = listOf("Привет", "Спасибо", "Пожалуйста", "До свидания"),
            correctAnswer = 0
        ),
        QuizQuestion(
            id = 2,
            question = "'Спасибо' ကို မြန်မာလို ဘယ်လိုပြောလဲ?",
            options = listOf("ကျေးဇူးတင်ပါတယ်", "မင်္ဂလာပါ", "ကောင်းပါတယ်", "ကျွန်တော်"),
            correctAnswer = 2
        ),
        QuizQuestion(
            id = 3,
            question = "'Один' ကို မြန်မာလို ဘယ်လိုပြောလဲ?",
            options = listOf("နှစ်", "သုံး", "တစ်", "လေး"),
            correctAnswer = 2
        ),
        QuizQuestion(
            id = 4,
            question = "မြန်မာ 'ကျေးဇူးတင်ပါတယ်' ကို ရုရှားလို ဘယ်လိုပြောလဲ?",
            options = listOf("Привет", "Спасибо", "Пожалуйста", "Два"),
            correctAnswer = 2
        ),
        QuizQuestion(
            id = 5,
            question = "'Два' ကို မြန်မာလို ဘယ်လိုပြောလဲ?",
            options = listOf("တစ်", "နှစ်", "သုံး", "လေး"),
            correctAnswer = 1
        )
    )
}
