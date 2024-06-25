package rw.bnr.banking.v1.serviceImpls;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rw.bnr.banking.v1.enums.ETransactionType;
import rw.bnr.banking.v1.exceptions.BadRequestException;
import rw.bnr.banking.v1.exceptions.ResourceNotFoundException;
import rw.bnr.banking.v1.models.BankingTransaction;
import rw.bnr.banking.v1.models.Customer;
import rw.bnr.banking.v1.models.Role;
import rw.bnr.banking.v1.payload.request.CreateTransactionDTO;
import rw.bnr.banking.v1.repositories.IBankingTransactionRepository;
import rw.bnr.banking.v1.services.IBankingTransactionService;
import rw.bnr.banking.v1.services.ICustomerService;
import rw.bnr.banking.v1.standalone.MailService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankingTransactionService implements IBankingTransactionService {

    private final IBankingTransactionRepository bankingTransactionRepository;
    private final ICustomerService customerService;
    private final EntityManager em;
    private final MailService mailService;


    @Override
    public BankingTransaction createTransaction(CreateTransactionDTO dto) {
        Customer customer = this.customerService.getLoggedInCustomer();
        BankingTransaction transaction = new BankingTransaction();
        if (dto.getTransactionType() == ETransactionType.WITHDRAW) {
            if (customer.getBalance() < dto.getAmount()) {
                throw new BadRequestException("Insufficient balance");
            }
            customer.setBalance(customer.getBalance() - dto.getAmount());
        } else if (dto.getTransactionType() == ETransactionType.SAVING) {
            customer.setBalance(customer.getBalance() + dto.getAmount());
        } else if (dto.getTransactionType() == ETransactionType.TRANSFER && dto.getSendTo() != null) {
            if (customer.getBalance() < dto.getAmount()) {
                throw new BadRequestException("Insufficient balance");
            }
            customer.setBalance(customer.getBalance() - dto.getAmount());
            Customer receiver = this.customerService.getById(dto.getSendTo());
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
    public Page<BankingTransaction> getAllTransactions(Pageable pageable, ETransactionType type, UUID customerId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<BankingTransaction> cr = cb.createQuery(BankingTransaction.class);
        Root<BankingTransaction> root = cr.from(BankingTransaction.class);

        // Query for count
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<BankingTransaction> countRoot = countQuery.from(BankingTransaction.class);
        countQuery.select(cb.count(countRoot));

        // List to hold predicates
        List<Predicate> predicates = new ArrayList<>();

        if (type != null) {
            Predicate typePredicate = cb.equal(root.get("transactionType"), type);
            predicates.add(typePredicate);
        }

        if (customerId != null) {
            Predicate customerPredicate = cb.equal(root.get("customer").get("id"), customerId);
            predicates.add(customerPredicate);
        }

        // Apply predicates to queries
        if (!predicates.isEmpty()) {
            Predicate combinedPredicate = cb.and(predicates.toArray(new Predicate[0]));
            cr.where(combinedPredicate);
            countQuery.where(combinedPredicate);
        }

        // Pagination
        TypedQuery<BankingTransaction> query = em.createQuery(cr);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<BankingTransaction> resultList = query.getResultList();
        Long count = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), count);
    }

    @Override
    public BankingTransaction getTransactionById(UUID id) {
        return this.bankingTransactionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Transaction", "id", id.toString()));
    }
}
