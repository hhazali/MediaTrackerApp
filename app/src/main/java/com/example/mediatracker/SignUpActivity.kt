package com.example.mediatracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var tvLoginRedirect: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialise Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialise views
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        btnSignUp = findViewById(R.id.btn_sign_up)
        tvLoginRedirect = findViewById(R.id.tv_login_redirect)

        // Handle Sign-Up Button Click
        btnSignUp.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (validateInputs(email, password, confirmPassword)) {
                signUpWithEmail(email, password)
            }
        }

        // Handle Login Redirect Click
        tvLoginRedirect.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close SignUpActivity to prevent going back
        }
    }

    private fun validateInputs(email: String, password: String, confirmPassword: String): Boolean {
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email"
            return false
        }
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            return false
        }
        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters long"
            return false
        }
        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            return false
        }
        return true
    }

    private fun signUpWithEmail(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    saveUserDataToFirestore(userId, email)
                } else {
                    Toast.makeText(this, "Sign-Up Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserDataToFirestore(userId: String, email: String) {
        val user = hashMapOf(
            "email" to email,
            "isFirstTime" to true,
            "mediaPreferences" to emptyList<String>()
        )

        FirebaseFirestore.getInstance().collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Sign-Up Complete!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MediaPreferenceActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
