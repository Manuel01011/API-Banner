package com.example.banner.backend
import com.example.backend_banner.backend.Models.Career_
import com.example.backend_banner.backend.Controllers.CareerController
import com.example.backend_banner.backend.Controllers.CicloController
import com.example.backend_banner.backend.Controllers.CourseController
import com.example.backend_banner.backend.Controllers.EnrollmentController
import com.example.backend_banner.backend.Controllers.GrupoController
import com.example.backend_banner.backend.Controllers.RegistrationRequestController
import com.example.backend_banner.backend.Controllers.StudentController
import com.example.backend_banner.backend.Controllers.TeacherController
import com.example.backend_banner.backend.Models.Ciclo_
import com.example.backend_banner.backend.Models.Course_
import com.example.backend_banner.backend.Models.Enrollment_
import com.example.backend_banner.backend.Models.Grupo_
import com.example.backend_banner.backend.Models.RegistrationRequest_
import com.example.backend_banner.backend.Models.Student_
import com.example.backend_banner.backend.Models.Teacher_
import com.example.backend_banner.backend.Models.Usuario_
import com.example.banner.backend.Controllers.UserController
import com.google.android.gms.identitycredentials.RegistrationRequest
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.json.JSONObject
import java.net.ServerSocket
import java.net.Socket
import java.io.*
import com.google.gson.reflect.TypeToken

class SimpleHttpServer(private val port: Int) {
    private val gson = Gson()
    private val careerController = CareerController()
    private val cicloController = CicloController()
    private val courseController = CourseController()
    private val enrollmentController = EnrollmentController()
    private val groupController = GrupoController()
    private val studentController = StudentController()
    private val teacherController = TeacherController()
    private val userController = UserController()
    private val registrationRequestController = RegistrationRequestController()

