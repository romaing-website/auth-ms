package fr.rguillemot.website.backend.authms.type.Service

data class UserTokenResponse (
    val accessToken: String,
    val refreshToken: String
)