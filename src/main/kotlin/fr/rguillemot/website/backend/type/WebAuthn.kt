package fr.rguillemot.website.backend.type

import fr.rguillemot.website.backend.model.User
import java.time.Instant
import java.util.UUID

/**
 * Ceremony type to distinguish registration (create) vs authentication (get).
 */
enum class CeremonyType {
    CREATE, GET
}

/**
 * Challenge record you will store in DB (or cache) keyed by sessionId/user context.
 * Store as-is; all fields are serializable-friendly.
 */
data class ChallengeRecord(
    val user: User,
    val challengeB64Url: String,  // base64url-encoded (no padding)
    val type: CeremonyType,
    val createdAt: Instant,
    val expiresAt: Instant? = null // optional TTL if you want server-side timeout
)

/**
 * What you send back to the client (frontend) after createChallenge().
 * Keep only what the client must receive.
 */
data class ChallengePayload(
    val rp: Map<String, String>,
    val user: Map<String, String>,
    val challenge: String,
    val pubKeyCredParams: List<Map<String, Any>>,
    val authenticatorSelection: Map<String, String>,
    val timeout: Long,
    val attestation: String,
    val expiresAt: Instant?
)
data class ChallengePayloadFull(
    val options: Map<String, Any?>
)


/**
 * Small helper result for service functions when you want both DB and client data.
 */
data class ChallengeIssueResult(
    val recordForDB: ChallengeRecord,
    val payloadForClient: ChallengePayloadFull
)
