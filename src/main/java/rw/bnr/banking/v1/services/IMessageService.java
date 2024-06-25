package rw.bnr.banking.v1.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rw.bnr.banking.v1.models.Message;

import java.util.UUID;

public interface IMessageService {

    Message getMessageById(UUID id);

    Page<Message> findAllMessages(Pageable pageable);
    Page<Message> findAllMessagesByCustomer(Pageable pageable,UUID customerId);

}
