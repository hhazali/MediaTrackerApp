package com.example.mediatracker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult

class ScannerActivity : AppCompatActivity() {

    private lateinit var barcodeScannerView: DecoratedBarcodeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        // Initialize the barcode scanner view
        barcodeScannerView = findViewById(R.id.barcode_scanner)

        // Start scanning
        barcodeScannerView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                if (result != null) {
                    handleBarcodeResult(result.text)
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<com.google.zxing.ResultPoint>?) {
                // No-op
            }
        })
    }

    private fun handleBarcodeResult(barcode: String) {
        Toast.makeText(this, "Scanned Barcode: $barcode", Toast.LENGTH_LONG).show()
        // TODO: Handle the scanned barcode (e.g., pass it to another activity or save it)
    }

    override fun onResume() {
        super.onResume()
        barcodeScannerView.resume() // Resume scanning when the activity is resumed
    }

    override fun onPause() {
        super.onPause()
        barcodeScannerView.pause() // Pause scanning when the activity is paused
    }
}
