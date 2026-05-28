package com.example.trabalho.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.trabalho.data.model.ChatUIState
import com.example.trabalho.data.model.Message
import com.example.trabalho.data.model.NetworkState
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatUIState: ChatUIState,
    networkState: NetworkState,
    userRole: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modo: $userRole") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar ao Painel Principal"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(16.dp)
        ) {
            // Lista de mensagens
            LazyColumn(
                modifier = Modifier.weight(1f),
                reverseLayout = true
            ) {
                items(chatUIState.messages.reversed()) { message ->
                    MessageBubble(message = message)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // NOVA GESTÃO DE ESTADOS DE REDE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                when (networkState) {
                    is NetworkState.Idle -> {
                        // Estado inicial: Mostra a caixa de texto
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = chatUIState.inputText,
                                onValueChange = onMessageChange,
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Mensagem...") },
                                shape = RoundedCornerShape(24.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = onSendMessage,
                                enabled = chatUIState.inputText.isNotBlank()
                            ) {
                                Text("Enviar")
                            }
                        }
                    }

                    is NetworkState.Loading -> {
                        // Loading: Mostrar a roda e o estado atual do mock-up (Encriptar, Procurar...)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = chatUIState.networkStatus.ifBlank { "A processar..." },
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    is NetworkState.Error -> {
                        // Error: Falha na OppNet (não encontrou caminho)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Erro de Rota",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Nenhuma rota encontrada para o destinatário.",
                                color = MaterialTheme.colorScheme.error, // Fica vermelho para chamar a atenção
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = onSendMessage) {
                                Text("Tentar Novamente")
                            }
                        }
                    }

                    is NetworkState.MaliciousNode -> {
                        // Nó Malicioso: O atacante descartou o pacote
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Ataque Malicioso",
                                tint = Color(0xFFD32F2F) // Vermelho escuro
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ALERTA: Pacote Comprometido por um Nó Malicioso! \n Envio da mensagem cancelado",
                                color = Color(0xFFD32F2F),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onSendMessage,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                            ) {
                                Text("Tentar Nova Rota")
                            }
                        }
                    }

                    is NetworkState.Success -> {
                        // A mensagem chegou ao destino
                        Text(
                            text = "✓ Mensagem Entregue",
                            color = Color(0xFF4CAF50),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val backgroundColor = if (message.isFromMe) Color(0xFFDCF8C6) else Color(0xFFF1F1F1)
    val alignment = if (message.isFromMe) Alignment.CenterEnd else Alignment.CenterStart

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start
    ) {
        // Balão de texto normal
        Surface(
            color = backgroundColor,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = Color.Black
            )
        }

        // implementação da Unidade 5: Mostrar os Avatares do Robohash com Coil
        if (message.routedBy.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Rota:",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Uma linha (Row) com as imagens dos robots que ajudaram a entregar
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                message.routedBy.forEach { helperName ->
                    AsyncImage(
                        // API que gera as imagens unicas dos robots
                        model = "https://robohash.org/$helperName?set=set1&bgset=bg1&size=150x150",
                        contentDescription = "Avatar do $helperName",
                        modifier = Modifier
                            .size(24.dp) // Tamanho pequeno para caber debaixo da mensagem
                            .clip(CircleShape) // Corta a imagem num círculo perfeito
                    )
                }
            }
        }
    }
}