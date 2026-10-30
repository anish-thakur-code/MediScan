package com.anish.mediscan

data class ScanHistoryItem(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val type: String = "Medication",
    val dosage: String = "Scanned Medicine",
    val date: String,
    var isFavorite: Boolean = false,
    val requiresPrescription: Boolean = false,
    val iconRes: Int = R.drawable.ic_pill
)
