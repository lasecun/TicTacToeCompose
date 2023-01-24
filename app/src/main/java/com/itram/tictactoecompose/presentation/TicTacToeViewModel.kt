package com.itram.tictactoecompose.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itram.tictactoecompose.data.GameState
import com.itram.tictactoecompose.data.MakeTurn
import com.itram.tictactoecompose.data.RealTimeMessagingClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.ConnectException
import javax.inject.Inject

@HiltViewModel
class TicTacToeViewModel @Inject constructor(
    private val client: RealTimeMessagingClient,
) : ViewModel() {

    val state = client
        .getGameStateStream()
        .onStart { _isConnecting.value = true }
        .onEach { _isConnecting.value = false }
        .catch { t -> _showConnectionError.value = t is ConnectException }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GameState())

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting = _isConnecting.asStateFlow()

    private val _showConnectionError = MutableStateFlow(false)
    val showConnectionError = _showConnectionError.asStateFlow()

    fun finishTurn(x: Int, y: Int) {
        if (state.value.field[y][x] != null || state.value.winningPlayer != null) {
            return
        }
        Log.d("Position", x.toString())
        Log.d("Position", y.toString())
        viewModelScope.launch {
            client.sendAction(MakeTurn(x, y))
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            client.close()
        }
    }
}