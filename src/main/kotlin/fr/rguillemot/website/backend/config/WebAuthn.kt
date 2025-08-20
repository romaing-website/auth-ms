package fr.rguillemot.website.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "webauthn")
data class WebAuthnConfig(
    val rpId: String,
    val rpName: String,
    val originUrl: String,
    val timeoutMs: Long = 60_000,
    val challengeByteLength: Int = 32,
    val challengeTtlSeconds: Long? = 120
)
