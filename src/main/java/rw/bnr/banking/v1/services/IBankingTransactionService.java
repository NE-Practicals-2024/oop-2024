package rw.bnr.banking.v1.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rw.bnr.banking.v1.enums.ETransactionType;
import rw.bnr.banking.v1.models.BankingTransaction;
import rw.bnr.banking.v1.payload.request.CreateTransactionDTO;

import java.util.List;
import java.util.UUID;

public interface IBankingTransactionService {

    BankingTransaction createTransaction(CreateTransactionDTO dto, String receiverAccount);

    Page<BankingTransaction> getAllTransactions(Pageable pageable);

    Page<BankingTransaction> getAllTransactionsByCustomer(Pageable pageable, UUID customerId);

    Page<BankingTransaction> getAllTransactionsByType(Pageable pageable, ETransactionType type);

    BankingTransaction getTransactionById(UUID id);

    List<BankingTransaction> getAllTransactionsByCustomer(UUID customerId);

}
