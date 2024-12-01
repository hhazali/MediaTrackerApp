package com.example.mediatracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
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
    private lateinit var listView: ListView
    private lateinit var auth: FirebaseAuth
    private val books = mutableListOf<Map<String, Any>>() // Store books as maps
    private val mediaPreferences = mutableListOf<String>() // Dynamic menu items

    // Request code for starting ScannerActivity
    private val SCANNER_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_books)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toggleButton = findViewById(R.id.toggleButton)
        addBookButton = findViewById(R.id.btnAddBook)
        listView = findViewById(R.id.listView)
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

        // Fetch media preferences and books
        loadMediaPreferences()
        loadSavedBooks() // Make sure this method is public so it can be called externally

        // Set up click listener for ListView
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedBook = books[position]
            val title = selectedBook["title"] as? String
            val notes = selectedBook["notes"] as? String
            val coverPhoto = selectedBook["coverUrl"] as? String
            val authors = selectedBook["authors"] as? String

            // Navigate to DetailsFragment
            val fragment = DetailsFragment.newInstance(
                title = title ?: "No Title",
                coverPhoto = coverPhoto,
                notes = notes,
                authors = authors
            )
            supportFragmentManager.beginTransaction()
                .replace(R.id.drawerLayout, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun showAddBookOptions() {
        val options = arrayOf("Scan Barcode", "Add Manually")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Book")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, ScannerActivity::class.java)
                        startActivityForResult(intent, SCANNER_REQUEST_CODE) // Start ScannerActivity for result
                    }
                    1 -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.drawerLayout, ManualAdditionFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    public fun loadSavedBooks() {
        val userId = auth.currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .collection("books")
            .get()
            .addOnSuccessListener { documents ->
                books.clear()
                for (document in documents) {
                    val bookData = document.data
                    books.add(bookData)
                }
                updateBookList()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load books.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateBookList() {
        val bookTitles = books.map { it["title"] as? String ?: "Untitled" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, bookTitles)
        listView.adapter = adapter
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
        menu.clear()

        for (media in mediaPreferences) {
            menu.add(media).setOnMenuItemClickListener {
                Toast.makeText(this, "Selected: $media", Toast.LENGTH_SHORT).show()
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
        }

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
            else -> Toast.makeText(this, "Navigating to ${item.title}", Toast.LENGTH_SHORT).show()
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
