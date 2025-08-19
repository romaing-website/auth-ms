package fr.rguillemot.website.backend.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "\"user_oauth\"")
data class UserOauth(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

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