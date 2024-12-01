package com.example.mediatracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailsFragment : Fragment() {

    companion object {
        private const val ARG_ID = "id"
        private const val ARG_TITLE = "title"
        private const val ARG_COVER_PHOTO = "cover_photo"
        private const val ARG_NOTES = "notes"
        private const val ARG_AUTHORS = "authors"

        fun newInstance(
            id: String,
            title: String,
            coverPhoto: String?,
            notes: String?,
            authors: String?
        ): DetailsFragment {
            val fragment = DetailsFragment()
            val args = Bundle().apply {
                putString(ARG_ID, id) // Pass Firestore document ID
                putString(ARG_TITLE, title)
                putString(ARG_COVER_PHOTO, coverPhoto)
                putString(ARG_NOTES, notes)
                putString(ARG_AUTHORS, authors)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_details, container, false)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val ivCoverPhoto = view.findViewById<ImageView>(R.id.ivCoverPhoto)
        val tvNotes = view.findViewById<TextView>(R.id.tvNotes)
        val tvAuthors = view.findViewById<TextView>(R.id.tvAuthors)
        val tvBackButton = view.findViewById<TextView>(R.id.tvBackButton)
        val btnEditNotes = view.findViewById<Button>(R.id.btnEditNotes)
        val btnDeleteBook = view.findViewById<Button>(R.id.btnDeleteBook)

        val id = arguments?.getString(ARG_ID)
        val title = arguments?.getString(ARG_TITLE)
        val coverPhoto = arguments?.getString(ARG_COVER_PHOTO)
        val notes = arguments?.getString(ARG_NOTES)
        val authors = arguments?.getString(ARG_AUTHORS)

        tvTitle.text = title ?: "No Title"
        tvNotes.text = notes ?: "No Notes"
        tvAuthors.text = authors ?: "Unknown Author"

        if (!coverPhoto.isNullOrEmpty()) {
            Glide.with(this).load(coverPhoto).into(ivCoverPhoto)
        } else {
            ivCoverPhoto.setImageResource(R.drawable.imageplaceholder)
        }

        // Back button functionality
        tvBackButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Edit Notes functionality
        btnEditNotes.setOnClickListener {
            showEditNotesDialog(id, tvNotes.text.toString())
        }

        // Delete Book functionality
        btnDeleteBook.setOnClickListener {
            deleteBookFromFirestore(id)
        }

        return view
    }

    private fun showEditNotesDialog(id: String?, currentNotes: String) {
        if (id == null) return

        val editText = EditText(requireContext()).apply {
            setText(currentNotes)
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Edit Notes")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newNotes = editText.text.toString().trim()
                updateNotesInFirestore(id, newNotes)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun updateNotesInFirestore(id: String, newNotes: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .collection("books")
            .document(id)
            .update("notes", newNotes)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Notes updated!", Toast.LENGTH_SHORT).show()

                // Notify BooksActivity of the change
                (requireActivity() as? BooksActivity)?.updateBookNotesInList(id, newNotes)

                requireActivity().supportFragmentManager.popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update notes.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteBookFromFirestore(id: String?) {
        if (id == null) return

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .collection("books")
            .document(id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Book deleted!", Toast.LENGTH_SHORT).show()

                // Notify BooksActivity to remove the book
                (requireActivity() as? BooksActivity)?.removeBookFromList(id)

                requireActivity().supportFragmentManager.popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete book.", Toast.LENGTH_SHORT).show()
            }
    }
}
