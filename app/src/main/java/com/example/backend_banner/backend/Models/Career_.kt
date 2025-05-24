package com.example.backend_banner.backend.Models

data class Career_(
    var cod: Int = 0,
    var name: String = "",
    var title: String = "",
    val courses: List<Course_> = emptyList()
) {
    constructor() : this(0, "", "") // Constructor vacío necesario para GSON
}