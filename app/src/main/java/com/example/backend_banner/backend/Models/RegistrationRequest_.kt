package com.example.backend_banner.backend.Models

data class RegistrationRequest_(
    val id: Int,
    val user_id: Int,
    val password: String,
    val role: String, // 'student', 'teacher', 'admin', 'matriculador'
    val name: String?,
    val tel_number: Int?,
    val email: String?,
    val born_date: String?,
    val career_cod: Int?,
    val status: String, // 'pending', 'approved', 'rejected'
    val request_date: String
)