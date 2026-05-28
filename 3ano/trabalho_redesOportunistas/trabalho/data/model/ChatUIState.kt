package com.example.trabalho.data.model

data class ChatUIState(
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
    val networkStatus: String = ""
)