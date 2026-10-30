package com.anish.mediscan

import android.content.Context
import com.google.gson.Gson

object GeminiMedicineCache {

    private const val PREF_NAME = "mediscan_gemini_medicine_cache"
    private val gson = Gson()

    fun get(context: Context, key: String): GeminiMedicineInfo? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(key, null) ?: return null
        return try {
            gson.fromJson(json, GeminiMedicineInfo::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun put(context: Context, key: String, info: GeminiMedicineInfo) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(info)
        prefs.edit().putString(key, json).apply()
    }
}
