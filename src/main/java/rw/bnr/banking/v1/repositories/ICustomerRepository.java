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
import java.util.concurrent.ExecutionException;

@Repository
public interface ICustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findById(UUID userID);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByActivationCode(String activationCode);

    Optional<Customer> findByAccount(String accountCode);

    @Query("SELECT u FROM Customer u" +
            " WHERE (lower(u.firstName)  LIKE ('%' || lower(:searchKey) || '%')) " +
            " OR (lower(u.lastName) LIKE ('%' || lower(:searchKey) || '%')) " +
            " OR (lower(u.email) LIKE ('%' || lower(:searchKey) || '%'))")
    Page<Customer> search(Pageable pageable, String searchKey);

}
