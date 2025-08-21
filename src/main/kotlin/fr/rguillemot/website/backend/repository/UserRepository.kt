package fr.rguillemot.website.backend.repository

import org.springframework.data.jpa.repository.JpaRepository
import fr.rguillemot.website.backend.model.User
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun existsByEmail(email: String): Boolean
}