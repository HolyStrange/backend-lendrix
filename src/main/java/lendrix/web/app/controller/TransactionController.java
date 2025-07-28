package lendrix.web.app.controller;

import lendrix.web.app.entity.Transaction;
import lendrix.web.app.service.TransactionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/history/{username}")
    public List<Transaction> getUserTransactionHistory(@PathVariable String username) {
        return transactionService.getUserTransactionHistory(username);
    }
} 