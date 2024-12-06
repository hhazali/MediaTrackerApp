package com.example.mediatracker

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ScannerActivity : AppCompatActivity() {

    private lateinit var barcodeScannerView: DecoratedBarcodeView
    private var isBarcodeScanned = false // Flag to track if barcode has already been scanned

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        val tvBackButton = findViewById<TextView>(R.id.tvBackButton)

        // Back button functionality
        tvBackButton.setOnClickListener {
            onBackPressed()
        }

        // Initialise the barcode scanner view
        barcodeScannerView = findViewById(R.id.barcode_scanner)

        // Start scanning
        barcodeScannerView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                if (result != null && !isBarcodeScanned) { // Only save if the barcode hasn't been scanned yet
                    isBarcodeScanned = true // Mark as scanned to prevent saving multiple times
                    handleBarcodeResult(result.text)
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<com.google.zxing.ResultPoint>?) {
                // No-operations
            }
        })
    }

    private fun handleBarcodeResult(barcode: String) {
        Toast.makeText(this, "Scanned Barcode: $barcode", Toast.LENGTH_LONG).show()

        // Fetch book details using the scanned ISBN from Open Library API
        fetchBookDetails(barcode)
    }

    private fun fetchBookDetails(isbn: String) {
        // Retrofit setup
        val retrofit = Retrofit.Builder()
            .baseUrl("https://openlibrary.org/") // Base URL of Open Library API
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(BookApiService::class.java)

        // Prepare ISBN for Open Library query
        val isbnKey = "ISBN:$isbn"

        //API call to fetch book details using the ISBN
        service.getBookDetailsByIsbn(isbnKey).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    val bookData = response.body()
                    // Check if the response contains the correct data for the ISBN
                    val bookDetails = bookData?.get(isbnKey) as? Map<String, Any>

                    // Extract the details (title, authors, cover)
                    val title = bookDetails?.get("title") as? String ?: "Unknown Title"
                    val authorsList = (bookDetails?.get("authors") as? List<Map<String, String>>)
                        ?.joinToString(", ") { it["name"] ?: "Unknown Author" }
                        ?: "Unknown Author"
                    val coverUrl = (bookDetails?.get("cover") as? Map<String, String>)?.get("medium")
                        ?: "https://via.placeholder.com/150"  // Default placeholder for cover

                    // Show book details
                    Toast.makeText(this@ScannerActivity, "Title: $title\nAuthors: $authorsList", Toast.LENGTH_LONG).show()

                    // Save the book details to Firestore
                    saveToFirestore(isbn, title, authorsList, coverUrl)
                } else {
                    Toast.makeText(this@ScannerActivity, "Failed to retrieve book details.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(this@ScannerActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveToFirestore(isbn: String, title: String, authors: String?, coverUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val bookData = hashMapOf(
            "isbn" to isbn,
            "title" to title,
            "authors" to authors,
            "coverUrl" to coverUrl
        )

        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .collection("books")
            .add(bookData)
            .addOnSuccessListener {
                Toast.makeText(this, "Book saved successfully!", Toast.LENGTH_SHORT).show()

                // Prepare data to pass back to BooksActivity
                val resultIntent = Intent().apply {
                    putExtra("isbn", isbn)
                    putExtra("title", title)
                    putExtra("authors", authors)
                    putExtra("coverUrl", coverUrl)
                }

                setResult(RESULT_OK, resultIntent) // Set result and send back to BooksActivity
                finish() // Close ScannerActivity and return to BooksActivity
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save book: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        barcodeScannerView.resume() // Resume scanning when the activity is resumed
        isBarcodeScanned = false // Reset the flag when resuming, so new scans can be saved
    }

    override fun onPause() {
        super.onPause()
        barcodeScannerView.pause() // Pause scanning when the activity is paused
    }
}