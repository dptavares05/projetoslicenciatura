package com.example.trabalho.data.model

sealed interface NetworkState {
    data object Idle : NetworkState
    object Loading : NetworkState
    object Error : NetworkState
    object MaliciousNode : NetworkState
    object Success : NetworkState
}