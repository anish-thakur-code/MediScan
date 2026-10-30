package com.anish.mediscan

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HistoryManager {

    private const val PREF_NAME = "mediscan_history_prefs"
    private const val KEY_SCANS = "scan_history_json"
    private const val MAX_ENTRIES = 20
    private val gson = Gson()

    fun isPrescriptionRequired(medicineName: String, category: String? = null): Boolean {
        val text = "${medicineName.lowercase(Locale.getDefault())} ${category.orEmpty().lowercase(Locale.getDefault())}"
        val prescriptionKeywords = listOf(
            "antibiotic", "prescription", "amoxicillin", "azithromycin",
            "steroid", "novamox", "mox", "anticonvulsant", "insulin"
        )
        return prescriptionKeywords.any { text.contains(it) }
    }

    fun saveScan(context: Context, medicineName: String, category: String? = null) {
        if (medicineName.isBlank()) return

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val currentDateStr = dateFormat.format(Date())

        val isRx = isPrescriptionRequired(medicineName, category)
        val typeBadgeText = if (isRx) "Prescription" else "OTC"

        val newEntry = ScanHistoryItem(
            id = System.currentTimeMillis().toString(),
            name = medicineName,
            type = typeBadgeText,
            dosage = if (!category.isNullOrBlank()) category else "Scanned Medicine",
            date = currentDateStr,
            isFavorite = false,
            requiresPrescription = isRx,
            iconRes = R.drawable.ic_pill
        )

        val currentList = getScans(context).toMutableList()

        // Remove previous entry with same name if exists to avoid duplicates
        currentList.removeAll { it.name.equals(medicineName, ignoreCase = true) }

        // Prepend new entry at the top
        currentList.add(0, newEntry)

        // Retain only the most recent 20 entries
        val trimmedList = currentList.take(MAX_ENTRIES)

        val json = gson.toJson(trimmedList)
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SCANS, json).apply()
    }

    fun deleteScan(context: Context, scanId: String) {
        val currentList = getScans(context).toMutableList()
        currentList.removeAll { it.id == scanId }
        val json = gson.toJson(currentList)
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SCANS, json).apply()
    }

    fun toggleFavorite(context: Context, scanId: String): Boolean {
        val currentList = getScans(context).toMutableList()
        val item = currentList.find { it.id == scanId } ?: return false
        item.isFavorite = !item.isFavorite

        val json = gson.toJson(currentList)
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SCANS, json).apply()

        return item.isFavorite
    }

    fun getScans(context: Context): List<ScanHistoryItem> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SCANS, null) ?: return emptyList()

        return try {
            val type = object : TypeToken<List<ScanHistoryItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
