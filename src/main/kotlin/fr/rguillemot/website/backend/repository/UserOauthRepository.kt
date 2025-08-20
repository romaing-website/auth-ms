import org.springframework.data.jpa.repository.JpaRepository
import fr.rguillemot.website.backend.model.UserOauth

interface UserOauthRepository : JpaRepository<UserOauth, Long>