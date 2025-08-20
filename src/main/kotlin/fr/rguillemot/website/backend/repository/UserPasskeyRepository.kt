import org.springframework.data.jpa.repository.JpaRepository
import fr.rguillemot.website.backend.model.UserPasskey

interface UserPasskeyRepository : JpaRepository<UserPasskey, Long>