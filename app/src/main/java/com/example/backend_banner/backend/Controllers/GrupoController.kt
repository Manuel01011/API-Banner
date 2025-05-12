package com.example.backend_banner.backend.Controllers

import com.example.backend_banner.backend.Models.Grupo_
import com.example.backend_banner.backend.service.DatabaseDAO
import java.sql.ResultSet

class GrupoController {

    fun getAllGrupos(): List<Grupo_> {
        val grupos = mutableListOf<Grupo_>()
        val procedureName = "GetAllGrups"  // Nombre del procedimiento almacenado

        // Llamamos al procedimiento almacenado que devuelve un ResultSet
        val resultSet: ResultSet? = DatabaseDAO.executeStoredProcedureWithResults(procedureName)

        resultSet?.let {
            while (it.next()) {
                // Crear un objeto Grupo a partir del ResultSet
                val grupo = Grupo_(
                    it.getInt("id"),
                    it.getInt("number_group"),
                    it.getInt("year"),
                    it.getString("horario"),
                    it.getInt("course_cod"),
                    it.getInt("teacher_id")
                )
                grupos.add(grupo)
            }
            it.close() // Cerramos el ResultSet después de usarlo
        }

        return grupos
    }

    fun insertGrupo(id: Int, numberGroup: Int, year: Int, horario: String, courseCod: Int, teacherId: Int): Boolean {
        val procedureName = "insert_grupo"
        return DatabaseDAO.executeStoredProcedure(procedureName, id, numberGroup, year, horario, courseCod, teacherId)
    }

    fun updateGrupo(id: Int, numberGroup: Int, year: Int, horario: String, courseCod: Int, teacherId: Int): Boolean {
        val procedureName = "update_grupo"
        return DatabaseDAO.executeStoredProcedure(procedureName, id, numberGroup, year, horario, courseCod, teacherId)
    }

    fun deleteGrupo(id: Int): Boolean {
        val procedureName = "delete_grupo"
        return DatabaseDAO.executeStoredProcedure(procedureName, id)
    }

    fun callStoredProcedure(procedureName: String, param1: Any, param2: Any): Boolean {
        return DatabaseDAO.executeStoredProcedure(procedureName, param1, param2)
    }

    fun getGroupsByCourse(courseId: Int): List<Grupo_> {
        val procedureName = "get_groups_by_course"

        // Llamar al procedimiento almacenado y obtener el ResultSet
        val resultSet: ResultSet? = DatabaseDAO.executeStoredProcedureWithResults(procedureName, courseId)

        val groups = mutableListOf<Grupo_>()
        resultSet?.use { rs ->
            while (rs.next()) {
                val group = Grupo_(
                    rs.getInt("id"),
                    rs.getInt("number_group"),
                    rs.getInt("year"),
                    rs.getString("horario"),
                    rs.getInt("course_cod"),
                    rs.getInt("teacher_id")
                )
                groups.add(group)
            }
        }

        return groups
    }

    fun addOrUpdateGrupo(id: Int, numberGroup: Int, year: Int, horario: String, courseCod: Int, teacherId: Int): Boolean {
        return DatabaseDAO.executeStoredProcedure(
            "add_or_update_group",
            id, numberGroup, year, horario, courseCod, teacherId
        )
    }
}
