package com.example.backend_banner.backend.Models

class Teacher_ {
    var id: Int = 0
    var name: String = ""
    var telNumber: Int = 0
    var email: String = ""
    var password : String = ""

    constructor()

    constructor(id: Int, name: String, telNumber: Int, email: String, password: String) {
        this.id = id
        this.name = name
        this.telNumber = telNumber
        this.email = email
        this.password = password
    }

    override fun toString(): String {
        return "Teacher(id=$id, name='$name', telNumber=$telNumber, email='$email' , password='$password')"
    }
}