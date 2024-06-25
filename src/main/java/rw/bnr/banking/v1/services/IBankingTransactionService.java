package rw.bnr.banking.v1.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rw.bnr.banking.v1.enums.ETransactionType;
import rw.bnr.banking.v1.models.BankingTransaction;
import rw.bnr.banking.v1.payload.request.CreateTransactionDTO;

import java.util.UUID;

public interface IBankingTransactionService {

    BankingTransaction createTransaction(CreateTransactionDTO dto);
    Page<BankingTransaction> getAllTransactions(Pageable pageable, ETransactionType type, UUID customerId);
    BankingTransaction getTransactionById(UUID id);

}
