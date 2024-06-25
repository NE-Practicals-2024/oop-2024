package rw.bnr.banking.v1.serviceImpls;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import rw.bnr.banking.v1.exceptions.BadRequestException;
import rw.bnr.banking.v1.exceptions.ResourceNotFoundException;
import rw.bnr.banking.v1.models.Customer;
import rw.bnr.banking.v1.models.File;
import rw.bnr.banking.v1.payload.request.UpdateCustomerDTO;
import rw.bnr.banking.v1.repositories.ICustomerRepository;
import rw.bnr.banking.v1.repositories.IRoleRepository;
import rw.bnr.banking.v1.services.ICustomerService;
import rw.bnr.banking.v1.services.IFileService;
import rw.bnr.banking.v1.standalone.FileStorageService;
import rw.bnr.banking.v1.utils.Utility;

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
    public Page<Customer> getAll(Pageable pageable) {
        return this.userRepository.findAll(pageable);
    }

    @Override
    public Page<Customer> search(Pageable pageable, String searchKey) {
        return this.userRepository.search(pageable, searchKey);
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
        entity.setMobile(dto.getMobile());

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
