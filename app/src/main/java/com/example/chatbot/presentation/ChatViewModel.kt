package com.example.chatbot.presentation

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbot.data.Chat
import com.example.chatbot.data.ChatData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel: ViewModel() {
    private val _state = MutableStateFlow(ChatState())
    val state = _state.asStateFlow()

    fun onEvent(event: ChatUiEvent){
        when(event) {
            is ChatUiEvent.SendPrompt -> {
                if(event.prompt.isNotEmpty()){
                    addPrompt(event.prompt, event.bitmap)

                    if(event.bitmap != null) {
                        getResponseWithImage(event.prompt, event.bitmap)
                    } else {
                        getResponse(event.prompt)
                    }
                }
            }

            is ChatUiEvent.UpdatePrompt ->{
                _state.update {
                    it.copy(
                        prompt = event.newPrompt
                    )
                }
            }
        }
    }


    private fun addPrompt(prompt: String, bitmap: Bitmap?){
        _state.update {
            it.copy(
                chatList = it.chatList.toMutableList().apply { //toMutableList makes the mutableList editable so you can add changes
                    add(0, Chat(prompt, bitmap, true))
                },
                prompt = "",//this sets the field to empty when prompt is sent
                bitmap = null
            )
        }
    }

    private fun getResponse(prompt: String){
        viewModelScope.launch {
            val chat = ChatData.getResponse(prompt)
            _state.update {
                it.copy(
                    chatList = it.chatList.toMutableList().apply {
                        add(0, chat)
                    }
                )
            }
        }
    }

    private fun getResponseWithImage(prompt: String, bitmap: Bitmap){
        viewModelScope.launch {
            val chat = ChatData.getResponse(prompt, bitmap)
            _state.update {
                it.copy(
                    chatList = it.chatList.toMutableList().apply {
                        add(0, chat)
                    }
                )
            }
        }
    }

}