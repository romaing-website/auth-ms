package fr.rguillemot.website.backend.request.Register

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegisterRequest(

    @field:NotBlank(message = "L'email est obligatoire")
    @field:Email(message = "Email invalide")
    // Regex pour interdire les alias +tag@ chez Gmail & co
    @field:Pattern(
        regexp = "^[^+@\\s]+@[^@\\s]+\\.[^@\\s]+\$",
        message = "Les alias (+tag) dans les emails ne sont pas autorisés"
    )
    val email: String,

    @field:NotBlank(message = "Le nom est obligatoire")
    @field:Size(min = 3, max = 50, message = "Le nom doit contenir entre 3 et 50 caractères")
    val firstName: String,

    @field:NotBlank(message = "Le nom est obligatoire")
    @field:Size(min = 3, max = 50, message = "Le nom doit contenir entre 3 et 50 caractères")
    val lastName: String,

    )