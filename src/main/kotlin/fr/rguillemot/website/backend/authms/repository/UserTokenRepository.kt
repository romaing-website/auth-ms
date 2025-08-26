package fr.rguillemot.website.backend.authms.repository

import org.springframework.data.jpa.repository.JpaRepository
import fr.rguillemot.website.backend.authms.model.UserToken
import org.springframework.stereotype.Repository

@Repository
interface UserTokenRepository : JpaRepository<UserToken, Long> {
   // fun findByRefreshTokenAndDeviceId(refreshToken: String, deviceId: String): UserToken?
}