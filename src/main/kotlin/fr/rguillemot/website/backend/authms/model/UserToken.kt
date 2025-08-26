package fr.rguillemot.website.backend.authms.model

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
    var token: String,

    @Column(name = "refresh_token", length = 255)
    var refreshToken: String? = null,

    @Column(name = "deviceId", length = 64)
    val deviceId: String? = null,

    @Column(name = "issued_at")
    var issuedAt: Instant = Instant.now()
)