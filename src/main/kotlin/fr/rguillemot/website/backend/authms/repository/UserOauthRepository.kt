package fr.rguillemot.website.backend.authms.repository

import org.springframework.data.jpa.repository.JpaRepository
import fr.rguillemot.website.backend.authms.model.UserOauth
import org.springframework.stereotype.Repository


@Repository
interface UserOauthRepository : JpaRepository<UserOauth, Long>