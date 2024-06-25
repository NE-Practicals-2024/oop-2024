package rw.bnr.banking.v1.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.bnr.banking.v1.models.BankingTransaction;

import java.util.UUID;

@Repository
public interface IBankingTransactionRepository extends JpaRepository<BankingTransaction, UUID> {

}
