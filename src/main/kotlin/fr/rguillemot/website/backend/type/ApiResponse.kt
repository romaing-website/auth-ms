package fr.rguillemot.website.backend.type

data class ApiResponse<T>(
    val status: String,
    val message: String? = null,
    val data: T? = null
)
