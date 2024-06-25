package rw.bnr.banking.v1.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.bnr.banking.v1.models.Message;

import java.util.UUID;

@Repository
public interface IMessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findAllByCustomerId(Pageable pageable, UUID customerId);

}
