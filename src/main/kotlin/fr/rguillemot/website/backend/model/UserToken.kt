package fr.rguillemot.website.backend.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "\"user_token\"")
data class UserToken(
    @Id
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val user: User,

    @Column(nullable = false, length = 255)
    val token: String,

    @Column(name = "refresh_token", length = 255)
    val refreshToken: String? = null,

    @Column(name = "device_name", length = 64)
    val deviceName: String? = null,

    @Column(length = 32)
    val provider: String = "local",

    @Column(name = "expires_at")
    val expiresAt: Instant? = null,

    @Column(name = "issued_at")
    val issuedAt: Instant = Instant.now()
)