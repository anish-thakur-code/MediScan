package com.anish.mediscan

import com.google.gson.annotations.SerializedName

data class OpenFdaResponse(
    @SerializedName("results")
    val results: List<DrugLabelResult>? = null
)

data class DrugLabelResult(
    @SerializedName("purpose")
    val purpose: List<String>? = null,

    @SerializedName("indications_and_usage")
    val indicationsAndUsage: List<String>? = null,

    @SerializedName("dosage_and_administration")
    val dosageAndAdministration: List<String>? = null,

    @SerializedName("warnings")
    val warnings: List<String>? = null,

    @SerializedName("warnings_and_cautions")
    val warningsAndCautions: List<String>? = null,

    @SerializedName("adverse_reactions")
    val adverseReactions: List<String>? = null,

    @SerializedName("openfda")
    val openfda: OpenFdaData? = null
)

data class OpenFdaData(
    @SerializedName("brand_name")
    val brandName: List<String>? = null,

    @SerializedName("generic_name")
    val genericName: List<String>? = null,

    @SerializedName("manufacturer_name")
    val manufacturerName: List<String>? = null,

    @SerializedName("product_type")
    val productType: List<String>? = null
)
