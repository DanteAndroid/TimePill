package com.example.myapplication.util

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
class Event<T>(val content: T) {
    private var hasBeenHandled = false

    val contentIfNotHandled: T?
        get() = if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }

    fun hasBeenHandled(): Boolean {
        return hasBeenHandled
    }

}