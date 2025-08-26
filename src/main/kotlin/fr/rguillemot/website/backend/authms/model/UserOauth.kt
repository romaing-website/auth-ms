package fr.rguillemot.website.backend.authms.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "\"user_oauth\"")
data class UserOauth(
    @Id
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val user: User,

    @Column(nullable = false, length = 32)
    val provider: String,

    @Column(nullable = false, length = 128)
    val externalId: String,

    @Column(name = "access_token", length = 255)
    val accessToken: String? = null,

    @Column(name = "refresh_token", length = 255)
    val refreshToken: String? = null,

    @Column(name = "expires_at")
    val expiresAt: Instant? = null
)