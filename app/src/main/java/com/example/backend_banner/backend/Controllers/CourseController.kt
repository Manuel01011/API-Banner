package com.example.backend_banner.backend.Controllers

import com.example.backend_banner.backend.Models.Course_
import com.example.backend_banner.backend.service.DatabaseDAO
import java.sql.ResultSet

class CourseController {

    fun getAllCourses(): List<Course_> {
        val cours = mutableListOf<Course_>()
        val procedureName = "GetAllCourses"  // Nombre del procedimiento almacenado

        // Llamamos al procedimiento almacenado que devuelve un ResultSet
        val resultSet: ResultSet? = DatabaseDAO.executeStoredProcedureWithResults(procedureName)

        resultSet?.let {
            while (it.next()) {
                val course = Course_(
                    it.getInt("cod"),
                    it.getString("name"),
                    it.getInt("credits"),
                    it.getInt("hours"),
                    it.getInt("ciclo_id")
                )
                cours.add(course)
            }
            it.close()
        }

        return cours
    }

    fun getCoursesNotAssignedToCareer(careerCod: Int): List<Course_> {
        val procedureName = "get_courses_not_assigned_to_career"
        val courses = mutableListOf<Course_>()
        val resultSet = DatabaseDAO.executeStoredProcedureWithResults(procedureName, careerCod)

        resultSet?.use { rs ->
            while (rs.next()) {
                val course = Course_(
                    rs.getInt("cod"),
                    rs.getString("name"),
                    rs.getInt("credits"),
                    rs.getInt("hours"),
                    rs.getInt("ciclo_id")
                )
                courses.add(course)
            }
        }
        return courses
    }

    fun insertCourse(cod: Int, name: String, credits: Int, hours: Int, cicloId: Int): Boolean {
        val procedureName = "insert_course"
        return DatabaseDAO.executeStoredProcedure(procedureName, cod, name, credits, hours, cicloId)
    }
    fun updateCourse(cod: Int, name: String, credits: Int, hours: Int, cicloId: Int): Boolean {
        return DatabaseDAO.executeStoredProcedure("update_course", cod, name, credits, hours, cicloId)
    }

    fun deleteCourse(cod: Int): Boolean {
        return DatabaseDAO.executeStoredProcedure("delete_course", cod)
    }
    fun callStoredProcedure(procedureName: String, param1: Any, param2: Any): Boolean {
        return DatabaseDAO.executeStoredProcedure(procedureName, param1, param2)
    }
    //funcionalidad esperada en el CourseController
    fun searchCourses(nombre: String?, codigo: Int?, carreraCod: Int?): List<Course_> {
        val cours = mutableListOf<Course_>()
        val procedureName = "BuscarCurso"
        val resultSet: ResultSet? = DatabaseDAO.executeStoredProcedureWithResults(
            procedureName,
            nombre,
            codigo,
            carreraCod
        )

        resultSet?.let {
            while (it.next()) {
                val course = Course_(
                    it.getInt("cod"),
                    it.getString("name"),
                    it.getInt("credits"),
                    it.getInt("hours"),
                    it.getInt("ciclo_id")
                )
                cours.add(course)
            }
            it.close()
        }
        return cours
    }

    fun getCoursesByCareerAndCycle(careerCod: Int, cicloId: Int): List<Course_> {
        val procedureName = "get_courses_by_career_and_cycle"
        val resultSet: ResultSet? = DatabaseDAO.executeStoredProcedureWithResults(procedureName, careerCod, cicloId)

        val cours = mutableListOf<Course_>()
        resultSet?.use { rs ->
            while (rs.next()) {
                val course = Course_(
                    rs.getInt("cod"),
                    rs.getString("name"),
                    rs.getInt("credits"),
                    rs.getInt("hours"),
                    cicloId
                )
                cours.add(course)
            }
        }
        return cours
    }
}