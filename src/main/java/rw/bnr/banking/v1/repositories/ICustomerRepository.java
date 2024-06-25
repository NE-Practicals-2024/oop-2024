package rw.bnr.banking.v1.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rw.bnr.banking.v1.enums.ERole;
import rw.bnr.banking.v1.models.Customer;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ICustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findById(UUID userID);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByActivationCode(String activationCode);
    Optional<Customer> findByAccount(String accountCode);


}
