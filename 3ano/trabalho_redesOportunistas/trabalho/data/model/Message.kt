package com.example.trabalho.data.model

data class Message(
    val id: Int,
    val text: String,
    val isFromMe: Boolean,
    val routedBy: List<String> = emptyList()
)