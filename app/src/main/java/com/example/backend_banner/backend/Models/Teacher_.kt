package com.example.backend_banner.backend.Models

class Teacher_ {
    var id: Int = 0
    var name: String = ""
    var telNumber: Int = 0
    var email: String = ""

    constructor()

    constructor(id: Int, name: String, telNumber: Int, email: String) {
        this.id = id
        this.name = name
        this.telNumber = telNumber
        this.email = email
    }

    override fun toString(): String {
        return "Teacher(id=$id, name='$name', telNumber=$telNumber, email='$email')"
    }
}