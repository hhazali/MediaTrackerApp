package com.example.mediatracker

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

class ManualAdditionFragment : Fragment() {

    private lateinit var etTitle: EditText
    private lateinit var etNotes: EditText
    private lateinit var btnSave: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manual_addition, container, false)
        etTitle = view.findViewById(R.id.et_title)
        etNotes = view.findViewById(R.id.et_notes)
        btnSave = view.findViewById(R.id.btn_save_manual_entry)

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val notes = etNotes.text.toString().trim()
            if (title.isNotEmpty()) {
                saveToFirestore(title, notes)
            } else {
                Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun saveToFirestore(title: String, notes: String?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val bookData = hashMapOf(
            "title" to title,
            "notes" to notes
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
