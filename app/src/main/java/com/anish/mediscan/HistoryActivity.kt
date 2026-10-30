package com.anish.mediscan

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

enum class HistoryFilterType {
    ALL, FAVORITES, PRESCRIPTIONS, OTC
}

class HistoryActivity : AppCompatActivity() {

    private var currentFilter = HistoryFilterType.ALL
    private var searchQuery = ""
    private var historyAdapter: HistoryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Back button returns to HomeActivity
        findViewById<View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        // Bottom Navigation
        findViewById<View>(R.id.navHome)?.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.navScan)?.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.navProfile)?.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Search EditText Watcher
        val etSearchHistory = findViewById<EditText>(R.id.etSearchHistory)
        etSearchHistory?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString()?.trim().orEmpty()
                refreshFilteredList()
            }
        })

        // Setup Filter Chips
        setupFilterChips()

        // Setup RecyclerView
        val rvHistory = findViewById<RecyclerView>(R.id.rvHistory)
        rvHistory?.layoutManager = LinearLayoutManager(this)

        refreshFilteredList()
    }

    private fun setupFilterChips() {
        val filterAll = findViewById<TextView>(R.id.filterAll)
        val filterFavorites = findViewById<TextView>(R.id.filterFavorites)
        val filterPrescriptions = findViewById<TextView>(R.id.filterPrescriptions)
        val filterOtc = findViewById<TextView>(R.id.filterOtc)

        val chips = listOf(
            filterAll to HistoryFilterType.ALL,
            filterFavorites to HistoryFilterType.FAVORITES,
            filterPrescriptions to HistoryFilterType.PRESCRIPTIONS,
            filterOtc to HistoryFilterType.OTC
        )

        chips.forEach { (view, type) ->
            view?.setOnClickListener {
                currentFilter = type
                updateChipStyles()
                refreshFilteredList()
            }
        }

        updateChipStyles()
    }

    private fun updateChipStyles() {
        val filterAll = findViewById<TextView>(R.id.filterAll)
        val filterFavorites = findViewById<TextView>(R.id.filterFavorites)
        val filterPrescriptions = findViewById<TextView>(R.id.filterPrescriptions)
        val filterOtc = findViewById<TextView>(R.id.filterOtc)

        val map = mapOf(
            HistoryFilterType.ALL to filterAll,
            HistoryFilterType.FAVORITES to filterFavorites,
            HistoryFilterType.PRESCRIPTIONS to filterPrescriptions,
            HistoryFilterType.OTC to filterOtc
        )

        map.forEach { (type, view) ->
            if (type == currentFilter) {
                view?.setBackgroundResource(R.drawable.bg_chip_active)
                view?.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                view?.setBackgroundResource(R.drawable.bg_chip_inactive)
                view?.setTextColor(ContextCompat.getColor(this, R.color.on_surface_variant))
            }
        }
    }

    private fun refreshFilteredList() {
        val allScans = HistoryManager.getScans(applicationContext)

        // Apply Tab Filter
        val tabFiltered = when (currentFilter) {
            HistoryFilterType.ALL -> allScans
            HistoryFilterType.FAVORITES -> allScans.filter { it.isFavorite }
            HistoryFilterType.PRESCRIPTIONS -> allScans.filter { it.requiresPrescription }
            HistoryFilterType.OTC -> allScans.filter { !it.requiresPrescription }
        }

        // Apply Search Query Filter
        val finalFiltered = if (searchQuery.isNotBlank()) {
            tabFiltered.filter { it.name.contains(searchQuery, ignoreCase = true) || it.dosage.contains(searchQuery, ignoreCase = true) }
        } else {
            tabFiltered
        }

        val rvHistory = findViewById<RecyclerView>(R.id.rvHistory)
        val emptyStateLayout = findViewById<View>(R.id.emptyStateLayout)

        if (finalFiltered.isNotEmpty()) {
            rvHistory?.visibility = View.VISIBLE
            emptyStateLayout?.visibility = View.GONE

            if (historyAdapter == null) {
                historyAdapter = HistoryAdapter(
                    items = finalFiltered,
                    onItemClick = { item ->
                        val intent = Intent(this, ResultActivity::class.java).apply {
                            putExtra(ScanActivity.EXTRA_RECOGNIZED_TEXT, item.name)
                        }
                        startActivity(intent)
                    },
                    onFavoriteClick = { item ->
                        HistoryManager.toggleFavorite(applicationContext, item.id)
                        refreshFilteredList()
                    },
                    onDeleteClick = { item ->
                        confirmDeleteScan(item)
                    }
                )
                rvHistory?.adapter = historyAdapter
            } else {
                historyAdapter?.updateItems(finalFiltered)
            }
        } else {
            rvHistory?.visibility = View.GONE
            emptyStateLayout?.visibility = View.VISIBLE
        }
    }

    private fun confirmDeleteScan(item: ScanHistoryItem) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Scan")
            .setMessage("Are you sure you want to remove '${item.name}' from your scan history?")
            .setPositiveButton("Delete") { _, _ ->
                HistoryManager.deleteScan(applicationContext, item.id)
                Toast.makeText(this, "Removed from history", Toast.LENGTH_SHORT).show()
                refreshFilteredList()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
