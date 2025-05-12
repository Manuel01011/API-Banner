package com.example.backend_banner.backend.service

import java.sql.Connection
import java.sql.DriverManager
import java.sql.CallableStatement
import java.sql.SQLException

class servicio {

    private val dbUrl = "jdbc:mysql://localhost:3306/banner"
    private val dbUser = "root"
    private val dbPassword = "root"

    init {
        try {
            // Registrar el controlador
            Class.forName("com.mysql.cj.jdbc.Driver")
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }

    // Función para obtener la conexión
    fun getConnection(): Connection? {
        return try {
            DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        } catch (e: SQLException) {
            e.printStackTrace()
            null
        }
    }

    // Función para cerrar la conexión
    fun closeConnection(connection: Connection?) {
        try {
            connection?.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
}