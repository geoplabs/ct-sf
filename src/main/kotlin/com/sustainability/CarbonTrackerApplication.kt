package com.sustainability

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CarbonTrackerApplication

fun main(args: Array<String>) {
    runApplication<CarbonTrackerApplication>(*args)
} 
