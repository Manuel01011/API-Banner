package com.example.backend_banner.backend.Controllers

import com.example.backend_banner.backend.Models.Enrollment_
import com.example.backend_banner.backend.Models.Student_
import com.example.backend_banner.backend.service.DatabaseDAO
import java.sql.ResultSet

class StudentController {

    fun getAllStudents(): List<Student_> {
        val students = mutableListOf<Student_>()
        val procedureName = "GetAllStudents"  // Nombre del procedimiento almacenado

        // Llamamos al procedimiento almacenado que devuelve un ResultSet
        val resultSet: ResultSet? = DatabaseDAO.executeStoredProcedureWithResults(procedureName)

        resultSet?.let {
            while (it.next()) {
                // Crear un objeto Student a partir del ResultSet
                val student = Student_(
                    it.getInt("id"),
                    it.getString("name"),
                    it.getInt("tel_number"),
                    it.getString("email"),
                    it.getString("born_date"),
                    it.getInt("career_cod")
                )
                students.add(student)
            }
            it.close() // Cerramos el ResultSet después de usarlo
        }

        return students
    }

    fun insertStudent(id: Int, name: String, telNumber: Int, email: String, bornDate: String, careerCod: Int): Boolean {
        val procedureName = "insert_student"
        return DatabaseDAO.executeStoredProcedure(procedureName, id, name, telNumber, email, bornDate, careerCod)
    }
    fun updateStudent(id: Int, name: String, telNumber: Int, email: String, bornDate: String, careerCod: Int): Boolean {
        return DatabaseDAO.executeStoredProcedure("update_student", id, name, telNumber, email, bornDate, careerCod)
    }

    fun deleteStudent(id: Int): Boolean {
        return DatabaseDAO.executeStoredProcedure("delete_student", id)
    }
    fun callStoredProcedure(procedureName: String, param1: Any, param2: Any): Boolean {
        return DatabaseDAO.executeStoredProcedure(procedureName, param1, param2)
    }

    fun buscar_alumno(nombre: String?, cod: Int?, carrera :Int?): List<Student_> {
        val students = mutableListOf<Student_>()
        val procedureName = "buscar_alumno"
        val resultSet: ResultSet? = DatabaseDAO.executeStoredProcedureWithResults(
            procedureName,
            nombre,
            cod,
            carrera
        )

        resultSet?.let {
            while (it.next()) {
                val student = Student_(
                    it.getInt("id"),
                    it.getString("name"),
                    it.getInt("tel_number"),
                    it.getString("email"),
                    it.getString("born_date"),
                    it.getInt("career_cod")
                )
                students.add(student)
            }
            it.close()
        }
        return students
    }

    fun alumno_historial(cod: Int?): List<Enrollment_> {
        val enrollments = mutableListOf<Enrollment_>()
        val procedureName = "alumno_historial"
        val resultSet: ResultSet? = DatabaseDAO.executeStoredProcedureWithResults(
            procedureName,
            cod
        )

        resultSet?.let {
            while (it.next()) {
                val enrollment = Enrollment_(
                    it.getInt("student_id"),
                    it.getInt("grupo_id"),
                    it.getDouble("grade")
                )
                enrollments.add(enrollment)
            }
            it.close()
        }
        return enrollments
    }

}
