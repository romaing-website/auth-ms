package fr.rguillemot.website.backend.model

import fr.rguillemot.website.backend.model.User
import fr.rguillemot.website.backend.type.CeremonyType
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "webauthn_challenges")
data class WebAuthnChallenge(
    @Id
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val user: User? = null,

    @Column(nullable = false)
    val challenge: String,

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    val type: CeremonyType,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant?
)
