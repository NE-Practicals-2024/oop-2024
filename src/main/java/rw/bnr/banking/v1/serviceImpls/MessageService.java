package rw.bnr.banking.v1.serviceImpls;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rw.bnr.banking.v1.exceptions.ResourceNotFoundException;
import rw.bnr.banking.v1.models.Message;
import rw.bnr.banking.v1.repositories.IMessageRepository;
import rw.bnr.banking.v1.services.IMessageService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService implements IMessageService {

    private final IMessageRepository messageRepository;

    @Override
    public Message getMessageById(UUID id) {
        return this.messageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Message", "id", id.toString()));
    }

    @Override
    public Page<Message> findAllMessages(Pageable pageable) {
        return this.messageRepository.findAll(pageable);
    }

    @Override
    public Page<Message> findAllMessagesByCustomer(Pageable pageable, UUID customerId) {
        return this.messageRepository.findAllByCustomerId(pageable, customerId);
    }

}
