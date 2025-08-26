package fr.rguillemot.website.backend.authms.request.Register

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
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

data class RegisterVerifyRequest(

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email is invalid")
    @field:Pattern(
        regexp = "^[^+@\\s]+@[^@\\s]+\\.[^@\\s]+\$",
        message = "We don't accept email alias."
    )
    val email: String,

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

    @field:Valid
    val response: KeyData
)