package com.links.freud

import android.content.Context
import android.util.Log
import com.links.links.ClaudeApi

class ChatManager constructor(context: Context, prompt: String) {

    private val CLAUDE_KEY = "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";

    private val api: ClaudeApi = ClaudeApi(CLAUDE_KEY);

    private var PROMPT = prompt

    private val messages: MutableList<Message> = mutableListOf(Message("user", PROMPT))

    fun sendMessage(message: String, callback: Callback<String>) {
        messages.add(Message("user", message))

        try {
            api.sendMessage(messages) { apiResponse ->
                Log.d("MESSAGES: ", messages.toString())
                messages.add(Message("assistant", apiResponse))
                callback.onResponse(apiResponse)
            }
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }
}