package rw.bnr.banking.v1.controllers;

import rw.bnr.banking.v1.enums.ECustomerStatus;
import rw.bnr.banking.v1.enums.ERole;
import rw.bnr.banking.v1.exceptions.BadRequestException;
import rw.bnr.banking.v1.models.File;
import rw.bnr.banking.v1.models.Role;
import rw.bnr.banking.v1.models.Customer;
import rw.bnr.banking.v1.payload.request.CreateCustomerDTO;
import rw.bnr.banking.v1.payload.request.UpdateCustomerDTO;
import rw.bnr.banking.v1.payload.response.ApiResponse;
import rw.bnr.banking.v1.repositories.IRoleRepository;
import rw.bnr.banking.v1.services.ICustomerService;
import rw.bnr.banking.v1.services.IFileService;
import rw.bnr.banking.v1.utils.Constants;
import rw.bnr.banking.v1.utils.Utility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final ICustomerService customerService;
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final IRoleRepository roleRepository;
    private final IFileService fileService;

    @Value("${uploads.directory.customer_profiles}")
    private String customerProfilesDirectory;

    @GetMapping(path = "/current-customer")
    public ResponseEntity<ApiResponse> currentlyLoggedInCustomer() {
        return ResponseEntity.ok(ApiResponse.success("Currently logged in customer fetched", customerService.getLoggedInCustomer()));
    }

    @PutMapping(path = "/update")
    public ResponseEntity<ApiResponse> update(@RequestBody UpdateCustomerDTO dto) {
        Customer updated = this.customerService.update(this.customerService.getLoggedInCustomer().getId(), dto);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", updated));
    }

    @GetMapping(path = "/all")
    public ResponseEntity<ApiResponse> getAllUsers(
            @RequestParam(value = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = Constants.DEFAULT_PAGE_SIZE) int limit,
            @RequestParam(value = "role", required = false) ERole role,
            @RequestParam(value = "searchKey", required = false) String searchKey,
            @RequestParam(value = "status", required = false) ECustomerStatus status
    ) {
        Pageable pageable = Pageable.ofSize(limit).withPage(page);
        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully", this.customerService.getAll(pageable, role, searchKey, status)));
    }


    @GetMapping(path = "/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable(value = "id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Customer fetched successfully", this.customerService.getById(id)));
    }

    @PostMapping(path = "/register")
    public ResponseEntity<ApiResponse> register(@RequestBody @Valid CreateCustomerDTO dto) {

        Customer customer = new Customer();

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        Role role = roleRepository.findByName(ERole.CUSTOMER).orElseThrow(
                () -> new BadRequestException("Customer Role not set"));
        String accountCode;
        do {
            accountCode = Utility.generateCode();
        } while (this.customerService.findByAccountCode(accountCode).isPresent());
        customer.setEmail(dto.getEmail());
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setTelephone(dto.getMobile());
        customer.setPassword(encodedPassword);
        customer.setBalance(dto.getBalance());
        customer.setAccount(accountCode);
        customer.setRoles(Collections.singleton(role));

        Customer entity = this.customerService.create(customer);
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().toString());
        return ResponseEntity.created(uri).body(ApiResponse.success("Customer created successfully", entity));
    }

    @PutMapping(path = "/upload-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadProfileImage(
            @RequestParam("file") MultipartFile document
    ) {
        if (!Utility.isImageFile(document)) {
            throw new BadRequestException("Only image files are allowed");
        }
        Customer customer = this.customerService.getLoggedInCustomer();
        File file = this.fileService.create(document, customerProfilesDirectory);

        Customer updated = this.customerService.changeProfileImage(customer.getId(), file);

        return ResponseEntity.ok(ApiResponse.success("File saved successfully", updated));

    }

    @PatchMapping(path = "/remove-profile")
    public ResponseEntity<ApiResponse> removeProfileImage() {
        Customer customer = this.customerService.getLoggedInCustomer();
        Customer updated = this.customerService.removeProfileImage(customer.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile image removed successfully", updated));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteMyAccount() {
        Customer customer = this.customerService.getLoggedInCustomer();
        this.customerService.delete(customer.getId());
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteByAdmin(
            @PathVariable(value = "id") UUID id
    ) {
        this.customerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully"));
    }

}