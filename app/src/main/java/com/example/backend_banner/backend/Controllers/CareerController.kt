package com.example.backend_banner.backend.Controllers

import com.example.backend_banner.backend.Models.Career_
import com.example.backend_banner.backend.Models.Course_
import com.example.backend_banner.backend.service.DatabaseDAO
import com.google.gson.Gson
import java.sql.ResultSet

class CareerController {
    fun getAllCareers(): List<Career_> {
        val careers = mutableListOf<Career_>()
        val procedureName = "GetAllCareers"

        val resultSet: ResultSet? = DatabaseDAO.executeStoredProcedureWithResults(procedureName)

        resultSet?.let {
            while (it.next()) {
                val cod = it.getInt("cod")
                val name = it.getString("name")
                val title = it.getString("title")
                careers.add(Career_(cod, name, title))
            }
            it.close()
        }

        return careers
    }

    // Insertar carrera utilizando un procedimiento almacenado
    fun insertCareer(cod: Int, name: String, title: String): Boolean {
        println("Inserting career: $cod, $name, $title")
        val procedureName = "insert_career" // Nombre del procedimiento almacenado
        return DatabaseDAO.executeStoredProcedure(procedureName, cod, name, title)
    }

    fun deleteCareer(cod: Int): Boolean {
        val procedureName = "delete_career"  // Nombre del procedimiento almacenado
        return DatabaseDAO.executeStoredProcedure(procedureName, cod)
    }

    // Llamar a procedimientos almacenados generales
    fun callStoredProcedure(procedureName: String, param1: String, param2: String): Boolean {
        return DatabaseDAO.executeStoredProcedure(procedureName, param1, param2)
    }

    //funcion esperada en Carrer
    fun getCareerByNameAndCode(name: String?, code: Int?): List<Triple<Int, String, String>> {
        val careers = mutableListOf<Triple<Int, String, String>>()
        val query = "buscar_carrera"  // Nombre del procedimiento almacenado

        // Llamamos al procedimiento almacenado y obtenemos el ResultSet
        val resultSet = DatabaseDAO.executeStoredProcedureWithResults(query, name, code)

        // Procesar los resultados
        resultSet?.let {
            while (it.next()) {
                val cod = it.getInt("cod")
                val name = it.getString("name")
                val title = it.getString("title")
                careers.add(Triple(cod, name, title))
            }
            it.close()
        }

        return careers
    }

    fun getCareerCourses(careerCod: Int): List<Course_> {
        val procedureName = "get_career_courses"
        return try {
            DatabaseDAO.executeStoredProcedureForList(
                procedureName,
                Course_::class.java,
                careerCod
            ) as List<Course_>
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun addCourseToCareer(careerCod: Int, courseCod: Int): Boolean {
        val procedureName = "add_course_to_career"
        return DatabaseDAO.executeStoredProcedure(procedureName, careerCod, courseCod)
    }

    fun removeCourseFromCareer(careerCod: Int, courseCod: Int): Boolean {
        val procedureName = "remove_course_from_career"
        return DatabaseDAO.executeStoredProcedure(procedureName, careerCod, courseCod)
    }

    fun getCareerById(careerId: Int): Career_? {
        val procedureName = "get_career_by_id"
        val resultSet = DatabaseDAO.executeStoredProcedureWithResults(procedureName, careerId)

        return resultSet?.let {
            if (it.next()) {
                val cod = it.getInt("cod")
                val name = it.getString("name")
                val title = it.getString("title")
                Career_(cod, name, title)
            } else {
                null
            }
        }
    }

    fun editCareer(
        cod: Int,
        name: String,
        title: String,
        coursesToAdd: List<Int>,
        coursesToRemove: List<Int>
    ): Boolean {
        val procedureName = "edit_career"
        val gson = Gson()

        return try {
            val result = DatabaseDAO.executeStoredProcedureWithResults(
                procedureName,
                cod,
                name,
                title,
                gson.toJson(coursesToAdd),
                gson.toJson(coursesToRemove)
            )

            if (result != null && result.next()) {
                result.getBoolean("result")
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun removeCourseFromCareer(courseId: Int): Boolean {
        return DatabaseDAO.executeStoredProcedure("remove_course_from_career", courseId)
    }

    fun updateCareer(id: Int, name: String, facultyId: Int): Boolean {
        return DatabaseDAO.executeStoredProcedure("update_career", id, name, facultyId)
    }



}
