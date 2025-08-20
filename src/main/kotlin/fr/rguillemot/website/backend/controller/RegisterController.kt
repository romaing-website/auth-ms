package fr.rguillemot.website.backend.controller

import fr.rguillemot.website.backend.model.User
import fr.rguillemot.website.backend.repository.UserRepository
import fr.rguillemot.website.backend.request.Register.RegisterRequest
import fr.rguillemot.website.backend.service.WebAuthnService
import fr.rguillemot.website.backend.type.ApiResponse
import fr.rguillemot.website.backend.type.CeremonyType
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class RegisterController(
    private val userRepository: UserRepository,
    private val webAuthnService: WebAuthnService
) {

    private val allowedContentTypes = setOf("image/png", "image/jpeg", "image/webp")

    @PostMapping("/register", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun register(
        @Valid @ModelAttribute req: RegisterRequest,
        @RequestPart("avatar", required = false) avatar: MultipartFile?
    ): ResponseEntity<ApiResponse<Any>> {

        var isAvatar = true

        // Validate the Avatar file
        if (avatar == null || avatar.isEmpty) {
            isAvatar = false
        }
        if (isAvatar && avatar?.contentType !in allowedContentTypes) {
            return ResponseEntity.badRequest().body(
                ApiResponse(status = "error", message = "Only PNG, JPEG, and WEBP images are allowed")
            )
        }
        if (isAvatar && avatar?.size!! > 8 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(
                ApiResponse(status = "error", message = "Avatar size must not exceed 8MB")
            )
        }
        // Check if the user already exists
        if (userRepository.existsByEmail(req.email)) {
            return ResponseEntity.badRequest().body(
                ApiResponse(status = "error", message = "User with this email already exists")
            )
        }
        // Save user to database
        val user = User(
            email = req.email,
            firstName = req.firstName,
            lastName = req.lastName,
        )
        val savedUser: User = userRepository.save(user)

        // Generate WebAuth Challenge

        try {
            val webAuthnChallenge = webAuthnService.createChallenge(savedUser, savedUser.email, savedUser.lastName + " " + savedUser.firstName, CeremonyType.CREATE)
            return ResponseEntity.ok(
                ApiResponse(
                    status = "ok",
                    message = "Compte créé avec succès",
                    data = mapOf(
                        "challenge" to webAuthnChallenge.payloadForClient
                    )
                )
            )
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(
                ApiResponse(status = "error", message = "Failed to create WebAuthn challenge: ${e.message}")
            )
        }

    }

/*@PostMapping("/register/verify", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun verifyRegistration(
        @Valid @ModelAttribute req: RegisterVerifyRequest
    ): ResponseEntity<ApiResponse<Any>> {

    }*/
}
