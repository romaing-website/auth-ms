package fr.rguillemot.website.backend.service

import fr.rguillemot.website.backend.model.User
import fr.rguillemot.website.backend.repository.UserPasskeyRepository
import org.springframework.stereotype.Service

@Service
class UserPassKeyService(
    private val userPassKeyRepository: UserPasskeyRepository
) {

    fun getUser(credentialId: String): User? {
        val user =  userPassKeyRepository.findUserByCredentialId(credentialId)
        return user
    }
}