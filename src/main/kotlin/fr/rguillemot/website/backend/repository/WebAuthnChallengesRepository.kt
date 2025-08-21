package fr.rguillemot.website.backend.repository

import fr.rguillemot.website.backend.model.WebAuthnChallenge
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface WebAuthnChallengesRepository : JpaRepository<WebAuthnChallenge, Long> {

    fun findByChallenge(challenge: String): WebAuthnChallenge?

    @Query("SELECT wc FROM WebAuthnChallenge wc WHERE wc.user.email = :userEmail AND wc.challenge = :challenge")
    fun findByUserEmailAndChallenge(userEmail: String, challenge: String): WebAuthnChallenge?

}