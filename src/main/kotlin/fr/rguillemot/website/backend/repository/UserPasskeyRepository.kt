package fr.rguillemot.website.backend.repository

import fr.rguillemot.website.backend.model.User
import org.springframework.data.jpa.repository.JpaRepository
import fr.rguillemot.website.backend.model.UserPasskey
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserPasskeyRepository : JpaRepository<UserPasskey, Long> {
    fun  findByUser_Email(userEmail: String): UserPasskey?
    fun findByCredentialId(credentielId: String): UserPasskey?

    @Query("SELECT u.user FROM UserPasskey u WHERE u.credentialId = :credentialId")
    fun findUserByCredentialId(credentialId: String): User?

}
