package lendrix.web.app.service.helper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.springframework.stereotype.Component;

import lendrix.web.app.dto.AccountDto;
import lendrix.web.app.dto.ConvertDto;
import lendrix.web.app.entity.Account;
import lendrix.web.app.entity.Transaction;
import lendrix.web.app.entity.User;
import lendrix.web.app.enums.Status;
import lendrix.web.app.enums.Type;
import lendrix.web.app.repository.AccountRepository;
import lendrix.web.app.repository.TransactionRepository;
import lendrix.web.app.service.ExchangeRateService;
import lendrix.web.app.util.RandomUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Getter
public class AccountHelper {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;

    private static final Map<String, String> CURRENCIES = Map.ofEntries(
        Map.entry("GHS", "Ghanaian Cedi"),
        Map.entry("USD", "United States Dollar"),
        Map.entry("EUR", "Euro"),
        Map.entry("GBP", "British Pound Sterling"),
        Map.entry("JPY", "Japanese Yen"),
        Map.entry("CHF", "Swiss Franc"),
        Map.entry("CAD", "Canadian Dollar"),
        Map.entry("AUD", "Australian Dollar"),
        Map.entry("NZD", "New Zealand Dollar"),
        Map.entry("CNY", "Chinese Yuan"),
        Map.entry("HKD", "Hong Kong Dollar"),
        Map.entry("SGD", "Singapore Dollar"),
        Map.entry("INR", "Indian Rupee"),
        Map.entry("ZAR", "South African Rand"),
        Map.entry("BRL", "Brazilian Real"),
        Map.entry("RUB", "Russian Ruble"),
        Map.entry("KRW", "South Korean Won"),
        Map.entry("SEK", "Swedish Krona"),
        Map.entry("NOK", "Norwegian Krone"),
        Map.entry("DKK", "Danish Krone"),
        Map.entry("MYR", "Malaysian Ringgit"),
        Map.entry("PHP", "Philippine Peso"),
        Map.entry("IDR", "Indonesian Rupiah"),
        Map.entry("THB", "Thai Baht"),
        Map.entry("MXN", "Mexican Peso"),
        Map.entry("TRY", "Turkish Lira"),
        Map.entry("AED", "United Arab Emirates Dirham"),
        Map.entry("SAR", "Saudi Riyal"),
        Map.entry("EGP", "Egyptian Pound"),
        Map.entry("NGN", "Nigerian Naira"),
        Map.entry("KES", "Kenyan Shilling"),
        Map.entry("TZS", "Tanzanian Shilling"),
        Map.entry("UGX", "Ugandan Shilling")
    );

    public Account createAccount(AccountDto accountDto, User user) throws Exception {
        validateAccountNonExistsForUser(accountDto.getCode(), user.getUid());

        long accountNumber;
        do {
            accountNumber = new RandomUtil().generateRandom(10);
        } while (accountRepository.existsByAccountNumber(accountNumber));

        var account = Account.builder()
                .accountNumber(accountNumber)
                .accountName(user.getFirstname() + " " + user.getLastname())
                .balance(BigDecimal.valueOf(1000))
                .owner(user)
                .code(accountDto.getCode())
                .symbol(accountDto.getSymbol())
                .label(CURRENCIES.getOrDefault(accountDto.getCode(), "Unknown Currency"))
                .build();

        return accountRepository.save(account);
    }

    public Transaction performTransfer(Account senderAccount, Account receiverAccount, BigDecimal amount, User user) {
        BigDecimal fee = amount.multiply(BigDecimal.valueOf(0.01));
        BigDecimal totalDeduction = amount.add(fee);

        if (senderAccount.getBalance().compareTo(totalDeduction) < 0) {
            throw new IllegalArgumentException("Insufficient balance including fees");
        }

        senderAccount.setBalance(senderAccount.getBalance().subtract(totalDeduction));
        receiverAccount.setBalance(receiverAccount.getBalance().add(amount));

        accountRepository.saveAll(List.of(senderAccount, receiverAccount));

        Transaction senderTransaction = createAccountTransaction(1, Type.WITHDRAW, fee, user, senderAccount, amount, "Transfer to account " + receiverAccount.getAccountNumber());
        Transaction receiverTransaction = createAccountTransaction(1, Type.DEPOSIT, BigDecimal.ZERO, user, receiverAccount, amount, "Received from account " + senderAccount.getAccountNumber());

        transactionRepository.save(receiverTransaction);

        return senderTransaction;
    }

