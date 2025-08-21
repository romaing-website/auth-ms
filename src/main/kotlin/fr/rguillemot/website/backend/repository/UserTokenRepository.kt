package fr.rguillemot.website.backend.repository

import org.springframework.data.jpa.repository.JpaRepository
import fr.rguillemot.website.backend.model.UserToken
import org.springframework.stereotype.Repository

@Repository
interface UserTokenRepository : JpaRepository<UserToken, Long>