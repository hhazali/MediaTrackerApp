package com.example.mediatracker

import android.os.Bundle
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

        fun newInstance(title: String, coverPhoto: String?, notes: String?): DetailsFragment {
            val fragment = DetailsFragment()
            val args = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_COVER_PHOTO, coverPhoto)
                putString(ARG_NOTES, notes)
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
        val tvBackButton = view.findViewById<TextView>(R.id.tvBackButton)

        val title = arguments?.getString(ARG_TITLE)
        val coverPhoto = arguments?.getString(ARG_COVER_PHOTO)
        val notes = arguments?.getString(ARG_NOTES)

        tvTitle.text = title ?: "No Title"
        tvNotes.text = notes ?: "No Notes"

        if (!coverPhoto.isNullOrEmpty()) {
            Glide.with(this).load(coverPhoto).into(ivCoverPhoto)
        } else {
            ivCoverPhoto.setImageResource(R.drawable.imageplaceholder)
        }

        // Back button functionality
        tvBackButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }
}
