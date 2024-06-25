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
import rw.bnr.banking.v1.exceptions.ResourceNotFoundException;
import rw.bnr.banking.v1.models.Message;
import rw.bnr.banking.v1.repositories.IMessageRepository;
import rw.bnr.banking.v1.services.IMessageService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService implements IMessageService {

    private final IMessageRepository messageRepository;
    private final EntityManager em;

    @Override
    public Message getMessageById(UUID id) {
        return this.messageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Message", "id", id.toString()));
    }

    @Override
    public Page<Message> findAllMessages(Pageable pageable, UUID customerId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Message> cr = cb.createQuery(Message.class);
        Root<Message> root = cr.from(Message.class);

        // Query for count
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Message> countRoot = countQuery.from(Message.class);
        countQuery.select(cb.count(countRoot));

        // List to hold predicates
        List<Predicate> predicates = new ArrayList<>();

        if (customerId != null) {
            Predicate predicate = cb.equal(root.get("customer").get("id"), customerId);
            predicates.add(predicate);
        }


        // Apply predicates to queries
        if (!predicates.isEmpty()) {
            Predicate combinedPredicate = cb.and(predicates.toArray(new Predicate[0]));
            cr.where(combinedPredicate);
            countQuery.where(combinedPredicate);
        }

        // Pagination
        TypedQuery<Message> query = em.createQuery(cr);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Message> resultList = query.getResultList();
        Long count = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), count);
    }

}
