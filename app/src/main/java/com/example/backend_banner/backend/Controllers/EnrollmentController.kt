package com.example.backend_banner.backend.Controllers

import com.example.backend_banner.backend.Models.Course_
import com.example.backend_banner.backend.Models.Enrollment_
import com.example.backend_banner.backend.service.DatabaseDAO
import java.sql.ResultSet

class EnrollmentController {

    // Obtener todas las matriculas
    fun getAllEnrollments(): List<Enrollment_> {
        val enrollments = mutableListOf<Enrollment_>()
        val procedureName = "GetAllEnrollments"  // Nombre del procedimiento almacenado

        // Llamamos al procedimiento almacenado que devuelve un ResultSet
        val resultSet: ResultSet? = DatabaseDAO.executeStoredProcedureWithResults(procedureName)

        resultSet?.let {
            while (it.next()) {
                // Crear un objeto Enrollment a partir del ResultSet
                val enrollment = Enrollment_(
                    it.getInt("student_id"),
                    it.getInt("grupo_id"),
                    it.getDouble("grade")
                )
                enrollments.add(enrollment)
            }
            it.close() // Cerramos el ResultSet después de usarlo
        }

        return enrollments
    }

    // Insertar una inscripción
    fun insertEnrollment(studentId: Int, grupoId: Int, grade: Double): Boolean {
        val procedureName = "insert_enrollment"
        // Llamar al procedimiento almacenado para insertar la inscripción
        return DatabaseDAO.executeStoredProcedure(procedureName, studentId, grupoId, grade)
    }

    // Eliminar una inscripción
    fun deleteEnrollment(studentId: Int, grupoId: Int): Boolean {
        val procedureName = "delete_enrollment"
        // Llamar al procedimiento almacenado para eliminar la inscripción
        return DatabaseDAO.executeStoredProcedure(procedureName, studentId, grupoId)
    }

    // Cambiar de semester para el estudiante (llama al procedimiento almacenado)
    fun changeCycle(studentId: Int, newCycleId: Int): Boolean {
        val procedureName = "change_cycle"
        // Llamar al procedimiento almacenado para cambiar el semester del estudiante
        return DatabaseDAO.executeStoredProcedure(procedureName, studentId, newCycleId)
    }


    fun updateStudentGrade(studentId: Int, groupId: Int, newGrade: Double): Boolean {
        val procedureName = "update_student_grade"
        return DatabaseDAO.executeStoredProcedure(procedureName, studentId, groupId, newGrade)
    }

    // Obtener los cursos matriculados en el semester activo por un estudiante
    fun getActiveCycleCourses(studentId: Int): List<Course_> {
        val cours = mutableListOf<Course_>()
        val query = "CALL get_active_cycle_courses(?)" // Llamamos al procedimiento almacenado

        val resultSet: ResultSet? = DatabaseDAO.executeQuery(query, studentId)

        resultSet?.let {
            while (it.next()) {
                // Crear el objeto Course con los nuevos parámetros
                cours.add(
                    Course_(
                        it.getInt("cod"),         // Código del curso
                        it.getString("name"),     // Nombre del curso
                        it.getInt("credits"),     // Créditos del curso
                        it.getInt("hours"),       // Horas del curso
                        it.getInt("year"),
                        it.getInt("ciclo_id")
                    )
                )
            }
            it.close()  // Cerramos el ResultSet después de usarlo
        }
        return cours
    }

    fun getStudentAcademicHistory(studentId: Int): List<StudentAcademicHistory> {
        val procedureName = "GetStudentAcademicHistory"
        val resultSet = DatabaseDAO.executeStoredProcedureWithResults(procedureName, studentId)
        val historyItems = mutableListOf<StudentAcademicHistory>()

        resultSet?.let {
            while (it.next()) {
                historyItems.add(
                    StudentAcademicHistory(
                        courseCode = it.getInt("course_cod"),
                        courseName = it.getString("course_name"),
                        credits = it.getInt("credits"),
                        grade = it.getDouble("grade"),
                        cycleYear = it.getInt("ciclo_year"),
                        cycleNumber = it.getInt("ciclo_number"),
                        careerCode = it.getInt("career_cod"),
                        careerName = it.getString("career_name"),
                        groupNumber = it.getInt("number_group"),
                        teacherName = it.getString("teacher_name") ?: "No asignado"
                    )
                )
            }
            it.close()
        }

        return historyItems
    }
}

data class StudentAcademicHistory(
    val courseCode: Int,
    val courseName: String,
    val credits: Int,
    val grade: Double,
    val cycleYear: Int,
    val cycleNumber: Int,
    val careerCode: Int,
    val careerName: String,
    val groupNumber: Int,
    val teacherName: String
) {
    val formattedCycle: String get() = "$cycleYear-$cycleNumber"
    val formattedGrade: String get() = "%.2f".format(grade)
}
