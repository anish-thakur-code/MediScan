package com.anish.mediscan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Display user details from Firebase Auth
        val currentUser = FirebaseAuth.getInstance().currentUser
        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        val tvProfileEmail = findViewById<TextView>(R.id.tvProfileEmail)

        val displayName = currentUser?.displayName
        tvProfileName?.text = if (!displayName.isNullOrBlank()) displayName else "User"
        tvProfileEmail?.text = currentUser?.email.orEmpty()

        // Account Settings click handler -> Navigate to AccountSettingsActivity
        findViewById<View>(R.id.btnAccountSettings)?.setOnClickListener {
            val intent = Intent(this, AccountSettingsActivity::class.java)
            startActivity(intent)
        }

        // Privacy & Security click handler -> Navigate to PrivacySecurityActivity
        findViewById<View>(R.id.btnPrivacySecurity)?.setOnClickListener {
            val intent = Intent(this, PrivacySecurityActivity::class.java)
            startActivity(intent)
        }

        // Back button returns to HomeActivity
        findViewById<View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        // Logout button signs out from Firebase, navigates to MainActivity and clears entire back stack
        findViewById<View>(R.id.btnLogout)?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
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

        findViewById<View>(R.id.navHistory)?.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
