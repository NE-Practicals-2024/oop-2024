package rw.bnr.banking.v1.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import rw.bnr.banking.v1.enums.ETransactionType;
import rw.bnr.banking.v1.models.BankingTransaction;

@Repository
public interface IBankingTransactionRepository extends JpaRepository<BankingTransaction, UUID> {

    Page<BankingTransaction> findAllByCustomerId(Pageable pageable, UUID customerId);

    Page<BankingTransaction> findAllByTransactionType(Pageable pageable, ETransactionType type);

}
