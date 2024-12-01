package com.example.mediatracker

data class BookResponse(
    val title: String?,
    val authors: List<Author>?,
    val cover: Cover?
)

data class Author(
    val name: String
)

data class Cover(
    val medium: String
)
