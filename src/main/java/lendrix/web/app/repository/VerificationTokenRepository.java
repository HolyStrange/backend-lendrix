package lendrix.web.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import lendrix.web.app.entity.VerificationToken;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, String> {
    Optional<VerificationToken> findByToken(String token);
}
