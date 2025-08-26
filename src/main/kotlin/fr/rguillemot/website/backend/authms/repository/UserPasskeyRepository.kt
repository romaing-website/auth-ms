package fr.rguillemot.website.backend.authms.repository

import fr.rguillemot.website.backend.authms.model.User
import org.springframework.data.jpa.repository.JpaRepository
import fr.rguillemot.website.backend.authms.model.UserPasskey
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserPasskeyRepository : JpaRepository<UserPasskey, Long> {

    fun findByCredentialId(credentielId: String): UserPasskey?

    @Query("SELECT up FROM UserPasskey up WHERE up.user.email = :userEmail")
    fun findByUserEmail(userEmail: String): UserPasskey?

    @Query("SELECT u.user FROM UserPasskey u WHERE u.credentialId = :credentialId")
    fun findUserByCredentialId(credentialId: String): User?

}
