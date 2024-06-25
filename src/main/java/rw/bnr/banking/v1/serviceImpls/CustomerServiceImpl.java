package rw.bnr.banking.v1.serviceImpls;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import rw.bnr.banking.v1.enums.ECustomerStatus;
import rw.bnr.banking.v1.enums.ERole;
import rw.bnr.banking.v1.exceptions.BadRequestException;
import rw.bnr.banking.v1.exceptions.ResourceNotFoundException;
import rw.bnr.banking.v1.models.Customer;
import rw.bnr.banking.v1.models.File;
import rw.bnr.banking.v1.models.Role;
import rw.bnr.banking.v1.payload.request.UpdateCustomerDTO;
import rw.bnr.banking.v1.repositories.IRoleRepository;
import rw.bnr.banking.v1.repositories.ICustomerRepository;
import rw.bnr.banking.v1.services.ICustomerService;
import rw.bnr.banking.v1.services.IFileService;
import rw.bnr.banking.v1.standalone.FileStorageService;
import rw.bnr.banking.v1.utils.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements ICustomerService {

    private final ICustomerRepository userRepository;
    private final IFileService fileService;
    private final FileStorageService fileStorageService;
    private final EntityManager em;
    private final IRoleRepository roleRepository;

    @Override
    public Page<Customer> getAll(Pageable pageable, ERole role, String searchKey, ECustomerStatus status) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Customer> cr = cb.createQuery(Customer.class);
        Root<Customer> root = cr.from(Customer.class);

        // Query for count
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Customer> countRoot = countQuery.from(Customer.class);
        countQuery.select(cb.count(countRoot));

        // List to hold predicates
        List<Predicate> predicates = new ArrayList<>();

        if (searchKey != null && !searchKey.isEmpty()) {
            String searchPattern = "%" + searchKey.toLowerCase() + "%";
            Predicate namePredicate = cb.like(cb.lower(root.get("name")), searchPattern);
            predicates.add(namePredicate);
        }

        if (role != null) {
            // Roles are stored in the database as a set of entity Role
            Role roleEntity = roleRepository.findByName(role).orElseThrow(() -> new BadRequestException("Customer Role not set"));
            Predicate rolePredicate = cb.isMember(roleEntity, root.get("roles"));
            predicates.add(rolePredicate);
        }

        // Apply predicates to queries
        if (!predicates.isEmpty()) {
            Predicate combinedPredicate = cb.and(predicates.toArray(new Predicate[0]));
            cr.where(combinedPredicate);
            countQuery.where(combinedPredicate);
        }

        // Pagination
        TypedQuery<Customer> query = em.createQuery(cr);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Customer> resultList = query.getResultList();
        Long count = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), count);
    }

    @Override
    public Customer getById(UUID id) {
        return this.userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "id", id.toString()));
    }

    @Override
    public Customer create(Customer user) {
        try {
            Optional<Customer> userOptional = this.userRepository.findByEmail(user.getEmail());
            if (userOptional.isPresent())
                throw new BadRequestException(String.format("Customer with email '%s' already exists", user.getEmail()));
            return this.userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            String errorMessage = Utility.getConstraintViolationMessage(ex, user);
            throw new BadRequestException(errorMessage, ex);
        }
    }

    @Override
    public Customer save(Customer user) {
        try {
            return this.userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            String errorMessage = Utility.getConstraintViolationMessage(ex, user);
            throw new BadRequestException(errorMessage, ex);
        }
    }

    @Override
    public Customer update(UUID id, UpdateCustomerDTO dto) {
        Customer entity = this.userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "id", id.toString()));

        Optional<Customer> userOptional = this.userRepository.findByEmail(dto.getEmail());
        if (userOptional.isPresent() && (userOptional.get().getId() != entity.getId()))
            throw new BadRequestException(String.format("Customer with email '%s' already exists", entity.getEmail()));

        entity.setEmail(dto.getEmail());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setTelephone(dto.getTelephone());

        return this.userRepository.save(entity);
    }

    @Override
    public boolean delete(UUID id) {
        this.userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("User", "id", id));

        this.userRepository.deleteById(id);
        return true;
    }
    @Override
    public Customer getLoggedInCustomer() {
        String email;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        return userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", email));
    }

    @Override
    public Customer getByEmail(String email) {
        return this.userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "id", email));
    }

    @Override
    public Customer changeProfileImage(UUID id, File file) {
        Customer entity = this.userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Document", "id", id.toString()));
        File existingFile = entity.getProfileImage();
        if (existingFile != null) {
            this.fileStorageService.removeFileOnDisk(existingFile.getPath());
        }
        entity.setProfileImage(file);
        return this.userRepository.save(entity);

    }

    @Override
    public Customer removeProfileImage(UUID id) {
        Customer user = this.userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "id", id.toString()));
        File file = user.getProfileImage();
        if (file != null) {
            this.fileService.delete(file.getId());
        }
        user.setProfileImage(null);
        return this.userRepository.save(user);
    }

    @Override
    public Optional<Customer> findByActivationCode(String activationCode) {
        return this.userRepository.findByActivationCode(activationCode);
    }

    @Override
    public Optional<Customer> findByAccountCode(String accountCode) {
        return this.userRepository.findByAccount(accountCode);
    }
}
