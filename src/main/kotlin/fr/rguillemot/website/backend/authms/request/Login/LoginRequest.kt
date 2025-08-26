package fr.rguillemot.website.backend.authms.request.Login

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size


data class KeyData(
    @field:NotBlank(message = "The clientDataJson field is required")
    val clientDataJson: String,

    @field:NotBlank(message = "The attestationObject field is required")
    val attestationObject: String,

    @field:NotBlank(message = "The authenticatorData field is required")
    val authenticatorData: String,

    val signature: String
)

data class LoginRequest(

    @field:NotBlank(message = "The id field is required")
    @field:Size(max = 255, message = "The id must not exceed 255 characters")
    val id: String,

    @field:NotBlank(message = "The rawId field is required")
    @field:Size(max = 255, message = "The rawId must not exceed 255 characters")
    val rawId: String,

    @field:NotBlank(message = "The type field is required")
    @field:Pattern(
        regexp = "^public-key\$",
        message = "The type field must be 'public-key'"
    )
    val type: String,

    @field:NotBlank(message = "The timeZone field is required")
    @field:Size(max = 100, message = "The timeZone must not exceed 100 characters")
    @field:Pattern(
        regexp = "^[A-Za-z/_+-]+$",
        message = "The timeZone field contains invalid characters"
    )
    val timeZone: String,

    @field:Valid
    val response: KeyData
)