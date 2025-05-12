package com.example.banner.backend

fun main() {
    val server = SimpleHttpServer(8080)
    server.start()

    // Mantener el programa corriendo
    while (true) {
        Thread.sleep(1000)
    }
}