package com.example.backend_banner.backend.Models

class Course_ {
    var cod: Int = 0
    var name: String = ""
    var credits: Int = 0
    var hours: Int = 0
    var cicloId: Int = 0
    var careerCod: Int = 0

    constructor()

    constructor(cod: Int, name: String, credits: Int, hours: Int, cicloId: Int, careerCod: Int) {
        this.cod = cod
        this.name = name
        this.credits = credits
        this.hours = hours
        this.cicloId = cicloId
        this.careerCod = careerCod
    }

    override fun toString(): String {
        return "Course(cod=$cod, name='$name', credits=$credits, hours=$hours, cicloId=$cicloId, careerCod=$careerCod)"
    }
}