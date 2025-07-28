package lendrix.web.app.repository;

import lendrix.web.app.entity.MoneyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MoneyRequestRepository extends JpaRepository<MoneyRequest, String> {
    List<MoneyRequest> findByRecipient_Username(String username);
    List<MoneyRequest> findByRequester_Username(String username);
} 