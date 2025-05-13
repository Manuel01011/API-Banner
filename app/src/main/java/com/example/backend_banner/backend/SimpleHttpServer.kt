package com.example.banner.backend
import com.example.backend_banner.backend.Models.Career_
import com.example.backend_banner.backend.Controllers.CareerController
import com.example.backend_banner.backend.Controllers.CicloController
import com.example.backend_banner.backend.Controllers.CourseController
import com.example.backend_banner.backend.Models.Ciclo_
import com.example.backend_banner.backend.Models.Course_
import com.google.gson.Gson
import java.net.ServerSocket
import java.net.Socket
import java.io.*

class SimpleHttpServer(private val port: Int) {
    private val gson = Gson()
    private val careerController = CareerController()
    private val cicloController = CicloController()
    private val courseController = CourseController()

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

    private fun handleUpdateCareer(writer: PrintWriter, body: String) {
        try {
            val career = gson.fromJson(body, Career_::class.java)
            val success = careerController.editCareer(career.cod, career.name, career.title, emptyList(), emptyList())
            if (success) {
                sendJsonResponse(writer, mapOf("success" to true))
            } else {
                sendErrorResponse(writer, "Error al actualizar carrera")
            }
        } catch (e: Exception) {
            sendErrorResponse(writer, "Datos inválidos")
        }
    }
    // ---------------------------Fin del manejo de solicitudes de la entidad "carreras"-------------------------


    // -------------------------Manejo de solicitudes de la entidad "ciclos"-------------------------
    private fun handleGetCiclos(writer: PrintWriter) {
        val ciclos = cicloController.getAllCiclos() // Asume que tienes un CicloController
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
                course.cicloId,
                course.careerCod
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
                course.cicloId,
                course.careerCod
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


    private fun sendJsonResponse(writer: PrintWriter, data: Any) {
        writer.println("HTTP/1.1 200 OK")
        writer.println("Access-Control-Allow-Origin: *")
        writer.println("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS")
        writer.println("Content-Type: application/json")
        writer.println("Connection: close")
        writer.println()
        writer.println(gson.toJson(data))
    }

    private fun sendErrorResponse(writer: PrintWriter, message: String) {
        writer.println("HTTP/1.1 400 Bad Request")
        writer.println("Content-Type: application/json")
        writer.println("Connection: close")
        writer.println()
        writer.println(gson.toJson(mapOf("error" to message)))
    }
}