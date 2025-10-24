package com.sustainability.accessmanagement.model

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Exception thrown when a resource is not found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFoundException(message: String) : RuntimeException(message)

/**
 * Exception thrown when a request is invalid
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidRequestException(message: String) : RuntimeException(message)

/**
 * Exception thrown when a user is not authorized to perform an action
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
class UnauthorizedException(message: String) : RuntimeException(message)

/**
 * Exception thrown when the resource state doesn't allow an operation
 */
@ResponseStatus(HttpStatus.CONFLICT)
class InvalidStateException(message: String) : RuntimeException(message)

/**
 * Exception thrown when there's an issue with the database transaction
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class DatabaseTransactionException(message: String) : RuntimeException(message) 
