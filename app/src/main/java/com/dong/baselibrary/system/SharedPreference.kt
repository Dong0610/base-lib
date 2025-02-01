package com.dong.baselibrary.system

import android.content.Context
import android.content.SharedPreferences

class SharedPreference (context: Context){

    private val sharedPreferences = context.getSharedPreferences("SHEQrScanner", Context.MODE_PRIVATE)


    fun edit(block: SharedPreferences.Editor.() -> Unit) {
        with(sharedPreferences!!.edit()) {
            block()
            apply()
        }
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return sharedPreferences!!.getBoolean(key, default)
    }

    fun putBoolean(key: String, value: Boolean) {
        edit {
            putBoolean(key, value)
        }
    }

    fun getString(key: String, default: String=""): String {
        return sharedPreferences!!.getString(key, default)!!
    }

    fun putString(key: String, value: String) {
        edit {
            putString(key, value)
        }
    }

    fun getInt(key: String, default: Int = 0): Int {
        return sharedPreferences!!.getInt(key, default)
    }

    fun putInt(key: String, value: Int) {
        edit {
            putInt(key, value)
        }
    }

    fun getFloat(key: String, default: Float = 0.0f): Float {
        return sharedPreferences!!.getFloat(key, default)
    }
    fun getDouble(key: String, default: Double = 0.0): Double {
        val longValue = sharedPreferences!!.getLong(key, java.lang.Double.doubleToRawLongBits(default))
        return java.lang.Double.longBitsToDouble(longValue)
    }

    fun putDouble(key: String, value: Double) {
        edit {
            putLong(key, java.lang.Double.doubleToRawLongBits(value))
        }
    }
    fun putFloat(key: String, value: Float) {
        edit {
            putFloat(key, value)
        }
    }

    fun getLong(key: String, default: Long = 0L): Long {
        return sharedPreferences!!.getLong(key, default)
    }

    fun putLong(key: String, value: Long) {
        edit {
            putLong(key, value)
        }
    }

    fun clear() {
        edit {
            clear()
        }
    }

}



