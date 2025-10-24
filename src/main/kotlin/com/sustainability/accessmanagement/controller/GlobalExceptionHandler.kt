package com.sustainability.accessmanagement.controller

import com.sustainability.accessmanagement.model.DatabaseTransactionException
import com.sustainability.accessmanagement.model.ErrorResponse
import com.sustainability.accessmanagement.model.InvalidRequestException
import com.sustainability.accessmanagement.model.InvalidStateException
import com.sustainability.accessmanagement.model.ResourceNotFoundException
import com.sustainability.accessmanagement.model.UnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.time.Instant

/**
 * Global exception handler for access management related controllers
 */
@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Handle ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
        ex: ResourceNotFoundException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error("Resource not found: {}", ex.message)

        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = ex.message ?: "Resource not found",
            path = request.getDescription(false).substring(4), // Remove "uri=" prefix
        )

        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    /**
     * Handle InvalidRequestException
     */
    @ExceptionHandler(InvalidRequestException::class)
    fun handleInvalidRequestException(ex: InvalidRequestException, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.error("Invalid request: {}", ex.message)

        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Invalid request",
            path = request.getDescription(false).substring(4),
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    /**
     * Handle UnauthorizedException
     */
    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(ex: UnauthorizedException, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.error("Unauthorized access: {}", ex.message)

        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.FORBIDDEN.value(),
            error = "Forbidden",
            message = ex.message ?: "Unauthorized access",
            path = request.getDescription(false).substring(4),
        )

        return ResponseEntity(errorResponse, HttpStatus.FORBIDDEN)
    }

    /**
     * Handle InvalidStateException
     */
    @ExceptionHandler(InvalidStateException::class)
    fun handleInvalidStateException(ex: InvalidStateException, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.error("Invalid state: {}", ex.message)

        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.CONFLICT.value(),
            error = "Conflict",
            message = ex.message ?: "Invalid state",
            path = request.getDescription(false).substring(4),
        )

        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    /**
     * Handle DatabaseTransactionException
     */
    @ExceptionHandler(DatabaseTransactionException::class)
    fun handleDatabaseTransactionException(
        ex: DatabaseTransactionException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error("Database transaction error: {}", ex.message)

        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = ex.message ?: "Database transaction error",
            path = request.getDescription(false).substring(4),
        )

        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    /**
     * Handle NoResourceFoundException (missing static resources like favicon)
     */
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(ex: NoResourceFoundException, request: WebRequest): ResponseEntity<Any> {
        // For favicon.ico requests, return a 204 No Content status
        if (request.getDescription(false).contains("uri=/favicon.ico")) {
            return ResponseEntity.noContent().build()
        }

        // For other missing resources, log and return a 404
        logger.debug("Resource not found: {}", ex.message)
        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = HttpStatus.NOT_FOUND.reasonPhrase,
            message = "The requested resource was not found",
            path = request.getDescription(false).replace("uri=", ""),
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    /**
     * Handle all uncaught exceptions
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled exception:", ex)
        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            message = "An unexpected error occurred",
            path = request.getDescription(false).replace("uri=", ""),
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}

/**
 * Standard error response
 */
data class ErrorResponse(
    val timestamp: Instant,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
) 
