package fr.rguillemot.website.backend.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "\"user\"")
data class User(
    @Id
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true, length = 255)
    val email: String,

    @Column(name = "first_name", length = 64)
    val firstName: String? = null,

    @Column(name = "last_name", length = 64)
    val lastName: String? = null,

    @Column(name = "avatar_url", length = 255)
    val avatarUrl: String? = null,

    @Column(length = 32)
    val role: String = "user",

    @Column(name = "is_active")
    val isActive: Boolean = false,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    val updatedAt: Instant = Instant.now()
)