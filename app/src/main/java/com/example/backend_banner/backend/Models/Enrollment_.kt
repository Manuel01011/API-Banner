package com.example.backend_banner.backend.Models

class Enrollment_ {
    var studentId: Int = 0
    var grupoId: Int = 0
    var grade: Double = 0.0

    constructor()

    constructor(studentId: Int, grupoId: Int, grade: Double) {
        this.studentId = studentId
        this.grupoId = grupoId
        this.grade = grade
    }

    override fun toString(): String {
        return "Enrollment(studentId=$studentId, grupoId=$grupoId, grade=$grade)"
    }
}