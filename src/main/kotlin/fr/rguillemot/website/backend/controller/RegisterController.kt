package fr.rguillemot.website.backend.controller

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import fr.rguillemot.website.backend.model.User
import fr.rguillemot.website.backend.repository.WebAuthnChallengesRepository
import fr.rguillemot.website.backend.request.Register.RegisterRequest
import fr.rguillemot.website.backend.request.RegisterVerifyRequest
import fr.rguillemot.website.backend.service.UserService
import fr.rguillemot.website.backend.service.WebAuthnService
import fr.rguillemot.website.backend.type.ApiResponse
import fr.rguillemot.website.backend.type.CeremonyType
import fr.rguillemot.website.backend.type.ChallengeRecord
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.Base64
import java.util.Objects


//TODO: When upload avatar check it with free antivirus (ClamAV)

@RestController
class RegisterController(
    private val webAuthnService: WebAuthnService,
    private val userService: UserService,
    private val webAuthnChallengesRepository: WebAuthnChallengesRepository,
) {


    @PostMapping("/register", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun register(
        @Valid @ModelAttribute req: RegisterRequest,
        @RequestPart("avatar", required = false) avatar: MultipartFile?
    ): ResponseEntity<ApiResponse<Any>> {



        val user = User(
            email = req.email,
            firstName = req.firstName,
            lastName = req.lastName,
        )
        val createdUser = userService.create(user, avatar)
        if (!createdUser.status || createdUser.user == null) {
            System.out.println(createdUser.status)
            System.out.println(createdUser.user)

            return ResponseEntity.internalServerError().body(
                ApiResponse(
                    status = "error",
                    message = Objects.requireNonNullElse(createdUser.message, "Unexpected Error")
                )
            )
        }
        val savedUser = createdUser.user
        // Generate WebAuth Challenge

        try {
            val webAuthnChallenge = webAuthnService.createRegisterChallenge(savedUser, savedUser.email, savedUser.lastName + " " + savedUser.firstName)
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

    @PostMapping("/register/verify", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun verifyRegistration(
        @Valid @RequestBody req: RegisterVerifyRequest
    ): ResponseEntity<ApiResponse<Any>> {

        val clientDataJson = String(java.util.Base64.getUrlDecoder().decode(req.response.clientDataJson))
        val clientData = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper().readValue(clientDataJson, Map::class.java)
        val originalChallenge = clientData["challenge"] as String

        val challengeData = webAuthnChallengesRepository.findByUser_EmailAndChallenge(req.email, originalChallenge)
        println("Stored challenge: ${challengeData?.challenge}")
        println("ClientData: $clientDataJson")
        println("Original challenge: $originalChallenge")
        if(challengeData == null) {
            return ResponseEntity.badRequest().body(
                ApiResponse(status = "error", message = "Failed to find WebAuthn challenge")
            )
        }
        var isChallengeValid: Boolean
        try {
            isChallengeValid = webAuthnService.validateChallenge(stored = ChallengeRecord(
                user = challengeData.user,
                challengeB64Url = challengeData.challenge,
                type = challengeData.type,
                createdAt = challengeData.createdAt,
                expiresAt = challengeData.expiresAt
            ), originalChallenge, CeremonyType.CREATE)
        } catch (e: IllegalStateException) {
            return ResponseEntity.badRequest().body(
                ApiResponse(
                    status = "error",
                    message = e.message ?: "Invalid or expired challenge",
                    data = null
                )
            )
        }
        if (!isChallengeValid) {
            return ResponseEntity.badRequest().body(
                ApiResponse(
                    status = "error",
                    message = "Invalid challenge",
                    data = null
                )
            )
        }

        val isSignatureValid = webAuthnService.verifySignature(req)
        if (!isSignatureValid) {
            return ResponseEntity.badRequest().body(
                ApiResponse(
                    status = "error",
                    message = "Invalid signature",
                    data = null
                )
            )
        }
        val user = challengeData.user

        if(user == null) {
            return ResponseEntity.badRequest().body(
                ApiResponse(
                    status = "error",
                    message = "Invalid User",
                    data = null
                )
            )
        }
        val coseKey = extractCoseKeyFromAttestationObject(req.response.attestationObject)

        webAuthnService.saveCredential(
            user = user,
            credential_id = req.rawId,
            public_key = Base64.getUrlEncoder().withoutPadding().encodeToString(coseKey),
            sign_Count = 0
        )
        return ResponseEntity.ok(
            ApiResponse(
                status = "success",
                message = "Registration verified and saved",
                data = null
            )
        )
    }

    private fun extractCoseKeyFromAttestationObject(attestationObjectB64: String): ByteArray {
        val attObj = java.util.Base64.getUrlDecoder().decode(attestationObjectB64)
        val mapper = CBORMapper()
        val node = mapper.readTree(attObj)
        val authData = node.get("authData").binaryValue() // binaire

        var offset = 37
        val credIdLen = ((authData[offset + 16].toInt() and 0xFF) shl 8) or (authData[offset + 17].toInt() and 0xFF)
        offset += 18 + credIdLen
        val coseKey = authData.copyOfRange(offset, authData.size)
        return coseKey
    }
}
