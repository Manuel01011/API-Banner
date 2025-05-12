package com.example.backend_banner.backend.Controllers

import com.example.backend_banner.backend.Models.Course_
import com.example.backend_banner.backend.Models.Teacher_
import com.example.backend_banner.backend.service.DatabaseDAO
import java.sql.ResultSet

class TeacherController {

    fun getAllTeachers(): List<Teacher_> {
        val teachers = mutableListOf<Teacher_>()
        val procedureName = "GetAllTeachers"  // Nombre del procedimiento almacenado

        // Llamamos al procedimiento almacenado que devuelve un ResultSet
        val resultSet: ResultSet? = DatabaseDAO.executeStoredProcedureWithResults(procedureName)

        resultSet?.let {
            while (it.next()) {
                // Crear un objeto Teacher a partir del ResultSet
                val teacher = Teacher_(
                    it.getInt("id"),
                    it.getString("name"),
                    it.getInt("tel_number"),
                    it.getString("email")
                )
                teachers.add(teacher)
            }
            it.close() // Cerramos el ResultSet después de usarlo
        }

        return teachers
    }

    fun insertTeacher(id: Int, name: String, telNumber: Int, email: String): Boolean {
        val procedureName = "insert_teacher"
        return DatabaseDAO.executeStoredProcedure(procedureName, id, name, telNumber, email)
    }
    fun updateTeacher(id: Int, name: String, telNumber: Int, email: String): Boolean {
        return DatabaseDAO.executeStoredProcedure("update_teacher", id, name, telNumber, email)
    }

    fun deleteTeacher(id: Int): Boolean {
        return DatabaseDAO.executeStoredProcedure("delete_teacher", id)
    }
    fun callStoredProcedure(procedureName: String, param1: Any, param2: Any): Boolean {
        return DatabaseDAO.executeStoredProcedure(procedureName, param1, param2)
    }

    fun buscar_profesor(nombre: String?, id: Int?): List<Teacher_> {
        val teachers = mutableListOf<Teacher_>()
        val procedureName = "buscar_profesor"
        val resultSet: ResultSet? = DatabaseDAO.executeStoredProcedureWithResults(
            procedureName,
            nombre,
            id
        )

        resultSet?.let {
            while (it.next()) {
                val teacher = Teacher_(
                    it.getInt("id"),
                    it.getString("name"),
                    it.getInt("tel_number"),
                    it.getString("email"),
                )
                teachers.add(teacher)
            }
            it.close()
        }
        return teachers
    }
    fun getProfessorCourses(professorId: Int): List<Course_> {
        val cours = mutableListOf<Course_>()
        val procedureName = "get_professor_courses"
        val resultSet: ResultSet? = DatabaseDAO.executeStoredProcedureWithResults(procedureName, professorId)

        resultSet?.let {
            while (it.next()) {
                val course = Course_(
                    it.getInt("course_cod"),   // course_cod
                    it.getString("course_name"), // course_name
                    it.getInt("credits"),     // credits
                    it.getInt("hours"),       // hours
                    it.getInt("ciclo_id"),    // ciclo_id
                    it.getInt("career_cod")   // career_cod
                )
                cours.add(course)
            }
            it.close()
        }
        return cours
    }
}