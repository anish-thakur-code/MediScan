package com.anish.mediscan

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeActivity : AppCompatActivity() {

    private var debounceJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set dynamic user greeting from Firebase Auth
        val currentUser = FirebaseAuth.getInstance().currentUser
        val displayName = currentUser?.displayName
        val greetingName = if (!displayName.isNullOrBlank()) displayName else "User"
        val tvUserGreeting = findViewById<TextView>(R.id.tvUserGreeting)
        tvUserGreeting?.text = "Hello, $greetingName"

        // Update Daily Health Tip
        updateDailyHealthTip()

        // Mode Toggle Tabs (Scan vs Search)
        val cardScanMedicine = findViewById<View>(R.id.cardScanMedicine)
        val cardSearchMedicine = findViewById<View>(R.id.cardSearchMedicine)
        val btnToggleScan = findViewById<TextView>(R.id.btnToggleScan)
        val btnToggleSearch = findViewById<TextView>(R.id.btnToggleSearch)

        btnToggleScan?.setOnClickListener {
            btnToggleScan.setBackgroundResource(R.drawable.bg_chip_active)
            btnToggleScan.setTextColor(ContextCompat.getColor(this, R.color.white))
            btnToggleSearch?.setBackgroundResource(R.drawable.bg_chip_inactive)
            btnToggleSearch?.setTextColor(ContextCompat.getColor(this, R.color.on_surface_variant))

            cardScanMedicine?.visibility = View.VISIBLE
            cardSearchMedicine?.visibility = View.GONE
        }

        btnToggleSearch?.setOnClickListener {
            btnToggleSearch.setBackgroundResource(R.drawable.bg_chip_active)
            btnToggleSearch.setTextColor(ContextCompat.getColor(this, R.color.white))
            btnToggleScan?.setBackgroundResource(R.drawable.bg_chip_inactive)
            btnToggleScan?.setTextColor(ContextCompat.getColor(this, R.color.on_surface_variant))

            cardScanMedicine?.visibility = View.GONE
            cardSearchMedicine?.visibility = View.VISIBLE
        }

        // Search Input Handling & Live Debounced Suggestions
        val etSearchMedicine = findViewById<EditText>(R.id.etSearchMedicine)
        val btnSubmitSearch = findViewById<View>(R.id.btnSubmitSearch)
        val rvSearchSuggestions = findViewById<RecyclerView>(R.id.rvSearchSuggestions)

        val performSearchSubmit = {
            val query = etSearchMedicine?.text?.toString()?.trim().orEmpty()
            if (query.isNotBlank()) {
                val intent = Intent(this, ResultActivity::class.java).apply {
                    putExtra(ScanActivity.EXTRA_RECOGNIZED_TEXT, query)
                }
                startActivity(intent)
            }
        }

        btnSubmitSearch?.setOnClickListener {
            performSearchSubmit()
        }

        etSearchMedicine?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearchSubmit()
                true
            } else false
        }

        etSearchMedicine?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                debounceJob?.cancel()
                val query = s?.toString()?.trim().orEmpty()

                if (query.isBlank()) {
                    rvSearchSuggestions?.visibility = View.GONE
                    return
                }

                debounceJob = lifecycleScope.launch {
                    delay(500) // 500ms debounce before showing suggestions

                    val historyItems = HistoryManager.getScans(applicationContext)
                    val matchingSuggestions = historyItems
                        .map { it.name }
                        .filter { it.contains(query, ignoreCase = true) }
                        .distinct()

                    if (matchingSuggestions.isNotEmpty()) {
                        rvSearchSuggestions?.visibility = View.VISIBLE
                        rvSearchSuggestions?.layoutManager = LinearLayoutManager(this@HomeActivity)
                        rvSearchSuggestions?.adapter = SuggestionAdapter(matchingSuggestions) { selectedName ->
                            val intent = Intent(this@HomeActivity, ResultActivity::class.java).apply {
                                putExtra(ScanActivity.EXTRA_RECOGNIZED_TEXT, selectedName)
                            }
                            startActivity(intent)
                        }
                    } else {
                        rvSearchSuggestions?.visibility = View.GONE
                    }
                }
            }
        })

        // Navigate to ScanActivity on 'Scan Medicine' card/button or bottom nav Scan
        val openScanner = View.OnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }
        findViewById<View>(R.id.cardScanMedicine)?.setOnClickListener(openScanner)
        findViewById<View>(R.id.btnLiveScanner)?.setOnClickListener(openScanner)
        findViewById<View>(R.id.navScan)?.setOnClickListener(openScanner)

        // Navigate to HistoryActivity on 'View All' or bottom nav History
        val openHistory = View.OnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
        findViewById<View>(R.id.btnViewAllScans)?.setOnClickListener(openHistory)
        findViewById<View>(R.id.navHistory)?.setOnClickListener(openHistory)

        // Navigate to ProfileActivity
        val openProfile = View.OnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        findViewById<View>(R.id.btnProfileHeader)?.setOnClickListener(openProfile)
        findViewById<View>(R.id.navProfile)?.setOnClickListener(openProfile)
    }

    override fun onResume() {
        super.onResume()
        updateDailyHealthTip()
        refreshRecentScans()
    }

    private fun updateDailyHealthTip() {
        val tvDailyHealthTip = findViewById<TextView>(R.id.tvDailyHealthTip)
        val tipsArray = resources.getStringArray(R.array.health_tips)

        if (tipsArray.isNotEmpty()) {
            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val tipIndex = (dayOfYear - 1) % tipsArray.size
            val todaysTip = tipsArray[Math.abs(tipIndex)]
            tvDailyHealthTip?.text = todaysTip
        }
    }

    private fun refreshRecentScans() {
        val rvRecentScans = findViewById<RecyclerView>(R.id.rvRecentScans)
        val tvRecentScansEmpty = findViewById<TextView>(R.id.tvRecentScansEmpty)

        val allScans = HistoryManager.getScans(applicationContext)
        val recentTop5 = allScans.take(5)

        if (recentTop5.isNotEmpty()) {
            rvRecentScans?.visibility = View.VISIBLE
            tvRecentScansEmpty?.visibility = View.GONE

            rvRecentScans?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            rvRecentScans?.adapter = RecentScanAdapter(recentTop5) { selectedItem ->
                val intent = Intent(this, ResultActivity::class.java).apply {
                    putExtra(ScanActivity.EXTRA_RECOGNIZED_TEXT, selectedItem.name)
                }
                startActivity(intent)
            }
        } else {
            rvRecentScans?.visibility = View.GONE
            tvRecentScansEmpty?.visibility = View.VISIBLE
        }
    }
}
