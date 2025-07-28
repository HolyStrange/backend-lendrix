package lendrix.web.app.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.naming.OperationNotSupportedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lendrix.web.app.entity.*;
import lendrix.web.app.enums.*;
import lendrix.web.app.repository.*;
import lendrix.web.app.service.helper.AccountHelper;
import lendrix.web.app.util.RandomUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardService {

        private final AccountRepository accountRepository;
        private final CardRepository cardRepository;
        private final TransactionRepository transactionRepository;
        private final AccountHelper accountHelper;

        /**
        Creates a new card for the user in the specified currency.
        */
        @Transactional
        public Card createCard(BigDecimal amount, User user, String billingAddress, int pin, String currencyCode)
        throws OperationNotSupportedException {

        // Validate amount
        if (amount == null || amount.compareTo(BigDecimal.valueOf(2)) < 0) {
        throw new IllegalArgumentException("Amount must be at least 2");
        }

        // Check if user has an account for the requested currency
        Account account = accountRepository.findByCodeAndOwnerUid(currencyCode, user.getUid())
                .orElseThrow(() -> new IllegalArgumentException("No " + currencyCode + " account found for user"));

        // Validate funds
        accountHelper.validateSufficientFunds(account, amount);

        // Deduct funds from account to fund the card
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        // Log transaction for withdrawal
        accountHelper.createAccountTransaction(
                1,
                Type.WITHDRAW,
                BigDecimal.ZERO,
                user,
                account,
                amount,
                "Card creation fee"
        );

        // Generate unique card number
        String cardNumber;
        do {
        cardNumber = generateCardNumber();
        } while (cardRepository.existsByCardNumber(cardNumber));

        // Build and save card
        Card card = Card.builder()
                .cardNumber(cardNumber)
                .cardHolder(user.getFirstname() + " " + user.getLastname())
                .balance(amount)
                .owner(user)
                .billingAddress(billingAddress)
                .pin(pin)
                .cvv((int) new RandomUtil().generateRandom(3))
                .exp(LocalDateTime.now().plusYears(3))
                .currency(currencyCode)
                .build();

        cardRepository.save(card);
        cardRepository.flush(); // Ensure card is written to DB before transaction references it

        //Log credit to card account
        accountHelper.createAccountTransaction(
                1,
                Type.CREDIT,
                BigDecimal.ZERO,
                user,
                account,
                amount,
                "Card funded"
        );

        //Now safely create card transaction record
        createCardTransaction(amount, user, card, BigDecimal.ZERO, Type.CREDIT);

        return card;
        }

        /**
        Generates a random 16-digit card number.
     */
        private String generateCardNumber() {
        return String.valueOf(new RandomUtil().generateRandom(16));
        }

        /**
        Adds money to the card (and syncs to user account in the same currency).
        */
        @Transactional
        public Transaction creditCard(BigDecimal amount, User user) {
        Card card = getCard(user);

        //update card balance
        card.setBalance(card.getBalance().add(amount));
        cardRepository.save(card);
        cardRepository.flush(); //flush to DB before creating transaction

        //update matching account balance
        Account account = accountRepository.findByCodeAndOwnerUid(card.getCurrency(), user.getUid())
                .orElseThrow();
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        //log account credit transaction
        accountHelper.createAccountTransaction(
                1,
                Type.CREDIT,
                BigDecimal.ZERO,
                user,
                account,
                amount,
                "Card credited"
        );

        //log card transaction
        return createCardTransaction(amount, user, card, BigDecimal.ZERO, Type.CREDIT);
        }

        /**
        Deducts money from the card 
        */
        @Transactional
        public Transaction debitCard(BigDecimal amount, User user) {
        Card card = getCard(user);

        //update card balance
        card.setBalance(card.getBalance().subtract(amount));
        cardRepository.save(card);
        cardRepository.flush(); //lush to DB before creating transaction

        //update matching account balance
        Account account = accountRepository.findByCodeAndOwnerUid(card.getCurrency(), user.getUid())
                .orElseThrow();
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        //log account debit transaction
        accountHelper.createAccountTransaction(
                1,
                Type.WITHDRAW,
                BigDecimal.ZERO,
                user,
                account,
                amount,
                "Card debited"
        );

        //log card transaction
        return createCardTransaction(amount, user, card, BigDecimal.ZERO, Type.WITHDRAW);
        }

        /**
        Creates a transaction record for card operations (credit/debit).
        */
        private Transaction createCardTransaction(BigDecimal amount, User user, Card card, BigDecimal txFee, Type type) {
        Transaction tx = Transaction.builder()
                .card(card)
                .owner(user)
                .amount(amount)
                .txFee(txFee)
                .status(Status.COMPLETED)
                .type(type)
                .build();
        return transactionRepository.save(tx);
        }

        /**
        Gets the user’s card (assumes one card per user for now).
        */
        public Card getCard(User user) {
        return cardRepository.findByOwnerUid(user.getUid())
                .orElseThrow(() -> new IllegalArgumentException("No card found for this user"));
        }

        /**
        Deletes the user's card.
        */
        @Transactional
        public void deleteCard(User user) {
        Card card = cardRepository.findByOwnerUid(user.getUid())
                .orElseThrow(() -> new IllegalArgumentException("No card found for this user"));
        cardRepository.delete(card);
        }
}
