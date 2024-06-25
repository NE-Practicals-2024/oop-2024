package rw.bnr.banking.v1.serviceImpls;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rw.bnr.banking.v1.enums.ETransactionType;
import rw.bnr.banking.v1.exceptions.BadRequestException;
import rw.bnr.banking.v1.exceptions.ResourceNotFoundException;
import rw.bnr.banking.v1.models.BankingTransaction;
import rw.bnr.banking.v1.models.Customer;
import rw.bnr.banking.v1.payload.request.CreateTransactionDTO;
import rw.bnr.banking.v1.repositories.IBankingTransactionRepository;
import rw.bnr.banking.v1.services.IBankingTransactionService;
import rw.bnr.banking.v1.services.ICustomerService;
import rw.bnr.banking.v1.standalone.MailService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankingTransactionService implements IBankingTransactionService {

    private final IBankingTransactionRepository bankingTransactionRepository;
    private final ICustomerService customerService;
    private final MailService mailService;


    @Override
    public BankingTransaction createTransaction(CreateTransactionDTO dto, UUID receiverId) {
        Customer customer = this.customerService.getLoggedInCustomer();
        BankingTransaction transaction = new BankingTransaction();
        if (dto.getTransactionType() == ETransactionType.WITHDRAW) {
            if (customer.getBalance() < dto.getAmount()) {
                throw new BadRequestException("Insufficient balance");
            }
            customer.setBalance(customer.getBalance() - dto.getAmount());
        } else if (dto.getTransactionType() == ETransactionType.SAVING) {
            customer.setBalance(customer.getBalance() + dto.getAmount());
        } else if (dto.getTransactionType() == ETransactionType.TRANSFER && receiverId != null) {
            if (customer.getBalance() < dto.getAmount()) {
                throw new BadRequestException("Insufficient balance");
            }
            customer.setBalance(customer.getBalance() - dto.getAmount());
            Customer receiver = this.customerService.getById(receiverId);
            receiver.setBalance(receiver.getBalance() + dto.getAmount());
            this.customerService.save(receiver);
            transaction.setReceiver(receiver);
        } else {
            throw new BadRequestException("Invalid transaction type");
        }
        this.customerService.save(customer);
        transaction.setAmount(dto.getAmount());
        transaction.setTransactionType(dto.getTransactionType());
        transaction.setCustomer(customer);
        if (dto.getTransactionType() == ETransactionType.SAVING) {
            mailService.sendSavingsStoredSuccessfullyEmail(customer.getEmail(), customer.getFullName(), dto.getAmount().toString(), customer.getAccount());
        } else if (dto.getTransactionType() == ETransactionType.WITHDRAW) {
            mailService.sendWithdrawalSuccessfulEmail(customer.getEmail(), customer.getFullName(), dto.getAmount().toString(), customer.getAccount());
        }
        return this.bankingTransactionRepository.save(transaction);
    }

    @Override
    public Page<BankingTransaction> getAllTransactions(Pageable pageable) {
        return this.bankingTransactionRepository.findAll(pageable);
    }

    @Override
    public Page<BankingTransaction> getAllTransactionsByCustomer(Pageable pageable, UUID customerId) {
        return this.bankingTransactionRepository.findAllByCustomerId(pageable, customerId);
    }

    @Override
    public Page<BankingTransaction> getAllTransactionsByType(Pageable pageable, ETransactionType type) {
        return this.bankingTransactionRepository.findAllByTransactionType(pageable, type);
    }

    @Override
    public BankingTransaction getTransactionById(UUID id) {
        return this.bankingTransactionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Transaction", "id", id.toString()));
    }
}
