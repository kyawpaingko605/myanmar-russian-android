package com.myanmarrussian.models

data class GroupMessage(
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val isStaticSender: Boolean = false
)
