package lendrix.web.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import lendrix.web.app.entity.Card;

public interface CardRepository extends JpaRepository<Card, String>{

    Optional <Card> findByOwnerUid(String uid);

    boolean existsByCardNumber(String cardNumber);


}
