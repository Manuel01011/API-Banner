package com.example.backend_banner.backend.Models

class Usuario_ {
    var id: Int = 0
    var password: String = ""
    var role: String = ""

    constructor()

    constructor(id: Int, password: String, role: String) {
        this.id = id
        this.password = password
        this.role = role
    }

    override fun toString(): String {
        return "Usuario(id=$id, password='$password', role='$role')"
    }
}