package lendrix.web.app.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import lendrix.web.app.entity.Transaction;
import lendrix.web.app.repository.TransactionRepository;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    public static final BigDecimal DAILY_LIMIT = new BigDecimal("2000.00");
    public static final BigDecimal WEEKLY_LIMIT = new BigDecimal("10000.00");

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    //Gets all transactions where the user is either the sender or receiver.
    public List<Transaction> getUserTransactionHistory(String username) {
        return transactionRepository.findBySenderOrReceiver(username, username);
    }

    //Gets all transactions owned by the user
    public List<Transaction> getUserOwnedTransactions(String username) {
        return transactionRepository.findByOwner_Username(username);
    }

    //Checks if a transaction keeps the user within their daily transfer limit.
    public boolean isWithinDailyLimit(String username, BigDecimal amount) {
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        BigDecimal totalSent = transactionRepository.sumSentAmountSince(username, since);
        return totalSent.add(amount).compareTo(DAILY_LIMIT) <= 0;
    }

    //Checks if a transaction keeps the user within their weekly transfer limit
    public boolean isWithinWeeklyLimit(String username, BigDecimal amount) {
        LocalDateTime since = LocalDateTime.now().minusWeeks(1);
        BigDecimal totalSent = transactionRepository.sumSentAmountSince(username, since);
        return totalSent.add(amount).compareTo(WEEKLY_LIMIT) <= 0;
    }
}