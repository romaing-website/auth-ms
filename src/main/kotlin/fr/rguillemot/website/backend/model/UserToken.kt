package fr.rguillemot.website.backend.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "\"user_token\"")
data class UserToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

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