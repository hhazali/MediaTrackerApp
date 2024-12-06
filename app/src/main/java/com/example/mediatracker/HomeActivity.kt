package com.example.mediatracker

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggleButton: ImageButton
    private lateinit var auth: FirebaseAuth
    private val mediaPreferences = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toggleButton = findViewById(R.id.toggleButton)
        auth = FirebaseAuth.getInstance()

        // Set up the toggle button to open/close the drawer
        toggleButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        navigationView.setNavigationItemSelectedListener(this)

        // Fetch media preferences from Firestore
        loadMediaPreferences()
    }

    private fun loadMediaPreferences() {
        val userId = auth.currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val preferences = document.get("mediaPreferences") as? List<String> ?: emptyList()
                mediaPreferences.clear()
                mediaPreferences.addAll(preferences)
                updateMenu()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load preferences.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateMenu() {
        val menu = navigationView.menu
        menu.clear() // Clear existing items

        // Add media preferences dynamically
        for (media in mediaPreferences) {
            menu.add(media).setOnMenuItemClickListener {
                if (media == "Books") {
                    navigateToBooksActivity() // Navigate to BooksActivity
                } else {
                    Toast.makeText(this, "Selected: $media", Toast.LENGTH_SHORT).show()
                }
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
        }

        // Add static menu items if needed
        menu.add("Add New Media").setOnMenuItemClickListener {
            Toast.makeText(this, "Add New Media clicked", Toast.LENGTH_SHORT).show()
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.title) {
            "Books" -> navigateToBooksActivity()
            "Add New Media" -> {
                Toast.makeText(this, "Add New Media Selected", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Navigating to ${item.title}", Toast.LENGTH_SHORT).show()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun navigateToBooksActivity() {
        val intent = Intent(this, BooksActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}