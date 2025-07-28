package lendrix.web.app.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.springframework.stereotype.Service;

import lendrix.web.app.entity.Account;
import lendrix.web.app.entity.MoneyRequest;
import lendrix.web.app.entity.User;
import lendrix.web.app.repository.AccountRepository;
import lendrix.web.app.repository.MoneyRequestRepository;
import lendrix.web.app.repository.UserRepository;
import lendrix.web.app.service.helper.AccountHelper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MoneyRequestService {

    private final MoneyRequestRepository moneyRequestRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountHelper accountHelper;

    public MoneyRequest requestMoney(String requesterUsername, String recipientUsername, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        User requester = userRepository.findByUsername(requesterUsername)
                .orElseThrow(() -> new RuntimeException("Requester not found: " + requesterUsername));

        User recipient = userRepository.findByUsername(recipientUsername)
                .orElseThrow(() -> new RuntimeException("Recipient not found: " + recipientUsername));

        MoneyRequest request = MoneyRequest.builder()
                .requester(requester)
                .recipient(recipient)
                .amount(amount)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        return moneyRequestRepository.save(request);
    }

    public MoneyRequest respondToRequest(String requestId, boolean approve) throws OperationNotSupportedException {
        MoneyRequest request = moneyRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Money request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalArgumentException("This request has already been processed");
        }

        if (approve) {
            handleApprovedRequest(request);
            request.setStatus("APPROVED");
        } else {
            request.setStatus("REJECTED");
        }

        return moneyRequestRepository.save(request);
    }

    private void handleApprovedRequest(MoneyRequest request) throws OperationNotSupportedException {
        BigDecimal amount = request.getAmount();
        User recipient = request.getRecipient();  // the one paying
        User requester = request.getRequester();  // the one receiving

        //Assume all requests use USD for now
        Account recipientAccount = accountRepository.findByCodeAndOwnerUid("USD", recipient.getUid())
                .orElseThrow(() -> new IllegalArgumentException("Recipient has no USD account"));

        Account requesterAccount = accountRepository.findByCodeAndOwnerUid("USD", requester.getUid())
                .orElseThrow(() -> new IllegalArgumentException("Requester has no USD account"));

        // Validate funds before transfer
        accountHelper.validateSufficientFunds(recipientAccount, amount);

        // Use AccountHelper to move the money & log transactions
        accountHelper.performTransfer(recipientAccount, requesterAccount, amount, recipient);
    }

    public List<MoneyRequest> getRequestsForUser(String username) {
        return moneyRequestRepository.findByRecipient_Username(username);
    }

    public List<MoneyRequest> getRequestsByUser(String username) {
        return moneyRequestRepository.findByRequester_Username(username);
    }
}
