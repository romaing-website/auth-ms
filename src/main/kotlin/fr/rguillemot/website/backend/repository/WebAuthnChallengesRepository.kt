package fr.rguillemot.website.backend.repository

import fr.rguillemot.website.backend.model.WebAuthnChallenge
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WebAuthnChallengesRepository : JpaRepository<WebAuthnChallenge, Long> {
    fun findByUser_EmailAndChallenge(userEmail: String, challenge: String): WebAuthnChallenge?
    fun findByChallenge(challenge: String): WebAuthnChallenge?
}