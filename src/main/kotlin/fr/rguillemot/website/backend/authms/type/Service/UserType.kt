package fr.rguillemot.website.backend.authms.type.Service

import fr.rguillemot.website.backend.authms.model.User

data class CreateResult(
    val status: Boolean,
    val message: String,
    val user: User?
)