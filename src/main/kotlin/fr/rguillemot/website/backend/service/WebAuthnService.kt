package fr.rguillemot.website.backend.service

import fr.rguillemot.website.backend.config.WebAuthnConfig
import fr.rguillemot.website.backend.model.User
import fr.rguillemot.website.backend.model.WebAuthnChallenge
import fr.rguillemot.website.backend.repository.WebAuthnChallengesRepository
import fr.rguillemot.website.backend.type.CeremonyType
import fr.rguillemot.website.backend.type.ChallengeIssueResult
import fr.rguillemot.website.backend.type.ChallengePayload
import fr.rguillemot.website.backend.type.ChallengePayloadFull
import fr.rguillemot.website.backend.type.ChallengeRecord
import fr.rguillemot.website.backend.utils.b64urlEncode
import fr.rguillemot.website.backend.utils.randomBytes
import fr.rguillemot.website.backend.utils.secureEquals
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

/**
 * Minimal challenge service: generate, save (hook), validate.
 * You own the persistence. This class returns exactly what you need for DB + client.
 */
@Service
class WebAuthnService(
    private val config: WebAuthnConfig,
    private val webAuthnChallengesRepository: WebAuthnChallengesRepository
) {


    /**
     * Generate a new challenge for a given session and ceremony type.
     * Returns both: a record for DB and a payload for the client.
     */
    fun createChallenge(
        user: User,
        email: String,
        displayName: String,
        type: CeremonyType
    ): ChallengeIssueResult {
        val challengeBytes = randomBytes(config.challengeByteLength)
        val challengeB64 = b64urlEncode(challengeBytes)
        val now = Instant.now()
        val expiresAt = config.challengeTtlSeconds?.let { now.plusSeconds(it) }

        val record = ChallengeRecord(
            user = user,
            challengeB64Url = challengeB64,
            type = type,
            createdAt = now,
            expiresAt = expiresAt
        )

        val payload = mapOf(
            "rp" to mapOf(
                "id" to config.rpId,
                "name" to config.rpName
            ),
            "user" to mapOf(
                "id" to b64urlEncode(user.id.toString().toByteArray()), // en Base64URL
                "name" to email,
                "displayName" to displayName
            ),
            "challenge" to challengeB64,
            "pubKeyCredParams" to listOf(
                mapOf("type" to "public-key", "alg" to -7),   // ES256
                mapOf("type" to "public-key", "alg" to -257) // RS256
            ),
            "authenticatorSelection" to mapOf(
                "userVerification" to "preferred"
            ),
            "timeout" to config.timeoutMs,
            "attestation" to "none",
            "expiresAt" to expiresAt
        )
        // Save in db

        val webauthnDB = WebAuthnChallenge(
            user = record.user,
            challenge = record.challengeB64Url,
            type = record.type,
            createdAt = record.createdAt,
            expiresAt = record.expiresAt
        )
        try {
            webAuthnChallengesRepository.save(webauthnDB)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to save challenge: ${e.message}", e)
        }
        return ChallengeIssueResult(
            recordForDB = record,
            payloadForClient = ChallengePayloadFull(payload)
        )
    }

    /**
     * Hook for your persistence layer. Here it does nothing.
     * Replace with your repository call.
     */
    fun saveChallenge(record: ChallengeRecord) {
        // NO-OP: plug your DB/cache here.
        // Example:
        // challengeRepository.upsert(record.sessionId, record)
    }

    /**
     * Validate an incoming challenge against the stored one.
     * - Checks type
     * - Checks equality (constant-time-ish)
     * - Checks expiration if enabled
     *
     * Returns true if valid; false otherwise.
     * Throws IllegalStateException if expired (opt-in strictness).
     */
    fun validateChallenge(
        stored: ChallengeRecord,
        incomingChallengeB64Url: String,
        expectedType: CeremonyType
    ): Boolean {
        // type must match
        if (stored.type != expectedType) return false

        // check expiration
        if (stored.expiresAt != null && Instant.now().isAfter(stored.expiresAt)) {
            // choose your policy: throw or return false
            throw IllegalStateException("Challenge is expired")
        }

        // constant-time comparison to reduce timing leaks
        return secureEquals(stored.challengeB64Url, incomingChallengeB64Url)
    }



}
