package com.example.trabalho.data.model

enum class UserMode { SEED, HELPER }

data class StoredPackage(
    val id: Int,
    val encryptedPayload: String,
    val isRevealed: Boolean = false
)

data class HomeUiState(
    val currentMode: UserMode = UserMode.SEED,
    val messageText: String = "",
    val isEncrypted: Boolean = false,
    val simulatedLogs: List<String> = emptyList(),
    val storedPackages: List<StoredPackage> = emptyList(),
    val pacotesReencaminhados: Int = 0
)