    fun start() {
        Thread {
            val serverSocket = ServerSocket(port)
            println("Servidor iniciado en puerto $port")

            while (true) {
                try {
                    val clientSocket = serverSocket.accept()
                    handleRequest(clientSocket)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    private fun handleRequest(clientSocket: Socket) {
        var reader: BufferedReader? = null
        var writer: PrintWriter? = null

        try {
            reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            writer = PrintWriter(clientSocket.getOutputStream(), true)

            // Leer primera línea (request line)
            val requestLine = reader.readLine() ?: run {
                writer.println("HTTP/1.1 400 Bad Request")
                writer.println()
                return
            }

            val requestParts = requestLine.split(" ")
            if (requestParts.size < 2) {
                writer.println("HTTP/1.1 400 Bad Request")
                writer.println()
                return
            }

            val method = requestParts[0]
            val path = requestParts[1]

            // Leer headers para obtener Content-Length si existe
            var contentLength = 0
            while (true) {
                val headerLine = reader.readLine() ?: break
                if (headerLine.isEmpty()) break // Fin de headers

                if (headerLine.startsWith("Content-Length:")) {
                    contentLength = headerLine.substringAfter(":").trim().toIntOrNull() ?: 0
                }
            }

            // Leer el cuerpo solo si hay contenido
            val body = if (contentLength > 0) {
                val buffer = CharArray(contentLength)
                reader.read(buffer, 0, contentLength)
                String(buffer)
            } else {
                ""
            }

            when {
                method == "OPTIONS" -> {
                    writer.println("HTTP/1.1 200 OK")
                    writer.println("Access-Control-Allow-Origin: *")
                    writer.println("Access-Control-Allow-Methods: POST, GET, PUT, DELETE, OPTIONS")
                    writer.println("Access-Control-Allow-Headers: Content-Type")
                    writer.println("Connection: close")
                    writer.println()
                    return
                }
                // Rutas para la entidad "Carreras"
                path.equals("/api/careers", ignoreCase = true) && method == "GET" -> {
                    handleGetCareers(writer)
                }
                path.startsWith("/api/careers/") && method == "DELETE" -> {
                    val id = path.removePrefix("/api/careers/").toIntOrNull()
                    handleDeleteCareer(writer, id)
                }
                path.equals("/api/careers", ignoreCase = true) && method == "POST" -> {
                    handleCreateCareer(writer, body)
                }
                path.equals("/api/careers", ignoreCase = true) && method == "PUT" -> {
                    handleUpdateCareer(writer, body)
                }
                // Agregar curso a carrera
                path.equals("/api/careers/courses", ignoreCase = true) && method == "POST" -> {
                    handleAddCourseToCareer(writer, body)
                }
                // Eliminar curso de carrera
                path.equals("/api/careers/courses", ignoreCase = true) && method == "DELETE" -> {
                    handleRemoveCourseFromCareer(writer, body)
                }

                // Rutas para la entidad "ciclos"
                path.equals("/api/ciclos", ignoreCase = true) && method == "GET" -> {
                    handleGetCiclos(writer)
                }
                path.startsWith("/api/ciclos/") && method == "DELETE" -> {
                    val id = path.removePrefix("/api/ciclos/").toIntOrNull()
                    handleDeleteCiclo(writer, id)
                }
                path.equals("/api/ciclos", ignoreCase = true) && method == "POST" -> {
                    handleCreateCiclo(writer, body)
                }
                path.equals("/api/ciclos", ignoreCase = true) && method == "PUT" -> {
                    handleUpdateCiclo(writer, body)
                }

                //Rutas para la entidad "cursos"
                path.equals("/api/courses", ignoreCase = true) && method == "GET" -> {
                    handleGetCourses(writer)
                }
                path.startsWith("/api/courses/") && method == "DELETE" -> {
                    val id = path.removePrefix("/api/courses/").toIntOrNull()
                    handleDeleteCourse(writer, id)
                }
                path.equals("/api/courses", ignoreCase = true) && method == "POST" -> {
                    handleCreateCourse(writer, body)
                }
                path.equals("/api/courses", ignoreCase = true) && method == "PUT" -> {
                    handleUpdateCourse(writer, body)
                }
                path.startsWith("/api/courses/not_assigned/") && method == "GET" -> {
                    val careerId = path.removePrefix("/api/courses/not_assigned/").toIntOrNull()
                    if (careerId != null) {
                        handleGetNotAssignedCourses(writer, careerId)
                    } else {
                        sendErrorResponse(writer, "ID de carrera inválido")
                    }
                }

                //Rutas para la entidad "matriculas"
                path.equals("/api/enrollments", ignoreCase = true) && method == "GET" -> {
                    handleGetEnrollments(writer)
                }
                path.startsWith("/api/enrollments/") && method == "DELETE" -> {
                    val parts = path.removePrefix("/api/enrollments/").split("/")
                    if (parts.size == 2) {
                        val studentId = parts[0].toIntOrNull()
                        val grupoId = parts[1].toIntOrNull()
                        handleDeleteEnrollment(writer, studentId, grupoId)
                    } else {
                        sendErrorResponse(writer, "Formato de URL inválido")
                    }
                }
                path.equals("/api/enrollments", ignoreCase = true) && method == "POST" -> {
                    handleCreateEnrollment(writer, body)
                }
                path.equals("/api/enrollments", ignoreCase = true) && method == "PUT" -> {
                    handleUpdateEnrollment(writer, body)
                }

                //Rutas para la entidad Group
                path.equals("/api/groups", ignoreCase = true) && method == "GET" -> {
                    handleGetGroups(writer)
                }
                path.startsWith("/api/groups/") && method == "DELETE" -> {
                    val id = path.removePrefix("/api/groups/").toIntOrNull()
                    handleDeleteGroup(writer, id)
                }
                path.equals("/api/groups", ignoreCase = true) && method == "POST" -> {
                    handleCreateGroup(writer, body)
                }
                path.equals("/api/groups", ignoreCase = true) && method == "PUT" -> {
                    handleUpdateGroup(writer, body)
                }

                //Rutas para la entidad "Student"
                path.equals("/api/students", ignoreCase = true) && method == "GET" -> {
                    handleGetStudents(writer)
                }
                path.startsWith("/api/students/") && method == "DELETE" -> {
                    val id = path.removePrefix("/api/students/").toIntOrNull()
                    handleDeleteStudent(writer, id)
                }
                path.equals("/api/students", ignoreCase = true) && method == "POST" -> {
                    handleCreateStudent(writer, body)
                }
                path.equals("/api/students", ignoreCase = true) && method == "PUT" -> {
                    handleUpdateStudent(writer, body)
                }
                path.startsWith("/api/students/academic-history") && method == "GET" -> {
                    // Obtener parámetros de la URL
                    val query = path.substringAfter("?", "")
                    val studentId = query.split("&")
                        .find { it.startsWith("studentId=") }
                        ?.substringAfter("=")
                        ?.toIntOrNull()

                    if (studentId != null) {
                        handleGetStudentAcademicHistory(writer, studentId)
                    } else {
                        sendErrorResponse(writer, "Se requiere studentId como parámetro en la URL")
                    }
                }

                //Rutas para la entidad Tecaher
                path.equals("/api/teachers", ignoreCase = true) && method == "GET" -> {
                    handleGetTeachers(writer)
                }
                path.startsWith("/api/teachers/") && method == "DELETE" -> {
                    val id = path.removePrefix("/api/teachers/").toIntOrNull()
                    handleDeleteTeacher(writer, id)
                }
                path.equals("/api/teachers", ignoreCase = true) && method == "POST" -> {
                    handleCreateTeacher(writer, body)
                }
                path.equals("/api/teachers", ignoreCase = true) && method == "PUT" -> {
                    handleUpdateTeacher(writer, body)
                }
                path.startsWith("/api/careers/") && path.endsWith("/courses") && method == "GET" -> {
                    handleGetCareerCourses(writer, path)
                }

                path.startsWith("/api/teachers/") && path.endsWith("/groups") && method == "GET" -> {
                    val teacherId = path.removePrefix("/api/teachers/").removeSuffix("/groups").toIntOrNull()
                    handleGetTeacherGroups(writer, teacherId)
                }
                path.startsWith("/api/groups/") && path.endsWith("/enrollments") && method == "GET" -> {
                    val groupId = path.removePrefix("/api/groups/").removeSuffix("/enrollments").toIntOrNull()
                    handleGetGroupEnrollments(writer, groupId)
                }

                //Rutas para la entidad User
                path.equals("/api/users", ignoreCase = true) && method == "GET" -> {
                    handleGetUsers(writer)
                }
                path.startsWith("/api/users/") && method == "DELETE" -> {
                    val id = path.removePrefix("/api/users/").toIntOrNull()
                    handleDeleteUser(writer, id)
                }
                path.equals("/api/users", ignoreCase = true) && method == "POST" -> {
                    handleCreateUser(writer, body)
                }
                path.equals("/api/users", ignoreCase = true) && method == "PUT" -> {
                    handleUpdateUser(writer, body)
                }
                //Ruta del login
                path.equals("/api/login", ignoreCase = true) && method == "POST" -> {
                    handleLogin(writer, body)
                }
                //rutas de registro
                // Nueva ruta para crear solicitudes de registro
                path.equals("/api/registration-requests", ignoreCase = true) && method == "POST" -> {
                    handleCreateRegistrationRequest(writer, body)
                }
                // Nueva ruta para obtener solicitudes pendientes
                path.equals("/api/registration-requests/pending", ignoreCase = true) && method == "GET" -> {
                    handleGetPendingRegistrationRequests(writer)
                }
                // Nueva ruta para aprobar solicitud
                path.matches(Regex("/api/registration-requests/\\d+/approve", RegexOption.IGNORE_CASE)) && method == "POST" -> {
                    val requestId = path.split("/")[3].toIntOrNull()
                    if (requestId != null) {
                        handleApproveRegistrationRequest(writer, requestId)
                    } else {
                        sendErrorResponse(writer, "ID de solicitud inválido")
                    }
                }
                // Nueva ruta para rechazar solicitud
                path.matches(Regex("/api/registration-requests/\\d+/reject", RegexOption.IGNORE_CASE)) && method == "POST" -> {
                    val requestId = path.split("/")[3].toIntOrNull()
                    if (requestId != null) {
                        handleRejectRegistrationRequest(writer, requestId)
                    } else {
                        sendErrorResponse(writer, "ID de solicitud inválido")
                    }
                }
                else -> {
                    writer.println("HTTP/1.1 404 Not Found")
                    writer.println()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            writer?.println("HTTP/1.1 500 Internal Server Error")
            writer?.println()
        } finally {
            reader?.close()
            writer?.close()
            try {
                clientSocket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun readRequestBody(reader: BufferedReader): String {
        val body = StringBuilder()
        try {
            // Leer headers hasta encontrar la línea vacía
            while (true) {
                val line = reader.readLine() ?: break
                if (line.isEmpty()) break // Fin de headers
            }

            // Leer el cuerpo
            while (reader.ready()) {
                val line = reader.readLine() ?: break
                body.append(line)
            }
        } catch (e: IOException) {
            println("Error reading request body: ${e.message}")
        }
        return body.toString()
    }

    // -------------------------Manejo de solicitudes de la entidad "carreras"-------------------------
    private fun handleGetCareers(writer: PrintWriter) {
        val careers = careerController.getAllCareers()
        val response = mapOf(
            "status" to "success",
            "data" to careers
        )
        sendJsonResponse(writer, response)
    }

    private fun handleDeleteCareer(writer: PrintWriter, id: Int?) {
        if (id == null) {
            sendErrorResponse(writer, "ID inválido")
            return
        }

        val success = careerController.deleteCareer(id)
        if (success) {
            sendJsonResponse(writer, mapOf("success" to true))
        } else {
            sendErrorResponse(writer, "Error al eliminar carrera")
        }
    }

    private fun handleCreateCareer(writer: PrintWriter, body: String) {
        try {
            println("Received POST request with body: $body")
            val career = gson.fromJson(body, Career_::class.java)
            println("Inserting career: ${career.cod}, ${career.name}, ${career.title}")
            val success = careerController.insertCareer(career.cod, career.name, career.title)

            // Construir respuesta manualmente para mayor control
            val response = """
            HTTP/1.1 ${if (success) 200 else 400}
            Content-Type: application/json
            Access-Control-Allow-Origin: *
            Access-Control-Allow-Methods: POST, GET, OPTIONS, PUT, DELETE
            Access-Control-Allow-Headers: Content-Type
            Connection: close
            
            ${gson.toJson(mapOf(
                "success" to success,
                "cod" to career.cod,
                "name" to career.name,
                "title" to career.title
            ))}
        """.trimIndent()

            writer.println(response)
            writer.flush() // Asegurar que se envía todo

        } catch (e: Exception) {
            println("Error in handleCreateCareer: ${e.message}")
            val errorResponse = """
            HTTP/1.1 400
            Content-Type: application/json
            
            {"error":"Invalid data format"}
        """.trimIndent()
            writer.println(errorResponse)
            writer.flush()
        }
    }

    fun handleUpdateCareer(writer: PrintWriter, body: String) {
        val gson = Gson()
        try {
            println("Raw update request body: $body")

            // Primero, eliminar las comillas exteriores si existen
            val cleanBody = body.removeSurrounding("\"")
            // Luego, reemplazar las secuencias de escape
            val unescapedBody = cleanBody.replace("\\\"", "\"")
                .replace("\\n", "")
                .replace("\\", "")

            println("Cleaned request body: $unescapedBody")

            val jsonObject = JsonParser.parseString(unescapedBody).asJsonObject
            val careerJson = jsonObject.getAsJsonObject("career")

            val cod = careerJson.get("cod").asInt
            val name = careerJson.get("name").asString
            val title = careerJson.get("title").asString

            val coursesToAdd = jsonObject.getAsJsonArray("coursesToAdd").map { it.asInt }
            val coursesToRemove = jsonObject.getAsJsonArray("coursesToRemove").map { it.asInt }

            println("Parsed data - Cod: $cod, Name: $name, Title: $title")
            println("Courses to add: $coursesToAdd")
            println("Courses to remove: $coursesToRemove")

            val result = careerController.editCareer(cod, name, title, coursesToAdd, coursesToRemove)

            val response = JsonObject().apply {
                addProperty("success", result)
                if (!result) addProperty("message", "No se pudo actualizar la carrera")
            }

            writer.println("HTTP/1.1 ${if (result) 200 else 400}")
            writer.println("Content-Type: application/json")
            writer.println()
            writer.println(response.toString())

        } catch (e: Exception) {
            println("Error in handleUpdateCareer: ${e.stackTraceToString()}")

            val errorResponse = JsonObject().apply {
                addProperty("success", false)
                addProperty("message", "Error en el formato de los datos: ${e.message}")
            }

            writer.println("HTTP/1.1 400")
            writer.println("Content-Type: application/json")
            writer.println()
            writer.println(errorResponse.toString())
        }
    }

    private fun handleAddCourseToCareer(writer: PrintWriter, body: String) {
        try {
            val json = JSONObject(body)
            val careerCod = json.getInt("careerCod")
            val courseCod = json.getInt("courseCod")

            val success = careerController.addCourseToCareer(careerCod, courseCod)
            if (success) {
                sendJsonResponse(writer, mapOf("success" to true))
            } else {
                sendErrorResponse(writer, "Error adding course to career")
            }
        } catch (e: Exception) {
            sendErrorResponse(writer, "Invalid data")
        }
    }

    private fun handleRemoveCourseFromCareer(writer: PrintWriter, body: String) {
        try {
            val json = JSONObject(body)
            val careerCod = json.getInt("careerCod")
            val courseCod = json.getInt("courseCod")

            val success = careerController.removeCourseFromCareer(careerCod, courseCod)
            if (success) {
                sendJsonResponse(writer, mapOf("success" to true))
            } else {
                sendErrorResponse(writer, "Error removing course from career")
            }
        } catch (e: Exception) {
            sendErrorResponse(writer, "Invalid data")
        }
    }
    // ---------------------------Fin del manejo de solicitudes de la entidad "carreras"-------------------------


    // -------------------------Manejo de solicitudes de la entidad "ciclos"-------------------------
    private fun handleGetCiclos(writer: PrintWriter) {
        val ciclos = cicloController.getAllCiclos()
        val response = mapOf(
            "status" to "success",
            "data" to ciclos
        )
        sendJsonResponse(writer, response)
    }

    private fun handleDeleteCiclo(writer: PrintWriter, id: Int?) {
        if (id == null) {
            sendErrorResponse(writer, "ID inválido")
            return
        }
        val success = cicloController.deleteCiclo(id)
        if (success) {
            sendJsonResponse(writer, mapOf("success" to true))
        } else {
            sendErrorResponse(writer, "Error al eliminar ciclo")
        }
    }

    private fun handleCreateCiclo(writer: PrintWriter, body: String) {
        try {
            println("Received POST request with body: $body")
            val ciclo = gson.fromJson(body, Ciclo_::class.java)
            println("Inserting ciclo: ${ciclo.id}, ${ciclo.year}, ${ciclo.number}")

            val success = cicloController.insertCiclo(
                ciclo.id,
                ciclo.year,
                ciclo.number,
                ciclo.dateStart,
                ciclo.dateFinish,
            )

            val response = """
            HTTP/1.1 ${if (success) 200 else 400}
            Content-Type: application/json
            Access-Control-Allow-Origin: *
            Access-Control-Allow-Methods: POST, GET, OPTIONS, PUT, DELETE
            Access-Control-Allow-Headers: Content-Type
            Connection: close
            
            ${gson.toJson(mapOf(
                "success" to success,
                "id" to ciclo.id,
                "year" to ciclo.year,
                "number" to ciclo.number
            ))}
        """.trimIndent()

            writer.println(response)
            writer.flush()

        } catch (e: Exception) {
            println("Error in handleCreateCiclo: ${e.message}")
            val errorResponse = """
            HTTP/1.1 400
            Content-Type: application/json
            
            {"error":"Invalid data format"}
        """.trimIndent()
            writer.println(errorResponse)
            writer.flush()
        }
    }

    private fun handleUpdateCiclo(writer: PrintWriter, body: String) {
        try {
            println("Received PUT request with body: $body")
            val ciclo = gson.fromJson(body, Ciclo_::class.java)
            println("Updating ciclo: ${ciclo.id}, ${ciclo.year}, ${ciclo.number}")

            val success = cicloController.updateCiclo(
                ciclo.id,
                ciclo.year,
                ciclo.number,
                ciclo.dateStart,
                ciclo.dateFinish,
                ciclo.is_active
            )

            val response = """
            HTTP/1.1 ${if (success) 200 else 400}
            Content-Type: application/json
            Access-Control-Allow-Origin: *
            Access-Control-Allow-Methods: POST, GET, OPTIONS, PUT, DELETE
            Access-Control-Allow-Headers: Content-Type
            Connection: close
            
            ${gson.toJson(mapOf(
                "success" to success,
                "message" to if (success) "Cycle updated successfull" else "Error al actualizar ciclo",
                "data" to ciclo
            ))}
        """.trimIndent()

            writer.println(response)
            writer.flush()

        } catch (e: Exception) {
            println("Error in handleUpdateCiclo: ${e.message}")
            val errorResponse = """
            HTTP/1.1 500
            Content-Type: application/json
            
            ${gson.toJson(mapOf(
                "error" to "Internal server error",
                "message" to e.message
            ))}
        """.trimIndent()
            writer.println(errorResponse)
            writer.flush()
        }
    }
    // ---------------------------Fin del manejo de solicitudes de la entidad "ciclos"-------------------------


    // -------------------------Manejo de solicitudes de la entidad "cursos"-------------------------
    private fun handleGetCourses(writer: PrintWriter) {
        val courses = courseController.getAllCourses()
        val response = mapOf(
            "status" to "success",
            "data" to courses
        )
        sendJsonResponse(writer, response)
    }
    private fun handleDeleteCourse(writer: PrintWriter, id: Int?) {
        if (id == null) {
            sendErrorResponse(writer, "ID inválido")
            return
        }
        val success = courseController.deleteCourse(id)
        if (success) {
            sendJsonResponse(writer, mapOf("success" to true))
        } else {
            sendErrorResponse(writer, "Error al eliminar curso")
        }
    }

    private fun handleGetNotAssignedCourses(writer: PrintWriter, careerId: Int) {
        try {
            val courses = courseController.getCoursesNotAssignedToCareer(careerId)
            val response = mapOf(
                "status" to "success",
                "data" to courses
            )
            sendJsonResponse(writer, response)
        } catch (e: Exception) {
            sendErrorResponse(writer, "Error obteniendo cursos no asignados")
        }
    }

    private fun handleCreateCourse(writer: PrintWriter, body: String) {
        try {
            println("Received POST request with body: $body")
            val course = gson.fromJson(body, Course_::class.java)
            println("Inserting course: ${course.cod}, ${course.name}")

            val success = courseController.insertCourse(
                course.cod,
                course.name,
                course.credits,
                course.hours,
                course.cicloId
            )

            val response = """
            HTTP/1.1 ${if (success) 200 else 400}
            Content-Type: application/json
            Access-Control-Allow-Origin: *
            Access-Control-Allow-Methods: POST, GET, OPTIONS, PUT, DELETE
            Access-Control-Allow-Headers: Content-Type
            Connection: close
            
            ${gson.toJson(mapOf(
                "success" to success,
                "cod" to course.cod,
                "name" to course.name
            ))}
        """.trimIndent()

            writer.println(response)
            writer.flush()

        } catch (e: Exception) {
            println("Error in handleCreateCourse: ${e.message}")
            val errorResponse = """
            HTTP/1.1 400
            Content-Type: application/json
            
            {"error":"Invalid data format"}
        """.trimIndent()
            writer.println(errorResponse)
            writer.flush()
        }
    }

    private fun handleUpdateCourse(writer: PrintWriter, body: String) {
        try {
            println("Received PUT request with body: $body")
            val course = gson.fromJson(body, Course_::class.java)
            println("Updating course: ${course.cod}, ${course.name}")

            val success = courseController.updateCourse(
                course.cod,
                course.name,
                course.credits,
                course.hours,
                course.cicloId
            )

            val response = """
            HTTP/1.1 ${if (success) 200 else 400}
            Content-Type: application/json
            Access-Control-Allow-Origin: *
            Access-Control-Allow-Methods: POST, GET, OPTIONS, PUT, DELETE
            Access-Control-Allow-Headers: Content-Type
            Connection: close
            
            ${gson.toJson(mapOf(
                "success" to success,
                "cod" to course.cod,
                "name" to course.name
            ))}
        """.trimIndent()

            writer.println(response)
            writer.flush()

        } catch (e: Exception) {
            println("Error in handleUpdateCourse: ${e.message}")
            val errorResponse = """
            HTTP/1.1 400
            Content-Type: application/json
            
            {"error":"Invalid data format"}
        """.trimIndent()
            writer.println(errorResponse)
            writer.flush()
        }
    }
    // ---------------------------Fin del manejo de solicitudes de la entidad "cursos"-------------------------

    // -------------------------Manejo de solicitudes de la entidad "matriculas"-------------------------
    private fun handleGetEnrollments(writer: PrintWriter) {
        val enrollments = enrollmentController.getAllEnrollments()
        val response = mapOf(
            "status" to "success",
            "data" to enrollments
        )
        sendJsonResponse(writer, response)
    }

    private fun handleDeleteEnrollment(writer: PrintWriter, studentId: Int?, grupoId: Int?) {
        if (studentId == null || grupoId == null) {
            sendErrorResponse(writer, "Invalid id")
            return
        }
        try {
            val success = enrollmentController.deleteEnrollment(studentId, grupoId)
            if (success) {
                sendJsonResponse(writer, mapOf("success" to true))
            } else {
                sendErrorResponse(writer, "Error deleting the enrollment")
            }
        } catch (e: Exception) {
            sendErrorResponse(writer, "Error interno del servidor: ${e.message}")
        }
    }

    private fun handleCreateEnrollment(writer: PrintWriter, body: String) {
        try {
            val enrollment = gson.fromJson(body, Enrollment_::class.java)

            val success = enrollmentController.insertEnrollment(
                enrollment.studentId,
                enrollment.grupoId,
                enrollment.grade
            )


            val response = """
            HTTP/1.1 ${if (success) 200 else 400}
            Content-Type: application/json
            Access-Control-Allow-Origin: *
            Access-Control-Allow-Methods: POST, GET, OPTIONS, PUT, DELETE
            Access-Control-Allow-Headers: Content-Type
            Connection: close
            
            ${gson.toJson(mapOf(
                "success" to success,
                "message" to if (success) "Enrollment successfully created" else "Error al crear matrícula",
                "data" to enrollment
            ))}
        """.trimIndent()

            writer.println(response)
            writer.flush()

        } catch (e: Exception) {
            val errorResponse = """
            HTTP/1.1 500
            Content-Type: application/json
            
            ${gson.toJson(mapOf(
                "error" to "Internal server error",
                "message" to e.message
            ))}
        """.trimIndent()
            writer.println(errorResponse)
            writer.flush()
        }
    }

    private fun handleUpdateEnrollment(writer: PrintWriter, body: String) {
        try {
            println("Raw body received: $body")
            val enrollment = gson.fromJson(body, Enrollment_::class.java)
            println("Parsed enrollment: StudentID=${enrollment.studentId}, GrupoID=${enrollment.grupoId}, Grade=${enrollment.grade}")

            val success = enrollmentController.updateStudentGrade(
                enrollment.studentId,
                enrollment.grupoId,
                enrollment.grade
            )

            println("Update operation result: $success")

            // Respuesta más consistente
            val response = if (success) {
                """
            HTTP/1.1 200 OK
            Content-Type: application/json
            Access-Control-Allow-Origin: *
            Access-Control-Allow-Methods: POST, GET, OPTIONS, PUT, DELETE
            Access-Control-Allow-Headers: Content-Type
            Connection: close
            
            ${gson.toJson(mapOf(
                    "success" to true,
                    "message" to "Enrollment updated successfully",
                    "data" to enrollment
                ))}
            """.trimIndent()
            } else {
                """
            HTTP/1.1 400 Bad Request
            Content-Type: application/json
            Access-Control-Allow-Origin: *
            Access-Control-Allow-Methods: POST, GET, OPTIONS, PUT, DELETE
            Access-Control-Allow-Headers: Content-Type
            Connection: close
            
            ${gson.toJson(mapOf(
                    "success" to false,
                    "error" to "Failed to update enrollment"
                ))}
            """.trimIndent()
            }

            writer.println(response)
            writer.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            val errorResponse = """
            HTTP/1.1 500 Internal Server Error
            Content-Type: application/json
            Access-Control-Allow-Origin: *
            Access-Control-Allow-Methods: POST, GET, OPTIONS, PUT, DELETE
            Access-Control-Allow-Headers: Content-Type
            Connection: close
            
            ${gson.toJson(mapOf(
                "success" to false,
                "error" to "Internal server error: ${e.message}",
                "stackTrace" to e.stackTraceToString()
            ))}
        """.trimIndent()
            writer.println(errorResponse)
            writer.flush()
        }
    }

    private fun handleGetStudentAcademicHistory(writer: PrintWriter, studentId: Int) {
        try {
            val history = enrollmentController.getStudentAcademicHistory(studentId)
            val historyData = history.map {
                mapOf(
                    "course" to mapOf(
                        "code" to it.courseCode,
                        "name" to it.courseName,
                        "credits" to it.credits
                    ),
                    "grade" to it.grade,
                    "cycle" to mapOf(
                        "year" to it.cycleYear,
                        "number" to it.cycleNumber
                    ),
                    "career" to mapOf(
                        "name" to it.careerName
                    ),
                    "group" to mapOf(
                        "number" to it.groupNumber
                    ),
                    "teacher" to it.teacherName
                )
            }

            val response = mapOf(
                "status" to "success",
                "data" to historyData
            )
            sendJsonResponse(writer, response)
        } catch (e: Exception) {
            sendErrorResponse(writer, "Error al obtener historial académico: ${e.message}")
        }
    }


   // ---------------------------Fin del manejo de solicitudes de la entidad "matriculas"-------------------------

    // -------------------------Manejo de solicitud de entidad Group-------------------------
    private fun handleGetGroups(writer: PrintWriter) {
        val groups = groupController.getAllGrupos()
        val response = mapOf(
            "status" to "success",
            "data" to groups
        )
        sendJsonResponse(writer, response)
    }

    private fun handleDeleteGroup(writer: PrintWriter, id: Int?) {
        println("DELETE group request received for id: $id")
        if (id == null) {
            println("Invalid ID received")
            sendErrorResponse(writer, "ID inválido")
            return
        }
        try {
            val success = groupController.deleteGrupo(id)
            println("Delete operation result: $success")

            if (success) {
                sendJsonResponse(writer, mapOf("success" to true))
            } else {
                sendErrorResponse(writer, "Error deleting group")
            }
        } catch (e: Exception) {
            println("Error deleting group: ${e.message}")
            e.printStackTrace()
            sendErrorResponse(writer, "Error interno del servidor: ${e.message}")
        }
    }

    private fun handleCreateGroup(writer: PrintWriter, body: String) {
        println("POST /api/groups request received with body: $body")

        try {
            val group = gson.fromJson(body, Grupo_::class.java)
            println("Creating group: $group")

            // Usar el método correcto del controlador
            val createdGroup = groupController.insertGrupo(
                group.id,
                group.numberGroup,
                group.year,
                group.horario,
                group.courseCod,
                group.teacherId
            )

            val response = """
            HTTP/1.1 201 Created
            Content-Type: application/json
            Access-Control-Allow-Origin: *
            Access-Control-Allow-Methods: POST, GET, OPTIONS, PUT, DELETE
            Access-Control-Allow-Headers: Content-Type
            Connection: close
            
            ${gson.toJson(mapOf(
                "success" to true,
                "message" to "Group successfully created",
                "data" to createdGroup
            ))}
        """.trimIndent()

            writer.println(response)
            writer.flush()

        } catch (e: Exception) {
            println("Error in handleCreateGroup: ${e.message}")
            val errorResponse = """
            HTTP/1.1 500 Internal Server Error
            Content-Type: application/json
            
            ${gson.toJson(mapOf(
                "success" to false,
                "error" to "Error updating group",
                "message" to e.message
            ))}
        """.trimIndent()
            writer.println(errorResponse)
            writer.flush()
        }
    }

    private fun handleGetCareerCourses(writer: PrintWriter, path: String) {
        try {
            val segments = path.split("/")
            val careerCod = segments[3].toIntOrNull()

            if (careerCod == null) {
                sendErrorResponse(writer, "Invalid career code")
                return
            }

            val courses = careerController.getCareerCourses(careerCod)
            sendJsonResponse(writer, courses)
        } catch (e: Exception) {
            e.printStackTrace()
            sendErrorResponse(writer, "Error retrieving courses for career")
        }
    }

    private fun handleUpdateGroup(writer: PrintWriter, body: String) {
        println("PUT /api/groups request received with body: $body")

        try {
            val group = gson.fromJson(body, Grupo_::class.java)
            println("Updating group: $group")

            val success = groupController.updateGrupo(
                group.id,
                group.numberGroup,
                group.year,
                group.horario,
                group.courseCod,
                group.teacherId
            )

            val response = """
            HTTP/1.1 ${if (success) 200 else 400}
            Content-Type: application/json
            Access-Control-Allow-Origin: *
            Access-Control-Allow-Methods: POST, GET, OPTIONS, PUT, DELETE
            Access-Control-Allow-Headers: Content-Type
            Connection: close
            
            ${gson.toJson(mapOf(
                "success" to success,
                "message" to if (success) "Group successfully updated" else "Error updating group",
                "data" to group
            ))}
        """.trimIndent()

            writer.println(response)
            writer.flush()

        } catch (e: Exception) {
            println("Error in handleUpdateGroup: ${e.message}")
            val errorResponse = """
            HTTP/1.1 500 Internal Server Error
            Content-Type: application/json
            
            ${gson.toJson(mapOf(
                "success" to false,
                "error" to "Error updating group",
                "message" to e.message
            ))}
        """.trimIndent()
            writer.println(errorResponse)
            writer.flush()
        }
    }
    //--------------------------Fin del manejo de solicitudes de la entidad Grpup-------------------------

    // -------------------------Manejo de solicitudes de la entidad "Student"-------------------------
    private fun handleGetStudents(writer: PrintWriter) {
        try {
            val students = studentController.getAllStudents()
            val response = mapOf(
                "status" to "success",
                "data" to students
            )
            sendJsonResponse(writer, response)
        } catch (e: Exception) {
            println("Error in handleGetStudents: ${e.message}")
            sendErrorResponse(writer, "Error al obtener estudiantes")
        }
    }
    private fun handleDeleteStudent(writer: PrintWriter, id: Int?) {
        println("DELETE student request received for id: $id")
        if (id == null) {
            println("Invalid ID received")
            sendErrorResponse(writer, "ID inválido")
            return
        }
        try {
            val success = studentController.deleteStudent(id)
            println("Delete operation result: $success")

            if (success) {
                sendJsonResponse(writer, mapOf("success" to true))
            } else {
                sendErrorResponse(writer, "Error al eliminar estudiante")
            }
        } catch (e: Exception) {
            println("Error deleting student: ${e.message}")
            e.printStackTrace()
            sendErrorResponse(writer, "Error interno del servidor: ${e.message}")
        }
    }
    private fun handleCreateStudent(writer: PrintWriter, body: String) {
        println("POST /api/students request received with body: $body")

        try {
            val student = gson.fromJson(body, Student_::class.java)
            println("Creating student: $student")

            // Asumiendo que tienes un StudentController
            val createdStudent = studentController.insertStudent(
                student.id,
                student.name,
                student.telNumber,
                student.email,
                student.bornDate,
                student.careerCod,
                student.password
            )

            val response = """
            HTTP/1.1 201 Created
            Content-Type: application/json
            Access-Control-Allow-Origin: *
            Access-Control-Allow-Methods: POST, GET, OPTIONS, PUT, DELETE
            Access-Control-Allow-Headers: Content-Type
            Connection: close
            
            ${gson.toJson(mapOf(
                "success" to true,
                "message" to "Student successfully created",
                "data" to createdStudent
            ))}
        """.trimIndent()

            writer.println(response)
            writer.flush()

        } catch (e: Exception) {
            println("Error in handleCreateStudent: ${e.message}")
            val errorResponse = """
            HTTP/1.1 500 Internal Server Error
            Content-Type: application/json
            
            ${gson.toJson(mapOf(
                "success" to false,
                "error" to "Error al crear estudiante",
                "message" to e.message
            ))}
        """.trimIndent()
            writer.println(errorResponse)
            writer.flush()
        }
    }

    private fun handleUpdateStudent(writer: PrintWriter, body: String) {
        println("PUT /api/students request received with body: $body")

        try {
            val student = gson.fromJson(body, Student_::class.java)
            println("Updating student: $student")

            val success = studentController.updateStudent(
                student.id,
                student.name,
                student.telNumber,
                student.email,
                student.bornDate,
                student.careerCod
            )

            val response = """
            HTTP/1.1 ${if (success) 200 else 400}
            Content-Type: application/json
            Access-Control-Allow-Origin: *
            Access-Control-Allow-Methods: POST, GET, OPTIONS, PUT, DELETE
            Access-Control-Allow-Headers: Content-Type
            Connection: close
            
            ${gson.toJson(mapOf(
                "success" to success,
                "message" to if (success) "Student successfully updated" else "Error updating student",
                "data" to student
            ))}
        """.trimIndent()

            writer.println(response)
            writer.flush()

        } catch (e: Exception) {
            println("Error in handleUpdateStudent: ${e.message}")
            val errorResponse = """
            HTTP/1.1 500 Internal Server Error
            Content-Type: application/json
            
            ${gson.toJson(mapOf(
                "success" to false,
                "error" to "Error updating student",
                "message" to e.message
            ))}
        """.trimIndent()
            writer.println(errorResponse)
            writer.flush()
        }
    }


    //--------------------------Fin del manejo de solicitudes de la entidad "Student"-------------------------

    // -------------------------Manejo de solicitudes de la entidad "Teacher"-------------------------
    private fun handleGetTeachers(writer: PrintWriter) {
        try {
            val teachers = teacherController.getAllTeachers()
            val response = mapOf(
                "status" to "success",
                "data" to teachers
            )
            sendJsonResponse(writer, response)
        } catch (e: Exception) {
            println("Error in handleGetTeachers: ${e.message}")
            sendErrorResponse(writer, "Error al obtener profesores")
        }
    }
    private fun handleDeleteTeacher(writer: PrintWriter, id: Int?) {
        println("DELETE teacher request received for id: $id")
        if (id == null) {
            println("Invalid ID received")
            sendErrorResponse(writer, "Invalid ID")
            return
        }
        try {
            val success = teacherController.deleteTeacher(id)
            println("Delete operation result: $success")

            if (success) {
                sendJsonResponse(writer, mapOf("success" to true))
            } else {
                sendErrorResponse(writer, "Error al eliminar profesor")
            }
        } catch (e: Exception) {
            println("Error deleting teacher: ${e.message}")
            e.printStackTrace()
            sendErrorResponse(writer, "Error interno del servidor: ${e.message}")
        }
    }

    private fun handleCreateTeacher(writer: PrintWriter, body: String) {
        println("POST /api/teachers request received with body: $body")

        try {
            val teacher = gson.fromJson(body, Teacher_::class.java)
            println("Creating teacher: $teacher")

            val createdTeacher = teacherController.insertTeacher(
                teacher.id,
                teacher.name,
                teacher.telNumber,
                teacher.email,
                teacher.password
            )

            val response = """
            HTTP/1.1 201 Created
            Content-Type: application/json
            Access-Control-Allow-Origin: *
            Access-Control-Allow-Methods: POST, GET, OPTIONS, PUT, DELETE
            Access-Control-Allow-Headers: Content-Type
            Connection: close
            
            ${gson.toJson(mapOf(
                "success" to true,
                "message" to "Teacher successfully created",
                "data" to createdTeacher
            ))}
        """.trimIndent()

            writer.println(response)
            writer.flush()

        } catch (e: Exception) {
            println("Error in handleCreateTeacher: ${e.message}")
            val errorResponse = """
            HTTP/1.1 500 Internal Server Error
            Content-Type: application/json
            
            ${gson.toJson(mapOf(
                "success" to false,
                "error" to "Error al crear profesor",
                "message" to e.message
            ))}
        """.trimIndent()
            writer.println(errorResponse)
            writer.flush()
        }
    }

    private fun handleUpdateTeacher(writer: PrintWriter, body: String) {
        println("PUT /api/teachers request received with body: $body")

        try {
            val teacher = gson.fromJson(body, Teacher_::class.java)
            println("Updating teacher: $teacher")

            val success = teacherController.updateTeacher(
                teacher.id,
                teacher.name,
                teacher.telNumber,
                teacher.email
            )

            val response = """
            HTTP/1.1 ${if (success) 200 else 400}
            Content-Type: application/json
            Access-Control-Allow-Origin: *
            Access-Control-Allow-Methods: POST, GET, OPTIONS, PUT, DELETE
            Access-Control-Allow-Headers: Content-Type
            Connection: close
            
            ${gson.toJson(mapOf(
                "success" to success,
                "message" to if (success) "Teacher successfully updated" else "Error updating teacher",
                "data" to teacher
            ))}
        """.trimIndent()

            writer.println(response)
            writer.flush()

        } catch (e: Exception) {
            println("Error in handleUpdateTeacher: ${e.message}")
            val errorResponse = """
            HTTP/1.1 500 Internal Server Error
            Content-Type: application/json
            
            ${gson.toJson(mapOf(
                "success" to false,
                "error" to "Error updating teacher",
                "message" to e.message
            ))}
        """.trimIndent()
            writer.println(errorResponse)
            writer.flush()
        }
    }

    private fun handleGetTeacherGroups(writer: PrintWriter, teacherId: Int?) {
        if (teacherId == null) {
            sendErrorResponse(writer, "ID de profesor inválido")
            return
        }

        try {
            val groups = groupController.getGroupsByTeacher(teacherId)
            val response = mapOf(
                "status" to "success",
                "data" to groups
            )
            sendJsonResponse(writer, response)
        } catch (e: Exception) {
            sendErrorResponse(writer, "Error al obtener grupos del profesor")
        }
    }

    private fun handleGetGroupEnrollments(writer: PrintWriter, groupId: Int?) {
        if (groupId == null) {
            sendErrorResponse(writer, "ID de grupo inválido")
            return
        }

        try {
            val enrollments = enrollmentController.getEnrollmentsByGroup(groupId)
            val response = mapOf(
                "status" to "success",
                "data" to enrollments
            )
            sendJsonResponse(writer, response)
        } catch (e: Exception) {
            sendErrorResponse(writer, "Error al obtener matriculas del grupo")
        }
    }
    //--------------------------Fin del manejo de solicitudes de la entidad "Teacher"-------------------------


    // -------------------------Manejo de solicitudes de la entidad "User"-------------------------
    private fun handleGetUsers(writer: PrintWriter) {
        try {
            val users = userController.getAllUsers()
            val response = mapOf(
                "status" to "success",
                "data" to users
            )
            sendJsonResponse(writer, response)
        } catch (e: Exception) {
            println("Error in handleGetUsers: ${e.message}")
            sendErrorResponse(writer, "Error al obtener usuarios")
        }
    }

    private fun handleDeleteUser(writer: PrintWriter, id: Int?) {
        println("DELETE user request received for id: $id")
        if (id == null) {
            println("Invalid ID received")
            sendErrorResponse(writer, "ID inválido")
            return
        }
        try {
            val success = userController.deleteUser(id)
            println("Delete operation result: $success")

            if (success) {
                sendJsonResponse(writer, mapOf("success" to true))
            } else {
                sendErrorResponse(writer, "Error al eliminar usuario")
            }
        } catch (e: Exception) {
            println("Error deleting user: ${e.message}")
            e.printStackTrace()
            sendErrorResponse(writer, "Error interno del servidor: ${e.message}")
        }
    }

    private fun handleCreateUser(writer: PrintWriter, body: String) {
        println("POST /api/users request received with body: $body")

        try {
            val user = gson.fromJson(body, Usuario_::class.java)
            println("Creating user: $user")

            val createdUser = userController.insertUser(
                user.id,
                user.password,
                user.role
            )

            val response = """
            HTTP/1.1 201 Created
            Content-Type: application/json
            Access-Control-Allow-Origin: *
            Access-Control-Allow-Methods: POST, GET, OPTIONS, PUT, DELETE
            Access-Control-Allow-Headers: Content-Type
            Connection: close
            
            ${gson.toJson(mapOf(
                "success" to true,
                "message" to "User successfully created",
                "data" to createdUser
            ))}
        """.trimIndent()

            writer.println(response)
            writer.flush()

        } catch (e: Exception) {
            println("Error in handleCreateUser: ${e.message}")
            val errorResponse = """
            HTTP/1.1 500 Internal Server Error
            Content-Type: application/json
            
            ${gson.toJson(mapOf(
                "success" to false,
                "error" to "Error al crear usuario",
                "message" to e.message
            ))}
        """.trimIndent()
            writer.println(errorResponse)
            writer.flush()
        }
    }

    private fun handleUpdateUser(writer: PrintWriter, body: String) {
        println("PUT /api/users request received with body: $body")

        try {
            val user = gson.fromJson(body, Usuario_::class.java)
            println("Updating user: $user")

            val success = userController.updateUser(
                user.id,
                user.password,
                user.role
            )

            val response = """
            HTTP/1.1 ${if (success) 200 else 400}
            Content-Type: application/json
            Access-Control-Allow-Origin: *
            Access-Control-Allow-Methods: POST, GET, OPTIONS, PUT, DELETE
            Access-Control-Allow-Headers: Content-Type
            Connection: close
            
            ${gson.toJson(mapOf(
                "success" to success,
                "message" to if (success) "User successfully updated" else "Error updating user",
                "data" to user
            ))}
        """.trimIndent()

            writer.println(response)
            writer.flush()

        } catch (e: Exception) {
            println("Error in handleUpdateUser: ${e.message}")
            val errorResponse = """
            HTTP/1.1 500 Internal Server Error
            Content-Type: application/json
            
            ${gson.toJson(mapOf(
                "success" to false,
                "error" to "Error updating user",
                "message" to e.message
            ))}
        """.trimIndent()
            writer.println(errorResponse)
            writer.flush()
        }
    }
    // -------------------------Fin del manejo de solicitudes de la entidad "User"-------------------------

    //Login
    private fun handleLogin(writer: PrintWriter, body: String) {
        println("POST /api/login request received with body: $body")

        try {
            val jsonObject = gson.fromJson(body, JsonObject::class.java)
            val id = jsonObject.get("id")?.asInt
            val password = jsonObject.get("password")?.asString

            if (id == null || password == null) {
                sendErrorResponse(writer, "ID y contraseña son requeridos")
                return
            }

            // Verificar credenciales
            val loginSuccess = userController.loginUser(id, password)

            if (loginSuccess) {
                // Obtener información del usuario
                val user = userController.getUserById(id)
                if (user != null) {
                    val responseData = mapOf(
                        "success" to true,
                        "message" to "Login exitoso",
                        "user" to mapOf(
                            "id" to user.id,
                            "role" to user.role
                        )
                    )

                    // Respuesta exitosa
                    writer.println("HTTP/1.1 200 OK")
                    writer.println("Content-Type: application/json")
                    writer.println("Access-Control-Allow-Origin: *")
                    writer.println("Access-Control-Allow-Methods: POST, GET, OPTIONS")
                    writer.println("Access-Control-Allow-Headers: Content-Type")
                    writer.println("Connection: close")
                    writer.println()
                    writer.println(gson.toJson(responseData))
                } else {
                    // Caso improbable donde loginSuccess es true pero no se encuentra el usuario
                    sendErrorResponse(writer, "Error interno: usuario no encontrado")
                }
            } else {
                // Credenciales incorrectas
                val responseData = mapOf(
                    "success" to false,
                    "message" to "Credenciales incorrectas"
                )

                writer.println("HTTP/1.1 401 Unauthorized")
                writer.println("Content-Type: application/json")
                writer.println("Access-Control-Allow-Origin: *")
                writer.println("Connection: close")
                writer.println()
                writer.println(gson.toJson(responseData))
            }
        } catch (e: Exception) {
            println("Error in handleLogin: ${e.message}")
            e.printStackTrace()
            sendErrorResponse(writer, "Error en el servidor: ${e.message}")
        }
    }
    // -------------------------Fin del manejo de solicitudes de la entidad "login"-------------------------

    //-----------------------------------Manejo de solicitudes de registro ----------------------------------
    //registro de solicitudes
    private fun handleCreateRegistrationRequest(writer: PrintWriter, body: String) {
        try {
            val request = gson.fromJson(body, RegistrationRequest_::class.java)

            // Validaciones básicas
            if (request.user_id == 0 || request.password.isEmpty() || request.role.isEmpty()) {
                throw IllegalArgumentException("Datos incompletos")
            }

            // Validar campos según rol
            when (request.role) {
                "student" -> {
                    if (request.name.isNullOrEmpty() || request.tel_number == null ||
                        request.email.isNullOrEmpty() || request.born_date.isNullOrEmpty() ||
                        request.career_cod == null) {
                        throw IllegalArgumentException("Faltan datos requeridos para estudiante")
                    }
                }
                "teacher" -> {
                    if (request.name.isNullOrEmpty() || request.tel_number == null ||
                        request.email.isNullOrEmpty()) {
                        throw IllegalArgumentException("Faltan datos requeridos para profesor")
                    }
                }
                "admin", "matriculador" -> {
                    // No se requieren datos adicionales
                }
                else -> throw IllegalArgumentException("Rol no válido")
            }

            // Guardar solicitud en base de datos
            val requestId = registrationRequestController.createRequest(
                user_id = request.user_id,
                password = request.password,
                role = request.role,
                name = request.name,
                tel_number = request.tel_number,
                email = request.email,
                born_date = request.born_date,
                career_cod = request.career_cod
            )

            val response = """
                HTTP/1.1 201 Created
                Content-Type: application/json
                Access-Control-Allow-Origin: *
                
                ${gson.toJson(mapOf(
                "success" to true,
                "message" to "Solicitud de registro enviada para aprobación",
                "requestId" to requestId
            ))}
            """.trimIndent()
            writer.println(response)
        } catch (e: Exception) {
            val errorResponse = """
                HTTP/1.1 400 Bad Request
                Content-Type: application/json
                
                ${gson.toJson(mapOf(
                "success" to false,
                "error" to "Error en la solicitud de registro",
                "message" to e.message
            ))}
            """.trimIndent()
            writer.println(errorResponse)
        }
        writer.flush()
    }

    private fun handleGetPendingRegistrationRequests(writer: PrintWriter) {
        try {
            val requests = registrationRequestController.getPendingRequests()

            writer.println("HTTP/1.1 200 OK")
            writer.println("Content-Type: application/json")
            writer.println("Access-Control-Allow-Origin: *")
            writer.println("Connection: close")
            writer.println()
            writer.println(gson.toJson(mapOf(
                "success" to true,
                "data" to requests
            )))
        } catch (e: Exception) {
            e.printStackTrace()
            sendErrorResponse(writer, "Error al obtener solicitudes pendientes", 500)
        }
    }

    private fun handleApproveRegistrationRequest(writer: PrintWriter, requestId: Int) {
        try {
            // 1. Obtener la solicitud
            val request = registrationRequestController.getRequestById(requestId)
                ?: throw IllegalArgumentException("Solicitud no encontrada con ID: $requestId")

            // 2. Verificar que esté pendiente
            if (request.status != "pending") {
                throw IllegalStateException("La solicitud ID $requestId ya fue procesada (estado actual: ${request.status})")
            }

            // 3. Crear el usuario en el sistema
            val createdUser = userController.insertUser(
                id = request.user_id,
                password = request.password,
                role = request.role
            )

            // 4. Crear entidad específica según el rol
            when (request.role.lowercase()) {
                "student" -> {
                    require(request.name != null) { "Nombre requerido para estudiante" }
                    require(request.tel_number != null) { "Teléfono requerido para estudiante" }
                    require(request.email != null) { "Email requerido para estudiante" }
                    require(request.born_date != null) { "Fecha de nacimiento requerida para estudiante" }
                    require(request.career_cod != null) { "Carrera requerida para estudiante" }

                    studentController.insertStudent(
                        id = request.user_id,
                        name = request.name,
                        telNumber = request.tel_number,
                        email = request.email,
                        bornDate = request.born_date,
                        careerCod = request.career_cod,
                        password = request.password
                    )
                }
                "teacher" -> {
                    require(request.name != null) { "Nombre requerido para profesor" }
                    require(request.tel_number != null) { "Teléfono requerido para profesor" }
                    require(request.email != null) { "Email requerido para profesor" }

                    teacherController.insertTeacher(
                        id = request.user_id,
                        name = request.name,
                        telNumber = request.tel_number,
                        email = request.email,
                        password = request.password
                    )
                }
                "admin", "matriculador" -> {
                    // No se requieren datos adicionales para estos roles
                }
                else -> throw IllegalArgumentException("Rol no válido: ${request.role}")
            }

            // 5. Actualizar estado de la solicitud
            registrationRequestController.updateRequestStatus(requestId, "approved")

            // 6. Preparar respuesta exitosa
            val responseData = mapOf(
                "success" to true,
                "message" to "Solicitud aprobada exitosamente",
                "data" to mapOf(
                    "requestId" to requestId,
                    "userId" to request.user_id,
                    "role" to request.role,
                    "status" to "approved"
                )
            )

            // 7. Enviar respuesta HTTP
            sendCompleteResponse(writer, 200, responseData)

        } catch (e: IllegalArgumentException) {
            sendCompleteResponse(writer, 400, mapOf(
                "success" to false,
                "error" to "Datos inválidos",
                "message" to e.message.toString()
            ))
        } catch (e: IllegalStateException) {
            sendCompleteResponse(writer, 409, mapOf(
                "success" to false,
                "error" to "Conflicto de estado",
                "message" to e.message.toString()
            ))
        } catch (e: Exception) {
            sendCompleteResponse(writer, 500, mapOf(
                "success" to false,
                "error" to "Error interno del servidor",
                "message" to "Ocurrió un error al procesar la solicitud: ${e.message ?: "Error desconocido"}"
            ))
            e.printStackTrace()
        }
    }

    private fun sendCompleteResponse(writer: PrintWriter, statusCode: Int, data: Map<String, Any>) {
        try {
            writer.println("HTTP/1.1 $statusCode")
            writer.println("Content-Type: application/json; charset=utf-8")
            writer.println("Access-Control-Allow-Origin: *")
            writer.println("Access-Control-Allow-Methods: *")
            writer.println("Access-Control-Allow-Headers: *")
            writer.println("Connection: close")
            writer.println()
            writer.println(Gson().toJson(data))
            writer.flush()
        } catch (e: Exception) {
            println("Error al enviar respuesta: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun sendJsonResponse(writer: PrintWriter, statusCode: Int, data: Any) {
        writer.println("HTTP/1.1 $statusCode")
        writer.println("Content-Type: application/json")
        writer.println("Access-Control-Allow-Origin: *")
        writer.println("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS")
        writer.println("Access-Control-Allow-Headers: Content-Type")
        writer.println("Connection: close")
        writer.println()
        writer.println(gson.toJson(data))
        writer.flush()
    }

    private fun sendErrorResponse(
        writer: PrintWriter,
        statusCode: Int,
        error: String,
        message: String
    ) {
        val errorResponse = mapOf(
            "success" to false,
            "error" to error,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )

        writer.println("HTTP/1.1 $statusCode")
        writer.println("Content-Type: application/json")
        writer.println("Access-Control-Allow-Origin: *")
        writer.println("Connection: close")
        writer.println()
        writer.println(Gson().toJson(errorResponse))
    }

    private fun handleRejectRegistrationRequest(writer: PrintWriter, requestId: Int) {
        try {
            registrationRequestController.updateRequestStatus(requestId, "rejected")
            sendJsonResponse(writer, mapOf(
                "success" to true,
                "message" to "Solicitud rechazada"
            ))
        } catch (e: Exception) {
            sendErrorResponse(writer, "Error al rechazar solicitud: ${e.message}", 500)
        }
    }

    private fun handleError(writer: PrintWriter, e: Exception) {
        val errorResponse = """
        HTTP/1.1 500 Internal Server Error
        Content-Type: application/json
        Access-Control-Allow-Origin: *
        
        ${gson.toJson(mapOf(
            "success" to false,
            "error" to "Error interno del servidor",
            "message" to e.message
        ))}
    """.trimIndent()
        writer.println(errorResponse)
        writer.flush()
    }

    //--------------------------------fin del manejo de solicitudes de registro -----------------------------
    private fun sendJsonResponse(writer: PrintWriter, data: Any) {
        writer.println("HTTP/1.1 200 OK")
        writer.println("Access-Control-Allow-Origin: *")
        writer.println("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS")
        writer.println("Content-Type: application/json")
        writer.println("Connection: close")
        writer.println()
        writer.println(gson.toJson(data))
    }

    private fun sendErrorResponse(writer: PrintWriter, message: String, statusCode: Int = 400) {
        val errorResponse = """
        HTTP/1.1 $statusCode
        Content-Type: application/json
        Access-Control-Allow-Origin: *
        
        ${gson.toJson(mapOf(
            "success" to false.toString(),
            "error" to message
        ))}
    """.trimIndent()
        writer.println(errorResponse)
        writer.flush()
    }

}