package com.anish.mediscan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

class PrivacySecurityActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_privacy_security)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Back button returns to ProfileActivity
        findViewById<View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = currentUser?.email.orEmpty()

        // Change Password Row
        findViewById<View>(R.id.btnChangePasswordRow)?.setOnClickListener {
            if (email.isNotBlank()) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "A password reset link has been sent to your email.", Toast.LENGTH_LONG).show()
                        } else {
                            val error = task.exception?.localizedMessage ?: "Failed to send password reset email."
                            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "No valid user email found.", Toast.LENGTH_SHORT).show()
            }
        }

        // Delete Account Row
        findViewById<View>(R.id.btnDeleteAccountRow)?.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action is permanent and cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    currentUser?.delete()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Clear local cached data / SharedPreferences
                            val prefs = getSharedPreferences("mediscan_history_prefs", MODE_PRIVATE)
                            prefs.edit().clear().apply()

                            val cachePrefs = getSharedPreferences("mediscan_gemini_medicine_cache", MODE_PRIVATE)
                            cachePrefs.edit().clear().apply()

                            Toast.makeText(this, "Account deleted successfully.", Toast.LENGTH_LONG).show()

                            // Navigate to Login screen and clear back stack
                            val intent = Intent(this, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            val error = task.exception?.localizedMessage ?: "Failed to delete account. Please try logging in again first."
                            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
