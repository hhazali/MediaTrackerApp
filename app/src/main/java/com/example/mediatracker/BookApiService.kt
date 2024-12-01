// BookApiService.kt
package com.example.mediatracker

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BookApiService {
    @GET("api/books")
    fun getBookDetailsByIsbn(
        @Query("bibkeys") isbn: String,  // Query parameter for ISBN
        @Query("format") format: String = "json",  // Request response in JSON format
        @Query("jscmd") jscmd: String = "data" // Request specific data (title, author, cover image, etc.)
    ): Call<Map<String, Any>>  // Open Library API returns a map of book data
}
