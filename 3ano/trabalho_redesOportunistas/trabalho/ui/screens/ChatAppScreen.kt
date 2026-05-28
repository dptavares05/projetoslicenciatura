package com.example.trabalho.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

//Definir os nomes das rotas para a navegação
enum class ChatRoutes {
    Start,
    Chat
}

// Enum para os papéis do utilizador
enum class UserRole {
    SEED,
    HELPER
}

//O Ecrã Inicial onde o utilizador escolhe o papel (Seed ou Helper)
@Composable
fun StartScreen(
    onRoleSelected: (UserRole) -> Unit, // Função lambda que avisa quando um papel foi escolhido
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Rede Oportunista\nMock-up",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Escolha o seu papel na rede para continuar:",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botão para entrar como SEED
        Button(
            onClick = { onRoleSelected(UserRole.SEED) },
            modifier = Modifier.width(200.dp)
        ) {
            Text(text = "Entrar como Seed")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botão para entrar como HELPER
        Button(
            onClick = { onRoleSelected(UserRole.HELPER) },
            modifier = Modifier.width(200.dp)
        ) {
            Text(text = "Entrar como Helper")
        }
    }
}