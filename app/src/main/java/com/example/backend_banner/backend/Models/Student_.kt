package com.example.backend_banner.backend.Models

class Student_ {
    var id: Int = 0
    var name: String = ""
    var telNumber: Int = 0
    var email: String = ""
    var bornDate: String = ""
    var careerCod: Int = 0
    var password : String = ""

    constructor()

    constructor(id: Int, name: String, telNumber: Int, email: String, bornDate: String, careerCod: Int, password: String) {
        this.id = id
        this.name = name
        this.telNumber = telNumber
        this.email = email
        this.bornDate = bornDate
        this.careerCod = careerCod
        this.password = password
    }

    override fun toString(): String {
        return "Student(id=$id, name='$name', telNumber=$telNumber, email='$email', bornDate='$bornDate', careerCod=$careerCod), password='$password')"
    }
}