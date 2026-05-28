package com.example.trabalho.ui.screens

import androidx.compose.foundation.clickable // Para o clique nos pacotes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // IMPORTANTE: faltava-te esta importação para as cores!
import androidx.compose.ui.unit.dp
import com.example.trabalho.data.model.UserMode
import com.example.trabalho.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSimulator: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("OppNet Guard - Painel Principal") })
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Arrangement.spacedBy dá um espaço automático entre os elementos principais
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // O cartão que Escolhe o Modo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.currentMode == UserMode.SEED)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Estado Atual: ${uiState.currentMode.name}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.toggleUserMode() }) {
                        Text("Alternar para ${if (uiState.currentMode == UserMode.SEED) "HELPER" else "SEED"}")
                    }
                }
            }

            // dividir a interface dependendo do modo escolhido
            if (uiState.currentMode != UserMode.HELPER) {

                // --- INTERFACE MODO SEED ---
                Spacer(modifier = Modifier.weight(1f)) // Empurra o botão para baixo
                Button(
                    onClick = { onNavigateToSimulator(uiState.currentMode.name) },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Entrar no Simulador de Fluxo")
                }

            } else {

                // --- INTERFACE MODO HELPER ---

                // Cartão de informação
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Modo Escuta Ativo...", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "O teu dispositivo está pronto para aceitar pacotes de emergência de forma anónima e isolada (Store-and-Forward).",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // O contador de pacotes
                Text(
                    text = "Pacotes reencaminhados: ${uiState.pacotesReencaminhados}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "A receber pacotes próximos...",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // LazyColumn para mostrar os pacotes retidos (Unidade 3)
                // O weight(1f) garante que a lista ocupa o resto do ecrã disponível
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(uiState.storedPackages) { pkg ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                // O Modifier.clickable transforma o pacote num botão (Unidade 2)
                                .clickable { viewModel.togglePackageReveal(pkg.id) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Pacote Encaminhado #${pkg.id}", style = MaterialTheme.typography.titleSmall)

                                if (pkg.isRevealed) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tentativa de Leitura (Conteúdo Encriptado):",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Red
                                    )
                                    Text(
                                        text = pkg.encryptedPayload,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}