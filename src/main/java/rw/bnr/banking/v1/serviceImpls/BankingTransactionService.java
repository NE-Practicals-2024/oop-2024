package rw.bnr.banking.v1.serviceImpls;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
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
    public BankingTransaction createTransaction(CreateTransactionDTO dto, String receiverAccount) {
        Customer customer = this.customerService.getLoggedInCustomer();
        BankingTransaction transaction = new BankingTransaction();
        if (dto.getTransactionType() == ETransactionType.WITHDRAW) {
            if (customer.getBalance() < dto.getAmount()) {
                throw new BadRequestException("Insufficient balance");
            }
            customer.setBalance(customer.getBalance() - dto.getAmount());
        } else if (dto.getTransactionType() == ETransactionType.SAVING) {
            customer.setBalance(customer.getBalance() + dto.getAmount());
        } else if (dto.getTransactionType() == ETransactionType.TRANSFER && receiverAccount != null) {
            if (customer.getBalance() < dto.getAmount()) {
                throw new BadRequestException("Insufficient balance");
            }
            customer.setBalance(customer.getBalance() - dto.getAmount());
            Customer receiver = this.customerService.findByAccountCode(receiverAccount).orElseThrow(() -> new ResourceNotFoundException("Customer", "account", receiverAccount));
            if (receiver.getId().equals(customer.getId())) {
                throw new BadRequestException("You can't transfer to yourself");
            }
            receiver.setBalance(receiver.getBalance() + dto.getAmount());
            this.customerService.save(receiver);
            transaction.setReceiver(receiver);
        } else {
            if (dto.getTransactionType() == ETransactionType.TRANSFER) {
                throw new BadRequestException("Receiver id is required");
            }
            throw new BadRequestException("Invalid transaction type");
        }
        this.customerService.save(customer);
        transaction.setAmount(dto.getAmount());
        transaction.setTransactionType(dto.getTransactionType());
        transaction.setCustomer(customer);
        if (dto.getTransactionType() == ETransactionType.SAVING) {
            mailService.sendSavingsStoredSuccessfullyEmail(customer.getEmail(), customer.getFullName(), dto.getAmount().toString(), String.valueOf(customer.getBalance()), customer.getAccount());
        } else if (dto.getTransactionType() == ETransactionType.WITHDRAW) {
            mailService.sendWithdrawalSuccessfulEmail(customer.getEmail(), customer.getFullName(), dto.getAmount().toString(), String.valueOf(customer.getBalance()), customer.getAccount());
        } else if (dto.getTransactionType() == ETransactionType.TRANSFER) {
            mailService.sendTransferSuccessfulEmail(customer.getEmail(), customer.getFullName(), dto.getAmount().toString(), String.valueOf(customer.getBalance()), transaction.getReceiver().getFullName(), customer.getAccount());
            mailService.sendReceivedAmountEmail(transaction.getReceiver().getEmail(), transaction.getReceiver().getFullName(), customer.getFullName(), dto.getAmount().toString(), String.valueOf(transaction.getReceiver().getBalance()));
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
