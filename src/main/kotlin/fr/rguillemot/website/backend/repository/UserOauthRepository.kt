package fr.rguillemot.website.backend.repository

import org.springframework.data.jpa.repository.JpaRepository
import fr.rguillemot.website.backend.model.UserOauth
import org.springframework.stereotype.Repository


@Repository
interface UserOauthRepository : JpaRepository<UserOauth, Long>