package com.example.trabalho.data

import com.example.trabalho.data.model.Message

object FakeDataSource {
    val initialMessages = listOf(
        Message(id = 1, text = "Olá, Estou com problemas respiratórios! \n O que recomendam?", isFromMe = true),
        Message(id = 2, text = "Olá, recomendamos o uso de Inaladores ou Nebulizadores.\n  Possui um ao seu dispor?", isFromMe = false)
    )
}