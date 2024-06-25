package rw.bnr.banking.v1.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rw.bnr.banking.v1.enums.ECustomerStatus;
import rw.bnr.banking.v1.enums.ERole;
import rw.bnr.banking.v1.models.Customer;
import rw.bnr.banking.v1.models.File;
import rw.bnr.banking.v1.payload.request.UpdateCustomerDTO;

import java.util.Optional;
import java.util.UUID;


public interface ICustomerService {

    Page<Customer> getAll(Pageable pageable, ERole role, String searchKey, ECustomerStatus status);

    Customer getById(UUID id);

    Customer create(Customer user);

    Customer save(Customer user);

    Customer update(UUID id, UpdateCustomerDTO dto);

    boolean delete(UUID id);

    Customer getLoggedInCustomer();

    Customer getByEmail(String email);

    Customer changeProfileImage(UUID id, File file);

    Customer removeProfileImage(UUID id);

    Optional<Customer> findByActivationCode(String verificationCode);

    Optional<Customer> findByAccountCode(String accountCode);

}