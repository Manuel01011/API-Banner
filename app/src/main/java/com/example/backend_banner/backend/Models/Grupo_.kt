package com.example.backend_banner.backend.Models

class Grupo_ {
    var id: Int = 0
    var numberGroup: Int = 0
    var year: Int = 0
    var horario: String = ""
    var courseCod: Int = 0
    var teacherId: Int = 0

    constructor()

    constructor(id: Int, numberGroup: Int, year: Int, horario: String, courseCod: Int, teacherId: Int) {
        this.id = id
        this.numberGroup = numberGroup
        this.year = year
        this.horario = horario
        this.courseCod = courseCod
        this.teacherId = teacherId
    }

    override fun toString(): String {
        return "Grupo(id=$id, numberGroup=$numberGroup, year=$year, horario='$horario', courseCod=$courseCod, teacherId=$teacherId)"
    }
}