package com.anish.mediscan

data class GeminiMedicineInfo(
    val found: Boolean = false,
    val medicineName: String? = null,
    val genericName: String? = null,
    val category: String? = null,
    val primaryUses: List<String>? = null,
    val generalUsageInfo: List<String>? = null,
    val precautionsWarnings: List<String>? = null,
    val sideEffects: List<String>? = null
)
