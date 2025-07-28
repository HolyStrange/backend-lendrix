package lendrix.web.app.repository;

import java.util.List;
import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;

import lendrix.web.app.entity.Account;

public interface AccountRepository extends JpaRepository<Account, String > {

    boolean existsByAccountNumber(long accountNumber);

    boolean existsByCodeAndOwnerUid(String code, String uid);

    List<Account> findAllByOwnerUid(String uid);

    Optional<Account> findByCodeAndOwnerUid(String code, String uid); 

    Optional<Account> findByAccountNumber(long recipientAccountNumber); 
}
