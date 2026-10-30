package com.anish.mediscan

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnTogglePassword = findViewById<ImageView>(R.id.btnTogglePassword)

        btnTogglePassword?.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnTogglePassword.setImageResource(R.drawable.ic_visibility_off)
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnTogglePassword.setImageResource(R.drawable.ic_visibility)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        // Forgot Password link click handling -> Dialog
        findViewById<View>(R.id.tvForgotPassword)?.setOnClickListener {
            showForgotPasswordDialog()
        }

        // Login button click handling -> Firebase Auth
        findViewById<View>(R.id.btnLogin)?.setOnClickListener {
            loginUserWithFirebase()
        }

        // Don't have an account? Register link click handling -> Navigate to RegisterActivity
        val navigateToRegister = View.OnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        findViewById<View>(R.id.tvRegister)?.setOnClickListener(navigateToRegister)
        findViewById<View>(R.id.registerLayout)?.setOnClickListener(navigateToRegister)
    }

    private fun showForgotPasswordDialog() {
        val etResetEmail = EditText(this).apply {
            hint = "Enter registered email"
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            val currentTypedEmail = findViewById<EditText>(R.id.etEmail)?.text?.toString()?.trim().orEmpty()
            if (currentTypedEmail.isNotBlank()) {
                setText(currentTypedEmail)
            }
        }

        val container = FrameLayout(this).apply {
            setPadding(48, 16, 48, 16)
            addView(etResetEmail)
        }

        AlertDialog.Builder(this)
            .setTitle("Forgot Password")
            .setMessage("Enter your email address to receive a password reset link.")
            .setView(container)
            .setPositiveButton("OK") { _, _ ->
                val email = etResetEmail.text.toString().trim()
                if (email.isEmpty()) {
                    Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password reset link sent to your email", Toast.LENGTH_LONG).show()
                        } else {
                            val errorMsg = task.exception?.localizedMessage ?: "Failed to send password reset email."
                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loginUserWithFirebase() {
        val email = findViewById<EditText>(R.id.etEmail)?.text?.toString()?.trim().orEmpty()
        val password = findViewById<EditText>(R.id.etPassword)?.text?.toString()?.trim().orEmpty()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                } else {
                    val errorMsg = task.exception?.localizedMessage ?: "Login failed. Please try again."
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
    }
}
