package com.example.app_my_university.core.logging

import android.util.Log

/**
 * Единая точка логирования клиента. Не логировать пароли и полные токены.
 */
object AppLogger {
    private const val PREFIX = "MuUniversity"

    fun screen(name: String) {
        i("Screen", "open=$name")
    }

    fun nav(from: String, to: String) {
        i("Nav", "$from -> $to")
    }

    fun userAction(tag: String, detail: String) {
        i("Action", "$tag: $detail")
    }

    fun i(tag: String, message: String) {
        Log.i("$PREFIX:$tag", message)
    }

    fun w(tag: String, message: String) {
        Log.w("$PREFIX:$tag", message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) Log.e("$PREFIX:$tag", message, throwable)
        else Log.e("$PREFIX:$tag", message)
    }
}
