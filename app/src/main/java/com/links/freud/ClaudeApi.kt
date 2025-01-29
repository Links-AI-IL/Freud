package com.links.links

import android.util.Log
import com.links.freud.Message
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ClaudeApi(private val apiKey: String) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .build()


    fun sendMessage(userMessages: MutableList<Message>, callback: (String) -> Unit) {
        val url = "https://api.anthropic.com/v1/messages"

        val jsonBody = JSONObject()
        val messages = JSONArray()
        //val message = JSONObject()
        val content = JSONArray()
        val textContent = JSONObject()

        for (item in userMessages) {
            var message = JSONObject();
            message.put("role", item.role);
            message.put("content", item.content);
            messages.put(message);
        }

        jsonBody.put("model", "claude-3-5-sonnet-20241022")
        jsonBody.put("max_tokens", 1000)
        jsonBody.put("temperature", 0)
        jsonBody.put("messages", messages)

        println("MESSAGES: ${messages.toString()}")

        val requestBody = RequestBody.create(
                "application/json".toMediaType(), jsonBody.toString()
        )

        val request = Request.Builder()
                .url(url)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(requestBody)
                .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ClaudeApi", "Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("test16", "Response code: ${response}")
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    Log.d("test15", "Response body: $responseData")
                    val jsonResponse = JSONObject(responseData)
                    val messageText = jsonResponse.getJSONArray("content")
                            .getJSONObject(0)
                            .getString("text")
                    callback(messageText)
                } else {
                    Log.e("test14", "Error: ${response.message}")
                }
            }
        })
    }
}
