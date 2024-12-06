package com.example.mediatracker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MediaPreferenceActivity : AppCompatActivity() {

    private lateinit var btnBooks: Button
    private lateinit var btnMovies: Button
    private lateinit var btnTVShows: Button
    private lateinit var btnMangas: Button
    private lateinit var btnWebnovels: Button
    private lateinit var btnOther: Button
    private lateinit var btnSavePreferences: Button

    private val selectedPreferences = mutableSetOf<String>() // Use a set to avoid duplicates

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_preference)

        // Initialise buttons
        btnBooks = findViewById(R.id.btn_books)
        btnMovies = findViewById(R.id.btn_movies)
        btnTVShows = findViewById(R.id.btn_tv_shows)
        btnMangas = findViewById(R.id.btn_mangas)
        btnWebnovels = findViewById(R.id.btn_webnovels)
        btnOther = findViewById(R.id.btn_other)
        btnSavePreferences = findViewById(R.id.btn_save_preferences)

        // Set click listeners for media buttons
        setupToggleButton(btnBooks, "Books")
        setupToggleButton(btnMovies, "Movies")
        setupToggleButton(btnTVShows, "TV Shows")
        setupToggleButton(btnMangas, "Mangas")
        setupToggleButton(btnWebnovels, "Webnovels")
        setupToggleButton(btnOther, "Other")

        // Save preferences when the button is clicked
        btnSavePreferences.setOnClickListener {
            savePreferencesToFirestore()
        }
    }

    private fun setupToggleButton(button: Button, preference: String) {
        button.setOnClickListener {
            if (selectedPreferences.contains(preference)) {
                // Deselect the button
                selectedPreferences.remove(preference)
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.button_unselected)) // Default colour
                button.setTextColor(Color.WHITE) // Text colour when not selected
            } else {
                // Select the button
                selectedPreferences.add(preference)
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.button_selected)) // Selected colour
                button.setTextColor(Color.WHITE) // Text colour when selected
            }
        }
    }

    private fun savePreferencesToFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val updates = mapOf(
            "mediaPreferences" to selectedPreferences.toList(),
            "isFirstTime" to false // Update flag
        )

        FirebaseFirestore.getInstance().collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Preferences saved!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish() // Navigate to the next activity
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save preferences: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}