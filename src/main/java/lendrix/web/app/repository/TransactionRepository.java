package lendrix.web.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import lendrix.web.app.entity.Transaction;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findBySenderOrReceiver(String sender, String receiver);
    List<Transaction> findByOwner_Username(String username);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.sender = :username AND t.createdAt >= :since")
    BigDecimal sumSentAmountSince(@Param("username") String username, @Param("since") LocalDateTime since);
}
