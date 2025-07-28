package lendrix.web.app.controller;

import javax.naming.OperationNotSupportedException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lendrix.web.app.dto.AmountDto;
import lendrix.web.app.dto.CardDto;
import lendrix.web.app.entity.Card;
import lendrix.web.app.entity.Transaction;
import lendrix.web.app.entity.User;
import lendrix.web.app.service.CardService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/card")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    /**
    Fetch the user's card
     */
    @GetMapping
    public ResponseEntity<Card> getCard(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(cardService.getCard(user));
    }

    /**
    Create a new card in ANY currency (e.g. USD, EUR, GBP)
     */
    @PostMapping("/create")
    public ResponseEntity<?> createCard(@RequestBody CardDto cardDto,
                                        Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        try {
            Card card = cardService.createCard(
                    cardDto.getAmount(),
                    user,
                    cardDto.getBillingAddress(),
                    cardDto.getPin(),
                    cardDto.getCurrency()   //Added to support multiple currencies
            );
            return ResponseEntity.ok(card);
        } catch (OperationNotSupportedException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
    Credit (add funds) to the user's card
     */
    @PostMapping("/credit")
    public ResponseEntity<Transaction> creditCard(@RequestBody AmountDto amountDto,
                                                Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(cardService.creditCard(amountDto.getAmount(), user));
    }

    /**
    Debit (withdraw funds) from the user's card
     */
    @PostMapping("/debit")
    public ResponseEntity<Transaction> debitCard(@RequestBody AmountDto amountDto,
                                                Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(cardService.debitCard(amountDto.getAmount(), user));
    }

    /**
    Delete the user’s card
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteCard(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        try {
            cardService.deleteCard(user);
            return ResponseEntity.ok("Card deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
