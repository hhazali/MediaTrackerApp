package com.example.mediatracker

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

class DetailsFragment : Fragment() {

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_COVER_PHOTO = "cover_photo"
        private const val ARG_NOTES = "notes"
        private const val ARG_AUTHORS = "authors" // New argument

        fun newInstance(
            title: String,
            coverPhoto: String?,
            notes: String?,
            authors: String?
        ): DetailsFragment {
            val fragment = DetailsFragment()
            val args = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_COVER_PHOTO, coverPhoto)
                putString(ARG_NOTES, notes)
                putString(ARG_AUTHORS, authors) // Pass authors
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
        val tvAuthors = view.findViewById<TextView>(R.id.tvAuthors) // Authors TextView
        val tvBackButton = view.findViewById<TextView>(R.id.tvBackButton)

        val title = arguments?.getString(ARG_TITLE)
        val coverPhoto = arguments?.getString(ARG_COVER_PHOTO)
        val notes = arguments?.getString(ARG_NOTES)
        val authors = arguments?.getString(ARG_AUTHORS) // Get authors

        tvTitle.text = title ?: "No Title"
        tvNotes.text = notes ?: "No Notes"
        tvAuthors.text = authors ?: "Unknown Author" // Set authors

        if (!coverPhoto.isNullOrEmpty()) {
            Log.d("DetailsFragment", "Cover Photo URL: $coverPhoto")
            Glide.with(this)
                .load(coverPhoto)
                .placeholder(R.drawable.imageplaceholder) // Placeholder while loading
                .error(R.drawable.imageplaceholder)      // Fallback if the image fails to load
                .into(ivCoverPhoto)
        } else {
            ivCoverPhoto.setImageResource(R.drawable.imageplaceholder)
        }

        tvBackButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }
}
