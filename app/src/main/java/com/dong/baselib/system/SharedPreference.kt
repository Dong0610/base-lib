package com.dong.baselib.system

import android.content.Context
import android.content.SharedPreferences
interface OnPreferenceChangeListener {
    fun onPreferenceChanged(key: String)
}
class SharedPreference (context: Context){


    private val listeners = mutableListOf<OnPreferenceChangeListener>()
    private var sharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    private var lastKey=""
    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        }
    }

    fun registerListener(listener: OnPreferenceChangeListener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: OnPreferenceChangeListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners(key: String) {
        for (listener in listeners) {
            if(key!=""){
                listener.onPreferenceChanged(key)
            }
        }
    }

    fun edit(block: SharedPreferences.Editor.() -> Unit) {
        with(sharedPreferences!!.edit()) {
            block()
            apply()
            notifyListeners(lastKey)
        }
    }


    fun getBoolean(key: String, default: Boolean = false): Boolean {
        lastKey=key
        return sharedPreferences!!.getBoolean(key, default)
    }

    fun putBoolean(key: String, value: Boolean) {
        edit {
            lastKey=key
            putBoolean(key, value)
        }
    }

    fun getString(key: String, default: String=""): String {
        lastKey=key
        return sharedPreferences!!.getString(key, default)!!
    }

    fun putString(key: String, value: String) {
        edit {
            lastKey=key
            putString(key, value)
        }
    }

    fun getInt(key: String, default: Int = 0): Int {
        lastKey=key
        return sharedPreferences!!.getInt(key, default)
    }

    fun putInt(key: String, value: Int) {
        edit {
            lastKey=key
            putInt(key, value)
        }
    }

    fun getFloat(key: String, default: Float = 0.0f): Float {
        lastKey=key
        return sharedPreferences!!.getFloat(key, default)
    }
    fun getDouble(key: String, default: Double = 0.0): Double {
        lastKey=key
        val longValue = sharedPreferences!!.getLong(key, java.lang.Double.doubleToRawLongBits(default))
        return java.lang.Double.longBitsToDouble(longValue)
    }

    fun putDouble(key: String, value: Double) {
        edit {
            lastKey=key
            putLong(key, java.lang.Double.doubleToRawLongBits(value))
        }
    }
    fun putFloat(key: String, value: Float) {
        edit {
            lastKey=key
            putFloat(key, value)
        }
    }

    fun getLong(key: String, default: Long = 0L): Long {
        lastKey=key
        return sharedPreferences!!.getLong(key, default)
    }

    fun putLong(key: String, value: Long) {
        edit {
            lastKey=key
            putLong(key, value)
        }
    }

    fun clear() {
        edit {
            lastKey=""
            clear()
        }
    }

}






















