package fr.rguillemot.website.backend.authms.service

import com.nimbusds.jose.jwk.source.ImmutableSecret
import fr.rguillemot.website.backend.authms.model.User
import fr.rguillemot.website.backend.authms.model.UserToken
import fr.rguillemot.website.backend.authms.repository.UserTokenRepository
import fr.rguillemot.website.backend.authms.type.Service.UserTokenResponse
import fr.rguillemot.website.backend.authms.utils.b64urlEncode
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import javax.crypto.spec.SecretKeySpec

@Service
class UserTokenService(
    @Value("\${jwt.secret}") private val secret: String,
    private val userTokenRepository: UserTokenRepository
) {
     fun createToken(user: User, ip: String, userAgent: String, language: String, timezone: String): UserTokenResponse {
        val deviceID = generateDeviceID(ip, language, timezone, userAgent)
        val refreshToken = createToken("refresh", deviceID, user)
        val accessToken = createToken("access", deviceID, user)
        // Save token in database
        val userToken = UserToken(
            user = user,
            deviceId = deviceID,
            token = accessToken,
            refreshToken = refreshToken,
        )
        try {
            userTokenRepository.save(userToken)
        } catch (e: Exception) {
            throw Exception("Error saving token: ${e.message}")
        }
        return UserTokenResponse(accessToken, refreshToken)

    }
/*
     fun refreshToken(refreshToken: String, ip: String, userAgent: String, language: String, timezone: String): UserTokenResponse {
        val deviceID = generateDeviceID(ip, language, timezone, userAgent)
        val existingToken = userTokenRepository.findByRefreshTokenAndDeviceId(refreshToken, deviceID)
            ?: throw Exception("Invalid token or device ID")
        val user = existingToken.user
        // Create new tokens
        val newRefreshToken = createToken("refresh", deviceID, user)
        val newAccessToken = createToken("access", deviceID, user)
        // Update token in database
        existingToken.token = newAccessToken
        existingToken.refreshToken = newRefreshToken
        existingToken.issuedAt = Instant.now()
        try {
            userTokenRepository.save(existingToken)
        } catch (e: Exception) {
            throw Exception("Error saving token: ${e.message}")
        }
        return UserTokenResponse(newAccessToken, newRefreshToken)

    }*/

    private fun generateDeviceID(ip: String, language: String, timezone: String, userAgent: String): String {
        val raw = "$ip|$language|$timezone|$userAgent"
        val encodedString = b64urlEncode(raw.toByteArray())
        return encodedString
    }

    private fun createToken(type: String, deviceID: String, user: User): String {
        val time = if(type === "access") 3600L else 2592000L
        val claims = JwtClaimsSet.builder()
            .subject(user.id.toString())
            .claim("deviceID", deviceID)
            .claim("role", user.role)
            .claim("avatar", user.avatarUrl)
            .claim("firstName", user.firstName)
            .claim("lastName", user.lastName)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(time))
            .build()
        val secretKey = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        val encoder = NimbusJwtEncoder(ImmutableSecret(secretKey))

        return encoder.encode(JwtEncoderParameters.from(claims)).tokenValue

    }

}