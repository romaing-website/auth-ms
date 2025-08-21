package fr.rguillemot.website.backend.service

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import fr.rguillemot.website.backend.config.WebAuthnConfig
import fr.rguillemot.website.backend.model.User
import fr.rguillemot.website.backend.model.UserPasskey
import fr.rguillemot.website.backend.model.WebAuthnChallenge
import fr.rguillemot.website.backend.repository.UserPasskeyRepository
import fr.rguillemot.website.backend.repository.WebAuthnChallengesRepository
import fr.rguillemot.website.backend.request.Login.LoginRequest
import fr.rguillemot.website.backend.request.RegisterVerifyRequest
import fr.rguillemot.website.backend.type.CeremonyType
import fr.rguillemot.website.backend.type.ChallengeIssueResult
import fr.rguillemot.website.backend.type.ChallengePayload
import fr.rguillemot.website.backend.type.ChallengePayloadFull
import fr.rguillemot.website.backend.type.ChallengeRecord
import fr.rguillemot.website.backend.type.PasskeyAuthRequest
import fr.rguillemot.website.backend.utils.b64urlEncode
import fr.rguillemot.website.backend.utils.randomBytes
import fr.rguillemot.website.backend.utils.secureEquals
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.ECFieldFp
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import java.security.spec.EllipticCurve
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.util.UUID


//TODO: Challenge auto removed
//TODO: Challenge expire 5min
//TODO: Add "used" field for challenges
/**
 * Minimal challenge service: generate, save (hook), validate.
 * You own the persistence. This class returns exactly what you need for DB + client.
 */
