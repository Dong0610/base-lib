package com.dong.baselib.string

import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.Serializable

fun <A> String.fromJson(type: Class<A>): A? {
    return try {
        Gson().fromJson(this, type)
    } catch (e: JsonSyntaxException) {
        Log.e("JSON_PARSE_ERROR", "Failed to parse JSON: $this", e)
        null
    }
}
fun <A> A.toJson(): String {
    return Gson().toJson(this)
}

@Suppress("DEPRECATION")
inline fun <reified T : Serializable> Bundle.getSerializable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) getSerializable(key, T::class.java)
    else getSerializable(key) as? T
}