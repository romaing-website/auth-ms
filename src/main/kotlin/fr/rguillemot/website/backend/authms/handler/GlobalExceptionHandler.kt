package fr.rguillemot.website.backend.authms.handler

import fr.rguillemot.website.backend.authms.type.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MultipartException
import org.springframework.web.multipart.support.MissingServletRequestPartException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleUnsupportedMedia(ex: HttpMediaTypeNotSupportedException): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(ApiResponse(status = "error", message = "Unsupported media type: ${ex.contentType}"))

    @ExceptionHandler(MultipartException::class)
    fun handleMultipart(ex: MultipartException): ResponseEntity<ApiResponse<Any>> {
        val msg = if (ex.message?.contains("no multipart boundary", ignoreCase = true) == true)
            "Invalid multipart request: missing boundary"
        else
            "Invalid multipart request"
        return ResponseEntity.badRequest()
            .body(ApiResponse(status = "error", message = msg))
    }

    @ExceptionHandler(MissingServletRequestPartException::class)
    fun handleMissingPart(ex: MissingServletRequestPartException): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity.badRequest()
            .body(ApiResponse(status = "error", message = "Missing part: ${ex.requestPartName}"))

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(ex: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Any>> {
        val rootCause = ex.cause
        val msg = when (rootCause) {
            is com.fasterxml.jackson.databind.exc.MismatchedInputException -> {
                val path = rootCause.path.joinToString(".") { it.fieldName ?: "[${it.index}]" }
                "Invalid JSON at '$path': ${rootCause.originalMessage}"
            }
            else -> "Invalid JSON payload: ${rootCause?.message ?: ex.message}"
        }
        return ResponseEntity.badRequest()
            .body(ApiResponse(status = "error", message = msg))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Any>> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
        return ResponseEntity.badRequest()
            .body(ApiResponse(status = "error", message = "Validation failed", data = errors))
    }
}
