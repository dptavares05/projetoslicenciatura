package com.example.trabalho.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trabalho.data.FakeDataSource
import com.example.trabalho.data.model.ChatUIState
import com.example.trabalho.data.model.Message
import com.example.trabalho.data.model.NetworkState // Importação da nossa nova interface
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random // Para simular o erro

class ChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUIState(messages = FakeDataSource.initialMessages))
    val uiState: StateFlow<ChatUIState> = _uiState.asStateFlow()

    // O estado da rede gerido separadamente (como ensinado na Unidade 5)
    private val _networkState = MutableStateFlow<NetworkState>(NetworkState.Idle)
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    fun updateInputText(newText: String) {
        _uiState.update { it.copy(inputText = newText) }
    }

    fun sendMessage() {
        val currentText = _uiState.value.inputText
        if (currentText.isBlank()) return

        // Passamos o ecrã para o modo "A carregar"
        _networkState.value = NetworkState.Loading

        viewModelScope.launch {
            try {
                // Passo 1: Encriptar
                _uiState.update { it.copy(networkStatus = "A encriptar mensagem...") }
                delay(1500)

                // Passo 2: Encriptada
                _uiState.update { it.copy(networkStatus = "Mensagem encriptada!") }
                delay(1000)

                // Passo 3: Procurar Helpers
                _uiState.update { it.copy(networkStatus = "A procurar helpers na rede...") }
                delay(1500)

                // SIMULAÇÃO DE AMEAÇAS COM PROBABILIDADE
                val chance = kotlin.random.Random.nextInt(100)// 100% total
                if (chance < 20) {//20% de chance de erro ao encontrar helpers
                    // 1. Falha de rede normal: Lançamos exceção para cair no CATCH
                    throw Exception("Sem rota encontrada")
                } else if (chance < 40) {//+ outros 20% de chance de ser alterado por nó malicioso
                    // 2. Nó Malicioso: Mudamos o estado e saímos silenciosamente (SEM EXCEÇÕES!)
                    _networkState.value = NetworkState.MaliciousNode
                    return@launch // Pára a corrotina imediatamente e não deixa avançar para o Passo 4
                }

                // Passo 4: Rota encontrada com número aleatório (2 a 6 helpers para caberem bem no ecrã)
                val numHelpers = kotlin.random.Random.nextInt(2, 7)

                // Gerar nomes únicos com numeros diferentes
                val helperNames = List(numHelpers) { "helper_${kotlin.random.Random.nextInt(1, 100)}" }

                _uiState.update { it.copy(networkStatus = "Rota encontrada! A passar por $numHelpers helpers...") }
                delay(2500)

                // Passo 5: Prepara a mensagem final e anexa os helpers
                val newMessage = Message(
                    id = _uiState.value.messages.size + 1,
                    text = currentText,
                    isFromMe = true,
                    routedBy = helperNames
                )

                // Atualiza a lista e limpa o texto escrito
                _uiState.update {
                    it.copy(
                        messages = it.messages + newMessage,
                        inputText = "",
                        networkStatus = ""
                    )
                }

                // Muda a interface para mostrar o ✓ Verde
                _networkState.value = NetworkState.Success
                delay(2000)

                // Volta ao estado inicial (caixa de texto aberta)
                _networkState.value = NetworkState.Idle

            } catch (e: Exception) {
                // Cai aqui se a rede "falhar" ao procurar helpers
                _networkState.value = NetworkState.Error
            }
        }
    }
}