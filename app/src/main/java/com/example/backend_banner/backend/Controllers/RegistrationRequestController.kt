package com.example.backend_banner.backend.Controllers

import com.example.backend_banner.backend.Models.RegistrationRequest_
import com.example.backend_banner.backend.service.DatabaseDAO
import com.example.banner.backend.service.NoDataException
import java.sql.ResultSet
import java.sql.SQLException

class RegistrationRequestController {

    fun createRequest(
        user_id: Int,
        password: String,
        role: String,
        name: String?,
        tel_number: Int?,
        email: String?,
        born_date: String?,
        career_cod: Int?
    ): Int {
        val procedureName = "create_registration_request"

        // Convertir a tipos que puedan representar NULL en JDBC
        val params = mutableListOf<Any?>(
            user_id,
            password,
            role,
            name,
            tel_number,
            email,
            born_date,
            career_cod
        )

        val resultSet = DatabaseDAO.executeStoredProcedureWithNullableParams(procedureName, params)

        return resultSet?.use { rs ->
            if (rs.next()) {
                rs.getInt("id")
            } else {
                throw RuntimeException("No se pudo obtener el ID de la solicitud creada")
            }
        } ?: throw RuntimeException("Error al crear la solicitud de registro")
    }

    fun getRequestById(id: Int): RegistrationRequest_ {
        val procedureName = "get_registration_request_by_id"
        val resultSet = DatabaseDAO.executeStoredProcedureWithResults(procedureName, id)

        return resultSet?.use { rs ->
            if (rs.next()) {
                RegistrationRequest_(
                    id = rs.getInt("id"),
                    user_id = rs.getInt("user_id"),
                    password = rs.getString("password"),
                    role = rs.getString("role"),
                    name = rs.getString("name").takeIf { it.isNotEmpty() },
                    tel_number = rs.getInt("tel_number").takeIf { rs.getInt("tel_number") != 0 },
                    email = rs.getString("email").takeIf { it.isNotEmpty() },
                    born_date = rs.getString("born_date").takeIf { it.isNotEmpty() },
                    career_cod = rs.getInt("career_cod").takeIf { rs.getInt("career_cod") != 0 },
                    status = rs.getString("status"),
                    request_date = rs.getTimestamp("request_date").toString()
                )
            } else {
                throw NoSuchElementException("Solicitud no encontrada")
            }
        } ?: throw RuntimeException("Error al obtener la solicitud de registro")
    }

    fun updateRequestStatus(id: Int, status: String): Boolean {
        val procedureName = "update_registration_request_status"
        return DatabaseDAO.executeStoredProcedure(procedureName, id, status)
    }

    fun getPendingRequests(): List<RegistrationRequest_> {
        val procedureName = "get_pending_registration_requests"

        return try {
            val resultSet = DatabaseDAO.executeStoredProcedureWithResults(procedureName)
                ?: return emptyList() // Si es null, retornar lista vacía

            resultSet.use { rs ->
                val requests = mutableListOf<RegistrationRequest_>()
                while (rs.next()) {
                    requests.add(RegistrationRequest_(
                        id = rs.getInt("id"),
                        user_id = rs.getInt("user_id"),
                        password = rs.getString("password"),
                        role = rs.getString("role"),
                        name = rs.getStringOrNull("name"),
                        tel_number = rs.getIntOrNull("tel_number"),
                        email = rs.getStringOrNull("email"),
                        born_date = rs.getStringOrNull("born_date"),
                        career_cod = rs.getIntOrNull("career_cod"),
                        status = rs.getString("status"),
                        request_date = rs.getString("request_date")
                    ))
                }
                requests
            }
        } catch (e: NoDataException) {
            emptyList() // Retornar lista vacía en lugar de propagar la excepción
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Extensiones para manejar valores nulos
    fun ResultSet.getStringOrNull(column: String): String? {
        return try {
            getString(column).takeIf { !wasNull() }
        } catch (e: SQLException) {
            null
        }
    }

    fun ResultSet.getIntOrNull(column: String): Int? {
        return try {
            val value = getInt(column)
            if (wasNull()) null else value
        } catch (e: SQLException) {
            null
        }
    }
}