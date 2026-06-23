package com.myanmarrussian.models

/**
 * ChatMessage model - Equivalent to iOS ChatMessage struct
 */
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String
) {
    enum class MessageRole(val value: String) {
        USER("user"),
        ASSISTANT("assistant")
    }
}

/**
 * Language mode - Equivalent to iOS LangMode enum
 */
enum class LangMode(val value: String, val label: String) {
    MYANMAR("myanmar", "မြန်မာ"),
    RUSSIAN("russian", "ရုရှား")
}

/**
 * Tutor mode - Equivalent to iOS TutorMode enum
 */
enum class TutorMode(val value: String, val label: String) {
    CONVERSATION("conversation", "စကားပြော"),
    PRONUNCIATION("pronunciation", "အသံထွက်"),
    GRAMMAR("grammar", "သုံးစွဲမှု"),
    VOCABULARY("vocabulary", "စကားလုံး")
}