    public void validateAccountNonExistsForUser(String code, String uid) throws Exception {
        if (accountRepository.existsByCodeAndOwnerUid(code, uid)) {
            throw new Exception("Account of this type already exists for this user");
        }
    }

    public void validateAccountOwner(Account account, User user) throws OperationNotSupportedException {
        if (!account.getOwner().getUid().equals(user.getUid())) {
            throw new OperationNotSupportedException("Invalid account owner");
        }
    }

    public void validateSufficientFunds(Account account, BigDecimal amount) throws OperationNotSupportedException {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new OperationNotSupportedException("Insufficient funds in the account");
        }
    }

    public void validateAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid amount.");
        }
    }

    public void validateDifferentCurrencyType(ConvertDto convertDto) {
        if (convertDto.getFromCurrency().equals(convertDto.getToCurrency())) {
            throw new IllegalArgumentException("Cannot convert between the same currency.");
        }
    }

    public void validateAccountOwnership(ConvertDto convertDto, String uid) throws Exception {
        accountRepository.findByCodeAndOwnerUid(convertDto.getFromCurrency(), uid)
            .orElseThrow(() -> new Exception("Source account not found"));
        accountRepository.findByCodeAndOwnerUid(convertDto.getToCurrency(), uid)
            .orElseThrow(() -> new Exception("Target account not found"));
    }

    public void validateConversion(ConvertDto convertDto, String uid) throws Exception {
        validateDifferentCurrencyType(convertDto);
        validateAccountOwnership(convertDto, uid);
        validateAmount(convertDto.getAmount());

        Account fromAccount = accountRepository.findByCodeAndOwnerUid(convertDto.getFromCurrency(), uid)
            .orElseThrow(() -> new Exception("From account not found"));

        validateSufficientFunds(fromAccount, BigDecimal.valueOf(convertDto.getAmount()));
    }

    public Transaction convertCurrency(ConvertDto convertDto, User user) throws Exception {
        validateConversion(convertDto, user.getUid());

        var rates = exchangeRateService.getRates();
        double fromRate = rates.get(convertDto.getFromCurrency());
        double toRate = rates.get(convertDto.getToCurrency());
        double computedAmount = (toRate / fromRate) * convertDto.getAmount();

        Account fromAccount = accountRepository.findByCodeAndOwnerUid(convertDto.getFromCurrency(), user.getUid())
            .orElseThrow(() -> new Exception("From account not found"));
        Account toAccount = accountRepository.findByCodeAndOwnerUid(convertDto.getToCurrency(), user.getUid())
            .orElseThrow(() -> new Exception("To account not found"));

        BigDecimal sendAmount = BigDecimal.valueOf(convertDto.getAmount());
        BigDecimal receivedAmount = BigDecimal.valueOf(computedAmount);
        BigDecimal conversionFee = sendAmount.multiply(BigDecimal.valueOf(0.01));

        fromAccount.setBalance(fromAccount.getBalance().subtract(sendAmount.add(conversionFee)));
        toAccount.setBalance(toAccount.getBalance().add(receivedAmount));

        accountRepository.saveAll(List.of(fromAccount, toAccount));

        Transaction fromTransaction = createAccountTransaction(1, Type.CONVERSION, conversionFee, user, fromAccount, sendAmount, "Currency conversion");
        Transaction toTransaction = createAccountTransaction(1, Type.DEPOSIT, BigDecimal.ZERO, user, toAccount, receivedAmount, "Converted currency");

        transactionRepository.save(toTransaction);

        return fromTransaction;
    }

    public Transaction createAccountTransaction(
            int status,
            Type type,
            BigDecimal txFee,
            User user,
            Account account,
            BigDecimal amount,
            String description
    ) {
        Transaction tx = Transaction.builder()
                .account(account)
                .owner(user)
                .amount(amount)
                .txFee(txFee)
                .status(status == 1 ? Status.COMPLETED : Status.FAILED)
                .type(type)
                .build();
        return transactionRepository.save(tx);
    }

    public Transaction depositToAccount(Account account, double amount, User user) {
    validateAmount(amount); // this ensure amount is > 0

    BigDecimal depositAmount = BigDecimal.valueOf(amount);

    //Add to account balance
    account.setBalance(account.getBalance().add(depositAmount));
    accountRepository.save(account);

    //Create a DEPOSIT transaction
    Transaction depositTransaction = createAccountTransaction(
            1,
            Type.DEPOSIT,
            BigDecimal.ZERO,   // No fees for deposits
            user,
            account,
            depositAmount,
            "Deposit of " + amount + " into " + account.getCode() + " account"
    );

    return depositTransaction;
}

}
