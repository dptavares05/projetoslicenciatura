package com.example.trabalho.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // <-- Necessário para as corrotinas no ViewModel
import com.example.trabalho.data.model.HomeUiState
import com.example.trabalho.data.model.StoredPackage
import com.example.trabalho.data.model.UserMode
import kotlinx.coroutines.delay // <-- Para o tempo de espera
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch // <-- Para lançar a corrotina
import kotlin.random.Random // <-- Para gerar números aleatórios

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // O bloco 'init' corre automaticamente assim que o ViewModel é criado
    init {
        viewModelScope.launch {
            // Um loop infinito que corre em background
            while (true) {
                // Só atualizamos os pacotes se o utilizador estiver no modo HELPER
                if (_uiState.value.currentMode == UserMode.HELPER) {
                    _uiState.update { currentState ->
                        val novosPacotes = Random.nextInt(1, 6) // Gera um número entre 1 e 5
                        currentState.copy(
                            // NOTA: Precisas de garantir que tens a variável 'pacotesReencaminhados' no teu HomeUiState!
                            pacotesReencaminhados = currentState.pacotesReencaminhados + novosPacotes
                        )
                    }
                }
                delay(4000) // Espera 4 segundos ("de x em x tempo") antes do próximo ciclo
            }
        }
    }

    fun toggleUserMode() {
        _uiState.update { currentState ->
            val newMode = if (currentState.currentMode == UserMode.SEED) UserMode.HELPER else UserMode.SEED
            val newLog = "Modo alterado para: ${newMode.name}"
            currentState.copy(
                currentMode = newMode,
                simulatedLogs = currentState.simulatedLogs + newLog
            )
        }
    }

    fun updateMessage(text: String) {
        _uiState.update { it.copy(messageText = text) }
    }

    fun encryptMessage() {
        _uiState.update { currentState ->
            if (currentState.messageText.isNotBlank()) {
                currentState.copy(
                    isEncrypted = true,
                    simulatedLogs = currentState.simulatedLogs + "Mensagem encriptada com Chave Pública do Destino!"
                )
            } else currentState
        }
    }
    // Opcional: Função auxiliar para gerar lixo encriptado
    private fun generateEncryptedPayload(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        return (1..24).map { chars.random() }.joinToString("")
    }

    init {
        viewModelScope.launch {
            while (true) {
                if (_uiState.value.currentMode == UserMode.HELPER) {
                    _uiState.update { currentState ->
                        // Cria um novo pacote simulado
                        val newPackage = StoredPackage(
                            id = currentState.storedPackages.size + 1,
                            encryptedPayload = "E2EE_MSG:[${generateEncryptedPayload()}]"
                        )
                        currentState.copy(
                            // Adicionamos ao início da lista para o mais recente aparecer no topo
                            storedPackages = listOf(newPackage) + currentState.storedPackages
                        )
                    }
                }
                delay(5000) // Recebe um novo pacote a cada 5 segundos
            }
        }
    }

    // Função para quando o Helper tenta "cusbilhar" a mensagem
    fun togglePackageReveal(packageId: Int) {
        _uiState.update { state ->
            state.copy(
                storedPackages = state.storedPackages.map { pkg ->
                    if (pkg.id == packageId) pkg.copy(isRevealed = !pkg.isRevealed) else pkg
                }
            )
        }
    }
}