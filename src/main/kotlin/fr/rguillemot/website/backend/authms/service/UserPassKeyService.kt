package fr.rguillemot.website.backend.authms.service

import fr.rguillemot.website.backend.authms.model.User
import fr.rguillemot.website.backend.authms.repository.UserPasskeyRepository
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