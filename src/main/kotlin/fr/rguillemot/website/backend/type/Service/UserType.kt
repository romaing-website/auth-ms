package fr.rguillemot.website.backend.type.Service

import fr.rguillemot.website.backend.model.User

data class CreateResult(
    val status: Boolean,
    val message: String,
    val user: User?
)