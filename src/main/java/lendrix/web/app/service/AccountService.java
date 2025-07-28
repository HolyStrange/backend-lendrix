package lendrix.web.app.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lendrix.web.app.dto.AccountDto;
import lendrix.web.app.dto.ConvertDto;
import lendrix.web.app.dto.DepositDto;
import lendrix.web.app.dto.TransferDto;
import lendrix.web.app.entity.Account;
import lendrix.web.app.entity.Transaction;
import lendrix.web.app.entity.User;
import lendrix.web.app.enums.Status;
import lendrix.web.app.enums.Type;
import lendrix.web.app.repository.AccountRepository;
import lendrix.web.app.repository.TransactionRepository;
import lendrix.web.app.service.helper.AccountHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountHelper accountHelper;
    private final ExchangeRateService exchangeRateService;
    private final TransactionRepository transactionRepository;

    /**
    Create a new account for a user
     */
    public Account createAccount(AccountDto accountDto, User user) throws Exception {
        if (accountDto == null || accountDto.getCode() == null) {
            throw new IllegalArgumentException("Account type (currency code) is required");
        }
        log.info("Creating {} account for user {}", accountDto.getCode(), user.getUsername());
        return accountHelper.createAccount(accountDto, user);
    }

    /**
    Get all accounts for a specific user
     */
    public List<Account> getUserAccounts(String uid) {
        log.info("Fetching accounts for user: {}", uid);
        return accountRepository.findAllByOwnerUid(uid);
    }

    /**
    Transfer funds between accounts
     */
    public Transaction transferFunds(TransferDto transferDto, User user) {
        // Get sender account using code and user ID
        var senderAccount = accountRepository
                .findByCodeAndOwnerUid(transferDto.getSenderAccountCode(), user.getUid())
                .orElseThrow(() -> new IllegalArgumentException("Sender account not found"));

        // Convert recipientAccountNumber from String to long
        long recipientAccountNumber;
        try {
            recipientAccountNumber = Long.parseLong(transferDto.getRecipientAccountNumber());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Recipient account number must be numeric");
        }

        // Get receiver account using account number
        var receiverAccount = accountRepository
                .findByAccountNumber(recipientAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Receiver account not found"));

        log.info("Transferring {} from {} to {}", transferDto.getAmount(), senderAccount.getCode(), receiverAccount.getCode());

        // Perform transfer via helper
        return accountHelper.performTransfer(senderAccount, receiverAccount, transferDto.getAmount(), user);
    }

    /**
    Get exchange rates (for currency conversion)
     */
    public Map<String, Double> getExchangeRate() {
        log.info("Fetching exchange rates...");
        return exchangeRateService.getRates();
    }

    /**
    Convert currency between two user-owned accounts
     */
    public Transaction convertCurrency(ConvertDto convertDto, User user) throws Exception {
        log.info("Converting {} from {} to {} for user {}",
                convertDto.getAmount(), convertDto.getFromCurrency(), convertDto.getToCurrency(), user.getUsername());
        return accountHelper.convertCurrency(convertDto, user);
    }

    /**
    Deposit money into a user's account (manual, will later connect to Stripe)
     */
    @Transactional
    public Transaction deposit(DepositDto depositDto, User user) {
        // Find the account by currency code and user
        Account account = accountRepository
                .findByCodeAndOwnerUid(depositDto.getAccountCode(), user.getUid())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found for code: " + depositDto.getAccountCode()));

        // Validate deposit amount
        if (depositDto.getAmount() == null || depositDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        // Update the account balance
        BigDecimal newBalance = account.getBalance().add(depositDto.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        log.info("Deposited {} into {} account for user {}", depositDto.getAmount(), account.getCode(), user.getUsername());

        // Create and save a transaction record for the deposit
        Transaction transaction = Transaction.builder()
                .type(Type.DEPOSIT)
                .amount(depositDto.getAmount())
                .txFee(BigDecimal.ZERO)   //No fee for deposit
                .sender("External")       //Placeholder (Stripe later)
                .receiver(account.getCode())
                .owner(user)
                .status(Status.COMPLETED)
                .account(account)
                .build();

        return transactionRepository.save(transaction);
    }
}
