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

    fun insertUser(id: Int, password: String, role: String): Boolean {
        val procedureName = "insert_user"

        // Validar el rol antes de insertar
        val validRoles = setOf("admin", "teacher", "student")
        if (!validRoles.contains(role.lowercase())) {
            throw IllegalArgumentException("Invalid role. Must be one of: ${validRoles.joinToString()}")
        }

        // Validar la contraseña
        if (password.isBlank() || password.length < 4) {
            throw IllegalArgumentException("Password must be at least 4 characters long")
        }

        return DatabaseDAO.executeStoredProcedure(procedureName, id, password, role)
    }

    fun updateUser(id: Int, password: String, role: String): Boolean {
        return DatabaseDAO.executeStoredProcedure("update_user", id, password, role)
    }

    fun deleteUser(id: Int): Boolean {
        return DatabaseDAO.executeStoredProcedure("delete_user", id)
    }

    fun loginUser(id: Int, password: String): Boolean {
        val user = getUserById(id)
        return user != null && user.password == password
    }

    fun getUserById(id: Int): Usuario_? {
        val procedureName = "GetUsuarioById"
        val resultSet = DatabaseDAO.executeStoredProcedureWithResults(procedureName, id)

        return resultSet?.let {
            if (it.next()) {
                Usuario_(
                    it.getInt("id"),
                    it.getString("password"),
                    it.getString("role")
                ).also {
                    resultSet.close() // Asegurarse de cerrar el ResultSet
                }
            } else {
                null
            }
        }
    }

}