package fr.rguillemot.website.backend.repository

import fr.rguillemot.website.backend.model.WebAuthnChallenge
import org.springframework.data.jpa.repository.JpaRepository

interface WebAuthnChallengesRepository : JpaRepository<WebAuthnChallenge, Long>