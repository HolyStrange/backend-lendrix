package lendrix.web.app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lendrix.web.app.dto.AccountDto;
import lendrix.web.app.dto.ConvertDto;
import lendrix.web.app.dto.DepositDto;
import lendrix.web.app.dto.TransferDto;
import lendrix.web.app.entity.Account;
import lendrix.web.app.entity.Transaction;
import lendrix.web.app.entity.User;
import lendrix.web.app.service.AccountService;
import lendrix.web.app.service.TransactionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    /**
    Create a new account for the logged-in user.
    Prevents duplicate currency accounts.
     */
    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody AccountDto accountDto, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        try {
            Account account = accountService.createAccount(accountDto, user);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
    Get all accounts for the logged-in user.
     */
    @GetMapping
    public ResponseEntity<List<Account>> getUserAccounts(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(accountService.getUserAccounts(user.getUid()));
    }

    /**
    Transfer funds between accounts (user-to-user or own accounts).
    Includes fraud prevention: daily & weekly limits.
     */
    @PostMapping("/transfer")
    public ResponseEntity<?> transferFunds(@RequestBody TransferDto transferDto, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        try {
            // Fraud prevention checks
            if (!transactionService.isWithinDailyLimit(user.getUsername(), transferDto.getAmount())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Daily transfer limit exceeded.");
            }

            if (!transactionService.isWithinWeeklyLimit(user.getUsername(), transferDto.getAmount())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Weekly transfer limit exceeded.");
            }

            // Perform transfer
            Transaction transaction = accountService.transferFunds(transferDto, user);
            return ResponseEntity.ok(transaction);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
    Get latest exchange rates.
     */
    @GetMapping("/rates")
    public ResponseEntity<Map<String, Double>> getExchangeRate() {
        return ResponseEntity.ok(accountService.getExchangeRate());
    }

    /**
    Convert currency within the user's own accounts.
     */
    @PostMapping("/convert")
    public ResponseEntity<?> convertCurrency(@RequestBody ConvertDto convertDto, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        try {
            Transaction transaction = accountService.convertCurrency(convertDto, user);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
    Deposit money into an account (for later Stripe integration).
     */
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody DepositDto depositDto, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        try {
            Transaction transaction = accountService.deposit(depositDto, user);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
