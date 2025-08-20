import org.springframework.data.jpa.repository.JpaRepository
import fr.rguillemot.website.backend.model.UserToken

interface UserTokenRepository : JpaRepository<UserToken, Long>