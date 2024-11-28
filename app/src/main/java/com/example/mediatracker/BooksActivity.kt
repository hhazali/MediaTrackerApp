package com.example.mediatracker

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BooksActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggleButton: ImageButton
    private lateinit var addBookButton: ImageButton
    private lateinit var auth: FirebaseAuth
    private val mediaPreferences = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_books)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toggleButton = findViewById(R.id.toggleButton)
        addBookButton = findViewById(R.id.btnAddBook)
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

        // Plus button functionality
        addBookButton.setOnClickListener {
            showAddBookOptions()
        }

        // Fetch media preferences from Firestore
        loadMediaPreferences()
    }

    private fun showAddBookOptions() {
        val options = arrayOf("Scan Barcode", "Add Manually")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Book")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Scan Barcode Option
                        Toast.makeText(this, "Scan Barcode selected", Toast.LENGTH_SHORT).show()
                        // TODO: Implement barcode scanning functionality
                    }
                    1 -> {
                        // Add Manually Option
                        Toast.makeText(this, "Add Manually selected", Toast.LENGTH_SHORT).show()
                        // TODO: Navigate to manual book addition page
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
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
                Toast.makeText(this, "Selected: $media", Toast.LENGTH_SHORT).show()
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
            "Home" -> {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
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

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
