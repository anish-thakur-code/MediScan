package com.anish.mediscan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Locale

sealed class ResultScreenState {
    object Loading : ResultScreenState()
    data class Found(val info: GeminiMedicineInfo) : ResultScreenState()
    data class NotFound(val message: String = "Medicine not recognized. Please check spelling, try the generic name, or scan again.") : ResultScreenState()
    data class Error(val message: String) : ResultScreenState()
}

class ResultActivity : AppCompatActivity() {

    private var isBookmarked = false
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_result)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val handleBack = View.OnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        findViewById<View>(R.id.btnBack)?.setOnClickListener(handleBack)

        findViewById<View>(R.id.btnRetryScan)?.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.btnSearchManually)?.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        val rawExtractedName = intent.getStringExtra(ScanActivity.EXTRA_RECOGNIZED_TEXT)
        val cardRecognizedText = findViewById<View>(R.id.cardRecognizedText)
        val tvExtractedText = findViewById<TextView>(R.id.tvExtractedText)

        if (!rawExtractedName.isNullOrBlank()) {
            val cleanedName = cleanAndCorrectTypo(rawExtractedName)
            cardRecognizedText?.visibility = View.VISIBLE
            tvExtractedText?.text = cleanedName

            HistoryManager.saveScan(this, cleanedName)
            fetchGeminiMedicineData(cleanedName)
        } else if (intent.hasExtra(ScanActivity.EXTRA_RECOGNIZED_TEXT)) {
            cardRecognizedText?.visibility = View.VISIBLE
            tvExtractedText?.text = getString(R.string.no_text_detected)
            updateUiState(ResultScreenState.NotFound("No text detected. Please try scanning again."))
        }

        val btnBookmarkTop = findViewById<ImageView>(R.id.btnBookmarkTop)
        val btnFavCard = findViewById<ImageView>(R.id.btnFavCard)

        val toggleBookmark = View.OnClickListener {
            isBookmarked = !isBookmarked
            val drawableRes = if (isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark
            btnBookmarkTop?.setImageResource(drawableRes)
            btnFavCard?.setImageResource(drawableRes)
        }
        btnBookmarkTop?.setOnClickListener(toggleBookmark)
        btnFavCard?.setOnClickListener(toggleBookmark)

        setupExpandableSection(R.id.headerUses, R.id.contentUses, R.id.arrowUses)
        setupExpandableSection(R.id.headerDosage, R.id.contentDosage, R.id.arrowDosage)
        setupExpandableSection(R.id.headerPrecautions, R.id.contentPrecautions, R.id.arrowPrecautions)
        setupExpandableSection(R.id.headerEffects, R.id.contentEffects, R.id.arrowEffects)

        findViewById<View>(R.id.btnScanAnother)?.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.btnAddCabinet)?.setOnClickListener {
            finish()
        }
    }

    private fun cleanAndCorrectTypo(input: String): String {
        val trimmed = input.trim()
        val typoMap = mapOf(
            "paracitamol" to "Paracetamol",
            "paracetimol" to "Paracetamol",
            "paracitamall" to "Paracetamol",
            "paracetmol" to "Paracetamol",
            "crocen" to "Crocin",
            "dolo650" to "Dolo 650",
            "combyflam" to "Combiflam",
            "cetrissine" to "Cetirizine"
        )
        val lower = trimmed.lowercase(Locale.getDefault())
        return typoMap[lower] ?: trimmed
    }

    private fun getLocalFallback(medicineName: String): GeminiMedicineInfo? {
        val key = medicineName.trim().lowercase(Locale.getDefault())
        return when {
            key.contains("paracetamol") || key.contains("dolo") || key.contains("crocin") -> GeminiMedicineInfo(
                found = true,
                medicineName = "Paracetamol",
                genericName = "Acetaminophen",
                category = "Pain Reliever & Fever Reducer",
                primaryUses = listOf(
                    "Relieves mild to moderate pain",
                    "Reduces fever",
                    "Eases headaches and body ache"
                ),
                generalUsageInfo = listOf(
                    "Taken orally as tablets",
                    "Usually taken after food",
                    "Consult a doctor for exact amount"
                ),
                precautionsWarnings = listOf(
                    "Avoid alcohol while taking this medicine",
                    "Consult a doctor if you have liver problems",
                    "Do not exceed the recommended daily limit"
                ),
                sideEffects = listOf(
                    "Rare at normal use",
                    "May cause nausea in some people",
                    "Overdose can harm the liver"
                )
            )
            key.contains("ibuprofen") || key.contains("brufen") || key.contains("combiflam") -> GeminiMedicineInfo(
                found = true,
                medicineName = "Ibuprofen",
                genericName = "Ibuprofen",
                category = "Pain Reliever & Anti-inflammatory",
                primaryUses = listOf(
                    "Relieves pain and inflammation",
                    "Reduces fever",
                    "Eases muscle and joint pain"
                ),
                generalUsageInfo = listOf(
                    "Taken orally with food",
                    "Not for long-term use without medical advice"
                ),
                precautionsWarnings = listOf(
                    "Avoid if you have stomach ulcers",
                    "Use caution if you have kidney issues",
                    "Not recommended during late pregnancy"
                ),
                sideEffects = listOf(
                    "May cause stomach upset",
                    "Rarely causes dizziness",
                    "Long-term use may affect kidneys"
                )
            )
            key.contains("amoxicillin") || key.contains("novamox") || key.contains("mox") -> GeminiMedicineInfo(
                found = true,
                medicineName = "Amoxicillin",
                genericName = "Amoxicillin",
                category = "Antibiotic",
                primaryUses = listOf(
                    "Treats bacterial infections",
                    "Used for throat, ear, and chest infections"
                ),
                generalUsageInfo = listOf(
                    "Taken orally as prescribed by a doctor",
                    "Complete the full course even if you feel better"
                ),
                precautionsWarnings = listOf(
                    "Tell your doctor if allergic to penicillin",
                    "Does not work on viral infections like cold or flu"
                ),
                sideEffects = listOf(
                    "May cause mild stomach upset",
                    "Rarely causes skin rash",
                    "Seek help if you notice swelling or breathing trouble"
                )
            )
            else -> null
        }
    }

    private fun updateUiState(state: ResultScreenState) {
        val apiLoadingOverlay = findViewById<View>(R.id.apiLoadingOverlay)
        val containerFoundState = findViewById<View>(R.id.containerFoundState)
        val containerNotFoundState = findViewById<View>(R.id.containerNotFoundState)
        val tvUnrecognizedMessage = findViewById<TextView>(R.id.tvUnrecognizedMessage)

        when (state) {
            is ResultScreenState.Loading -> {
                apiLoadingOverlay?.visibility = View.VISIBLE
                containerFoundState?.visibility = View.GONE
                containerNotFoundState?.visibility = View.GONE
            }
            is ResultScreenState.Found -> {
                apiLoadingOverlay?.visibility = View.GONE
                containerFoundState?.visibility = View.VISIBLE
                containerNotFoundState?.visibility = View.GONE
                bindGeminiDataToUi(state.info)
            }
            is ResultScreenState.NotFound -> {
                apiLoadingOverlay?.visibility = View.GONE
                containerFoundState?.visibility = View.GONE
                containerNotFoundState?.visibility = View.VISIBLE
                tvUnrecognizedMessage?.text = state.message
            }
            is ResultScreenState.Error -> {
                apiLoadingOverlay?.visibility = View.GONE
                containerFoundState?.visibility = View.GONE
                containerNotFoundState?.visibility = View.VISIBLE
                tvUnrecognizedMessage?.text = state.message
            }
        }
    }

    private fun fetchGeminiMedicineData(medicineName: String) {
        updateUiState(ResultScreenState.Loading)

        val cacheKey = medicineName.lowercase(Locale.getDefault()).replace(" ", "_")
        val cachedInfo = GeminiMedicineCache.get(applicationContext, cacheKey)

        if (cachedInfo != null) {
            if (cachedInfo.found) {
                updateUiState(ResultScreenState.Found(cachedInfo))
            } else {
                updateUiState(ResultScreenState.NotFound())
            }
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val prompt = buildGeminiPrompt(medicineName)
            val groqKey = BuildConfig.GROQ_API_KEY

            try {
                val response = GroqClient.apiService.chat(
                    authHeader = "Bearer $groqKey",
                    request = GroqRequest(
                        model = "openai/gpt-oss-20b",
                        messages = listOf(element = GroqMessage(role = "user", content = prompt))
                    )
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val rawOutput = response.body()?.choices?.firstOrNull()?.message?.content.orEmpty()

                        Log.d("ResultActivity", "=== GROQ RAW RESPONSE START ===")
                        Log.d("ResultActivity", rawOutput)
                        Log.d("ResultActivity", "=== GROQ RAW RESPONSE END ===")

                        try {
                            val cleanJson = cleanJsonFence(rawOutput)
                            val info = gson.fromJson(cleanJson, GeminiMedicineInfo::class.java)

                            if (info != null && info.found) {
                                GeminiMedicineCache.put(applicationContext, cacheKey, info)
                                updateUiState(ResultScreenState.Found(info))
                            } else {
                                val notFoundInfo = GeminiMedicineInfo(found = false)
                                GeminiMedicineCache.put(applicationContext, cacheKey, notFoundInfo)
                                updateUiState(ResultScreenState.NotFound())
                            }
                        } catch (e: JsonSyntaxException) {
                            Log.e("ResultActivity", "JSON syntax error from Groq", e)
                            val fallback = getLocalFallback(medicineName)
                            if (fallback != null) {
                                updateUiState(ResultScreenState.Found(fallback))
                            } else {
                                Toast.makeText(this@ResultActivity, "Could not parse AI response. Please try again.", Toast.LENGTH_LONG).show()
                                updateUiState(ResultScreenState.Error("Could not parse AI response. Please try again."))
                            }
                        }
                    } else {
                        val statusCode = response.code()
                        val errorBody = response.errorBody()?.string().orEmpty()

                        Log.e("ResultActivity", "=== GROQ ERROR RESPONSE BODY START ===")
                        Log.e("ResultActivity", "Status Code: $statusCode")
                        Log.e("ResultActivity", "Error Body: $errorBody")
                        Log.e("ResultActivity", "=== GROQ ERROR RESPONSE BODY END ===")

                        val fallback = getLocalFallback(medicineName)
                        if (fallback != null) {
                            updateUiState(ResultScreenState.Found(fallback))
                        } else {
                            val errorMsg = "Server error ($statusCode). Check Logcat for details."
                            Toast.makeText(this@ResultActivity, errorMsg, Toast.LENGTH_LONG).show()
                            updateUiState(ResultScreenState.Error(errorMsg))
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ResultActivity", "Groq API exception", e)
                    val fallback = getLocalFallback(medicineName)
                    if (fallback != null) {
                        updateUiState(ResultScreenState.Found(fallback))
                    } else {
                        val errorMsg = when (e) {
                            is UnknownHostException, is ConnectException -> "No internet connection. Please check your network and try again."
                            is SocketTimeoutException -> "Request timed out. Please try again."
                            else -> e.localizedMessage ?: "An unexpected error occurred."
                        }
                        Toast.makeText(this@ResultActivity, errorMsg, Toast.LENGTH_LONG).show()
                        updateUiState(ResultScreenState.Error(errorMsg))
                    }
                }
            }
        }
    }

    private fun cleanJsonFence(raw: String): String {
        return raw.replace("```json", "")
            .replace("```", "")
            .trim()
    }

    private fun buildGeminiPrompt(medicineName: String): String {
        return """
You are a medical information assistant for an educational app. Based on the medicine name '$medicineName', provide accurate general information in this exact JSON format. If the input text has an obvious minor spelling mistake (like 'paracitamol' instead of 'Paracetamol'), still identify the intended medicine and correct the spelling in your response. If you are not confident this is a real medicine, set 'found' to false and leave other fields empty.

{
  "found": true,
  "medicineName": "Corrected Medicine Name",
  "genericName": "Generic Active Ingredient Name",
  "category": "Therapeutic Category (e.g. Pain Reliever & Fever Reducer)",
  "primaryUses": ["point 1", "point 2", "point 3"],
  "generalUsageInfo": ["general guidance only, NO specific dosage numbers or mg amounts"],
  "precautionsWarnings": ["point 1", "point 2", "point 3"],
  "sideEffects": ["point 1", "point 2", "point 3"]
}

Rules: Keep every bullet point under 12 words, use simple everyday language a non-medical person understands, no clinical jargon, no specific dosage amounts or mg numbers anywhere, 3 to 5 bullet points per field maximum. Respond with ONLY the JSON object, no other text before or after it.
""".trimIndent()
    }

    private fun bindGeminiDataToUi(info: GeminiMedicineInfo) {
        val tvMedicineName = findViewById<TextView>(R.id.tvMedicineName)
        val tvMedicineCategory = findViewById<TextView>(R.id.tvMedicineCategory)
        val tvMedicineGenericName = findViewById<TextView>(R.id.tvMedicineGenericName)
        val tvUsesContent = findViewById<TextView>(R.id.tvUsesContent)
        val tvDosageContent = findViewById<TextView>(R.id.tvDosageContent)
        val tvPrecautionsContent = findViewById<TextView>(R.id.tvPrecautionsContent)
        val tvSideEffectsContent = findViewById<TextView>(R.id.tvSideEffectsContent)

        val displayName = info.medicineName.takeIf { !it.isNullOrBlank() } ?: "Medicine Info"
        val formattedName = displayName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        tvMedicineName?.text = formattedName

        val categoryText = info.category.takeIf { !it.isNullOrBlank() } ?: "General Medication"
        tvMedicineCategory?.text = categoryText

        val genericText = info.genericName.takeIf { !it.isNullOrBlank() } ?: "Active Ingredient"
        tvMedicineGenericName?.text = genericText

        HistoryManager.saveScan(this, formattedName)

        val usesBullets = info.primaryUses.orEmpty()
        tvUsesContent?.text = if (usesBullets.isNotEmpty()) usesBullets.joinToString("\n\n") { "• $it" } else "Not available"

        val usageBullets = info.generalUsageInfo.orEmpty()
        tvDosageContent?.text = if (usageBullets.isNotEmpty()) usageBullets.joinToString("\n\n") { "• $it" } else "Not available"

        val precautionsBullets = info.precautionsWarnings.orEmpty()
        tvPrecautionsContent?.text = if (precautionsBullets.isNotEmpty()) precautionsBullets.joinToString("\n\n") { "• $it" } else "Not available"

        val sideEffectsBullets = info.sideEffects.orEmpty()
        tvSideEffectsContent?.text = if (sideEffectsBullets.isNotEmpty()) sideEffectsBullets.joinToString("\n\n") { "• $it" } else "Not available"

        findViewById<View>(R.id.contentUses)?.visibility = View.VISIBLE
        findViewById<View>(R.id.contentDosage)?.visibility = View.VISIBLE
        findViewById<View>(R.id.contentPrecautions)?.visibility = View.VISIBLE
        findViewById<View>(R.id.contentEffects)?.visibility = View.VISIBLE
        findViewById<ImageView>(R.id.arrowUses)?.setImageResource(R.drawable.ic_expand_less)
        findViewById<ImageView>(R.id.arrowDosage)?.setImageResource(R.drawable.ic_expand_less)
        findViewById<ImageView>(R.id.arrowPrecautions)?.setImageResource(R.drawable.ic_expand_less)
        findViewById<ImageView>(R.id.arrowEffects)?.setImageResource(R.drawable.ic_expand_less)
    }

    private fun setupExpandableSection(headerId: Int, contentId: Int, arrowId: Int) {
        val header = findViewById<View>(headerId)
        val content = findViewById<View>(contentId)
        val arrow = findViewById<ImageView>(arrowId)

        header?.setOnClickListener {
            if (content?.visibility == View.VISIBLE) {
                content.visibility = View.GONE
                arrow?.setImageResource(R.drawable.ic_expand_more)
            } else {
                content?.visibility = View.VISIBLE
                arrow?.setImageResource(R.drawable.ic_expand_less)
            }
        }
    }
}