@Service
class WebAuthnService(
    private val config: WebAuthnConfig,
    private val webAuthnChallengesRepository: WebAuthnChallengesRepository,
    private val userPasskeyRepository: UserPasskeyRepository
) {


    /**
     * Generate a new challenge for a given session and ceremony type.
     * Returns both: a record for DB and a payload for the client.
     */
    fun createRegisterChallenge(
        user: User,
        email: String,
        displayName: String,
    ): ChallengeIssueResult {
        val challengeBytes = randomBytes(config.challengeByteLength)
        val challengeB64 = b64urlEncode(challengeBytes)
        val now = Instant.now()
        val expiresAt = config.challengeTtlSeconds?.let { now.plusSeconds(it) }

        val record = ChallengeRecord(
            user = user,
            challengeB64Url = challengeB64,
            type = CeremonyType.CREATE,
            createdAt = now,
            expiresAt = expiresAt
        )
        if(record.user == null) {
            throw IllegalStateException("Failed to save challenge")
        }

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
            println(e.message)
            throw IllegalStateException("Failed to save challenge")
        }
        return ChallengeIssueResult(
            recordForDB = record,
            payloadForClient = ChallengePayloadFull(payload)
        )
    }


    fun createLoginChallenge(): ChallengeIssueResult {
        val challengeBytes = randomBytes(config.challengeByteLength)
        val challengeB64 = b64urlEncode(challengeBytes)
        val now = Instant.now()
        val expiresAt = config.challengeTtlSeconds?.let { now.plusSeconds(it) }

        val record = ChallengeRecord(
            challengeB64Url = challengeB64,
            type = CeremonyType.GET,
            createdAt = now,
            expiresAt = expiresAt
        )

        val payload = mapOf(
            "rp" to mapOf(
                "id" to config.rpId,
                "name" to config.rpName
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


    fun saveCredential(user: User, credential_id: String, public_key: String, sign_Count: Int) {
        val userCredential = UserPasskey(
            user = user,
            credentialId = credential_id,
            publicKey = public_key,
            signCount = sign_Count
        )
        userPasskeyRepository.save(userCredential)
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


    fun verifySignature(req: RegisterVerifyRequest): Boolean {
        if (req.response.signature.isBlank()) {
            return true
        }
        val clientData = String(java.util.Base64.getUrlDecoder().decode(req.response.clientDataJson))
        val authenticatorData = java.util.Base64.getUrlDecoder().decode(req.response.authenticatorData)
        val signature = java.util.Base64.getUrlDecoder().decode(req.response.signature)

        // Récupérer la clé publique associée à l'utilisateur (si présente)
        val passKey = userPasskeyRepository.findByUser_Email(req.email) ?: return false

        // Vérification de la signature
        return verifyWithPublicKey(passKey.publicKey, authenticatorData, clientData, signature)
    }

    fun verifySignature(req: LoginRequest): Boolean {
        if (req.response.signature.isBlank()) {
            return true
        }
        val clientDataJSON = java.util.Base64.getUrlDecoder().decode(req.response.clientDataJson)
        val authenticatorData = java.util.Base64.getUrlDecoder().decode(req.response.authenticatorData)
        val signature = java.util.Base64.getUrlDecoder().decode(req.response.signature)

        // Récupérer la clé publique associée à credentiel id if exist
        val passKey = userPasskeyRepository.findByCredentialId(req.id) ?: return false

        val clientDataHash = java.security.MessageDigest.getInstance("SHA-256").digest(clientDataJSON)
        val signedData = authenticatorData + clientDataHash
        // Vérification de la signature
        return verifyECDSASignatureWithCOSE(passKey.publicKey, signedData, signature)

    }

    private fun verifyWithPublicKey(publicKey: String, authData: ByteArray, clientData: String, signature: ByteArray): Boolean {
        val combinedData = authData + clientData.toByteArray()
        val coseKey = java.util.Base64.getUrlDecoder().decode(publicKey) // ou .decode pour binaire pur
        println(coseKey.joinToString(" ") { "%02x".format(it) })

        val pubKey = coseToECPublicKey(coseKey)

        val signatureInstance = Signature.getInstance("SHA256withECDSA")
        signatureInstance.initVerify(pubKey)
        signatureInstance.update(combinedData)

        return signatureInstance.verify(signature)
    }


    private fun verifyECDSASignatureWithCOSE(cosePublicKey: String, data: ByteArray, signature: ByteArray): Boolean {
        try {
            val coseKey = java.util.Base64.getUrlDecoder().decode(cosePublicKey)
            val publicKey = coseToECPublicKey(coseKey)

            val verifier = Signature.getInstance("SHA256withECDSA")
            verifier.initVerify(publicKey)
            verifier.update(data)

            return verifier.verify(signature)
        } catch (e: Exception) {
            println("ECDSA verification failed: ${e.message}")
            return false
        }
    }
    //TODO: Edit list of url for use application variables.
    private fun isValidOrigin(origin: String): Boolean {
        val allowedOrigins = listOf("http://localhost:8234")
        return allowedOrigins.contains(origin)
    }


    private fun coseToECPublicKey(cose: ByteArray): PublicKey {
        val mapper = CBORMapper()
        val tree = mapper.readTree(cose)
        // COSE_Key fields: -2 = x, -3 = y
        println(tree)
        println("COSE raw: ${cose.joinToString(",")}")
        println("CBOR tree: $tree")
        val x = tree.get("-2").binaryValue()
        val y = tree.get("-3").binaryValue()

        // Paramètres courbe P-256 secp256r1
        val p = BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853951")
        val a = BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853948")
        val b = BigInteger("41058363725152142129326129780047268409114441015993725554835256314039467401291")
        val gx = BigInteger("48439561293906451759052585252797914202762949526041747995844080717082404635286")
        val gy = BigInteger("36134250956749795798585127919587881956611106672985015071877198253568414405109")
        val n = BigInteger("115792089210356248762697446949407573529996955224135760342422259061068512044369")
        val curve = EllipticCurve(ECFieldFp(p), a, b)
        val generator = ECPoint(gx, gy)
        val ecSpec = ECParameterSpec(curve, generator, n, 1)

        val ecPoint = ECPoint(BigInteger(1, x), BigInteger(1, y))
        val pubKeySpec = ECPublicKeySpec(ecPoint, ecSpec)
        return KeyFactory.getInstance("EC").generatePublic(pubKeySpec)
    }


}
