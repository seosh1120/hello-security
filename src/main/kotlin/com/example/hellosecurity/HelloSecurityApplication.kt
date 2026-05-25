package com.example.hellosecurity

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HelloSecurityApplication

fun main(args: Array<String>) {
    runApplication<HelloSecurityApplication>(*args)
}
