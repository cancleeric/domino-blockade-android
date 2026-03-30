package com.cancleeric.dominoblockade.domain.model

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val emoji: String,
    val timestamp: Long = System.currentTimeMillis()
)

object QuickEmojis {
    val list = listOf("👍", "😢", "😮", "🎉", "😅", "🔥")
}
