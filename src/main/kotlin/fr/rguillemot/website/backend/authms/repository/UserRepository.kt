package fr.rguillemot.website.backend.authms.repository

import org.springframework.data.jpa.repository.JpaRepository
import fr.rguillemot.website.backend.authms.model.User
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun existsByEmail(email: String): Boolean
}