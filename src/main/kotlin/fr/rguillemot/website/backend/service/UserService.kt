package fr.rguillemot.website.backend.service

import fr.rguillemot.website.backend.model.User
import fr.rguillemot.website.backend.repository.UserRepository
import fr.rguillemot.website.backend.type.ApiResponse
import fr.rguillemot.website.backend.type.Service.CreateResult
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    private val allowedContentTypes = setOf("image/png", "image/jpeg", "image/webp")

    fun create(user: User, avatar: MultipartFile?): CreateResult {
        var isAvatar = true

        // Validate the Avatar file
        if (avatar == null || avatar.isEmpty) {
            isAvatar = false
        }
        if (isAvatar && avatar?.contentType !in allowedContentTypes) {
            return CreateResult(false, "Only PNG, JPEG, and WEBP images are allowed", null)
        }
        if (isAvatar && avatar?.size!! > 8 * 1024 * 1024) {
            return CreateResult(false, "Avatar size must not exceed 8MB", null)
        }
        // Check if the user already exists
        if (userRepository.existsByEmail(user.email)) {
            return CreateResult(false, "User with this email already exists", null)
        }
        // Save user to database
        try {
            val savedUser: User = userRepository.save(user)
            return CreateResult(true, "User created successfully", savedUser)
        } catch (e: Exception) {
            return CreateResult(false, "Unexcepted error during user creation", null)
        }

    }
}