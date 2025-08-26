package fr.rguillemot.website.backend.type.Service

data class UserTokenResponse (
    val accessToken: String,
    val refreshToken: String
)