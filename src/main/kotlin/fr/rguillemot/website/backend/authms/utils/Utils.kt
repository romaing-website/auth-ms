package fr.rguillemot.website.backend.authms.utils

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

private val RNG: SecureRandom = SecureRandom()

fun randomBytes(len: Int): ByteArray =
    ByteArray(len).also { RNG.nextBytes(it) }

fun secureEquals(a: String, b: String): Boolean {
    val aBytes = a.toByteArray(StandardCharsets.UTF_8)
    val bBytes = b.toByteArray(StandardCharsets.UTF_8)
    if (aBytes.size != bBytes.size) return false
    // constant-time compare (JCA)
    return MessageDigest.isEqual(aBytes, bBytes)
}

fun b64urlEncode(data: ByteArray): String =
    Base64.getUrlEncoder().withoutPadding().encodeToString(data)

fun b64urlDecode(s: String): ByteArray =
    Base64.getUrlDecoder().decode(s)



