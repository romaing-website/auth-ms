package fr.rguillemot.website.backend.authms.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "\"user_passkey\"")
data class UserPasskey(
    @Id
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val user: User,

    @Column(name = "credential_id", nullable = false, unique = true, length = 128)
    val credentialId: String,

    @Column(name = "public_key", nullable = false, length = 512)
    val publicKey: String,

    @Column(name = "sign_count")
    val signCount: Int = 0,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now()
)