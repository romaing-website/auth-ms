package fr.rguillemot.website.backend.controller

import fr.rguillemot.website.backend.repository.WebAuthnChallengesRepository
import fr.rguillemot.website.backend.request.Login.LoginRequest
import fr.rguillemot.website.backend.service.UserPassKeyService
import fr.rguillemot.website.backend.service.UserTokenService
import fr.rguillemot.website.backend.service.WebAuthnService
import fr.rguillemot.website.backend.type.ApiResponse
import fr.rguillemot.website.backend.type.CeremonyType
import fr.rguillemot.website.backend.type.ChallengeRecord
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import kotlin.collections.get




@RestController
class LoginController(
    private val webAuthnChallengesRepository: WebAuthnChallengesRepository,
    private val userPasskeyService: UserPassKeyService,
    private val webAuthnService: WebAuthnService,
    private val userTokenService: UserTokenService,
) {

    @PostMapping("/login", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun login(): ResponseEntity<ApiResponse<Any>> {
            val challenge = webAuthnService.createLoginChallenge()
            return ResponseEntity.ok(
                ApiResponse(
                    status = "ok",
                    message = "Challenge created successfully",
                    data = mapOf(
                        "challenge" to challenge.payloadForClient
                    )
                )
            )

    }

    @PostMapping("/login/verify", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun loginCheck(
        @Valid @RequestBody req: LoginRequest,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<Any>> {
        val clientDataJson = String(java.util.Base64.getUrlDecoder().decode(req.response.clientDataJson))
        val clientData = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper().readValue(clientDataJson, Map::class.java)
        val originalChallenge = clientData["challenge"] as String
        val ip = request.remoteAddr
        val userAgent = request.getHeader("User-Agent") ?: "Unknown"
        val language = request.getHeader("Accept-Language") ?: "Unknown"
        val timezone = req.timeZone

        val challengeData = webAuthnChallengesRepository.findByChallenge(originalChallenge)
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
                challengeB64Url = challengeData.challenge,
                type = challengeData.type,
                createdAt = challengeData.createdAt,
                expiresAt = challengeData.expiresAt
            ), originalChallenge, CeremonyType.GET)
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
        val user = userPasskeyService.getUser(req.id)
        if(user == null) {
            return ResponseEntity.badRequest().body(
                ApiResponse(
                    status = "error",
                    message = "User not found",
                    data = null
                )
            )
        }
        val tokens = userTokenService.createToken(user, ip, userAgent, language, timezone)
        return ResponseEntity.ok(
            ApiResponse(
                status = "success",
                message = "Login Successfull",
                data = {
                    "accessToken" to tokens.accessToken
                    "refreshToken" to tokens.refreshToken
                }
            )
        )

    }
}