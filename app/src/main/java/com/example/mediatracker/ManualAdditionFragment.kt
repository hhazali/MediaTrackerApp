package com.example.mediatracker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ManualAdditionFragment : Fragment() {

    private lateinit var etTitle: EditText
    private lateinit var etNotes: EditText
    private lateinit var btnSave: Button
    private lateinit var btnAddCoverPhoto: Button
    private var coverPhotoUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manual_addition, container, false)
        etTitle = view.findViewById(R.id.et_title)
        etNotes = view.findViewById(R.id.et_notes)
        btnSave = view.findViewById(R.id.btn_save_manual_entry)
        btnAddCoverPhoto = view.findViewById(R.id.btn_add_cover_photo)

        btnAddCoverPhoto.setOnClickListener { openImagePicker() }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val notes = etNotes.text.toString().trim()

            if (title.isNotEmpty()) {
                if (coverPhotoUri != null) {
                    uploadCoverPhotoAndSaveBook(title, notes)
                } else {
                    saveToFirestore(title, notes, null)
                }
            } else {
                Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            coverPhotoUri = data.data
            Toast.makeText(requireContext(), "Cover photo selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadCoverPhotoAndSaveBook(title: String, notes: String?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("users/$userId/covers/${System.currentTimeMillis()}.jpg")

        coverPhotoUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveToFirestore(title, notes, downloadUrl.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to upload cover photo", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveToFirestore(title: String, notes: String?, coverUrl: String?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val bookData = hashMapOf(
            "title" to title,
            "notes" to notes,
            "coverUrl" to coverUrl
        )

        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .collection("books")
            .add(bookData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Book saved!", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
                (requireActivity() as BooksActivity).loadSavedBooks() // Refresh book list
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}