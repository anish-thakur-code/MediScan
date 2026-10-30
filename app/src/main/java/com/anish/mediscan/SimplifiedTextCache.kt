package com.anish.mediscan

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SimplifiedTextCache {

    private const val PREF_NAME = "mediscan_simplified_text_cache"
    private val gson = Gson()

    fun get(context: Context, key: String): List<String>? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(key, null) ?: return null
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    fun put(context: Context, key: String, bullets: List<String>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(bullets)
        prefs.edit().putString(key, json).apply()
    }
}
