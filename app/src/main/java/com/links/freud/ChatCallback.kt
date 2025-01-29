package com.links.freud

interface Callback<T> {
    fun onResponse(response: T)
    fun onFailure(e: Exception?)
}