package com.example.banner.backend.Controllers
import com.example.backend_banner.backend.Models.Usuario_
import com.example.backend_banner.backend.service.DatabaseDAO
import java.sql.ResultSet

class UserController {

    fun getAllUsers(): List<Usuario_> {
        val usuarios = mutableListOf<Usuario_>()
        val procedureName = "GetAllUsuarios"  // Nombre del procedimiento almacenado

        // Llamamos al procedimiento almacenado que devuelve un ResultSet
        val resultSet: ResultSet? = DatabaseDAO.executeStoredProcedureWithResults(procedureName)

        resultSet?.let {
            while (it.next()) {
                // Crear un objeto Usuario a partir del ResultSet
                val usuario = Usuario_(
                    it.getInt("id"),
                    it.getString("password"),
                    it.getString("role")
                )
                usuarios.add(usuario)
            }
            it.close() // Cerramos el ResultSet después de usarlo
        }

        return usuarios
    }

    // Insertar usuario utilizando un procedimiento almacenado
    fun insertUser(id: Int, password: String, role: String): Boolean {
        val procedureName = "insert_user" // Nombre del procedimiento almacenado
        return DatabaseDAO.executeStoredProcedure(procedureName, id, password, role)
    }

    fun updateUser(id: Int, password: String, role: String): Boolean {
        return DatabaseDAO.executeStoredProcedure("update_user", id, password, role)
    }

    //login
    fun loginUser(id: Int, password: String): Boolean {
        val procedureName = "sp_LoginUsuario"
        val resultSet = DatabaseDAO.executeStoredProcedureWithResults(procedureName, id, password)

        return resultSet?.let {
            if (it.next()) {
                it.getBoolean("p_resultado") // Obtener el valor de salida
            } else {
                false
            }
        } ?: false
    }
}