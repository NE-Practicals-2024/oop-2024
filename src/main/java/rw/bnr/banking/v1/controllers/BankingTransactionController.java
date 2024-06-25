package rw.bnr.banking.v1.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import rw.bnr.banking.v1.enums.ERole;
import rw.bnr.banking.v1.enums.ETransactionType;
import rw.bnr.banking.v1.payload.request.CreateTransactionDTO;
import rw.bnr.banking.v1.payload.response.ApiResponse;
import rw.bnr.banking.v1.services.IBankingTransactionService;
import rw.bnr.banking.v1.utils.Constants;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/transactions")
@RequiredArgsConstructor
public class BankingTransactionController {

    private final IBankingTransactionService bankingTransactionService;

    @PostMapping("/create")
    private ResponseEntity<ApiResponse> createTransaction(
            @RequestBody @Valid CreateTransactionDTO dto
    ) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().toString());
        return ResponseEntity.created(uri).body(ApiResponse.success("Transaction created successfully", this.bankingTransactionService.createTransaction(dto)));
    }

    @GetMapping("/all")
    private ResponseEntity<ApiResponse> getAllTransactions(
            @RequestParam(value = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = Constants.DEFAULT_PAGE_SIZE) int limit,
            @RequestParam(value = "type", required = false) ETransactionType type,
            @RequestParam(value = "customerId", required = false) UUID customerId
    ) {
        Pageable pageable = PageRequest.of(page, limit);
        return ResponseEntity.ok(ApiResponse.success("Transactions fetched successfully", this.bankingTransactionService.getAllTransactions(pageable, type, customerId)));
    }

    @GetMapping("/{id}")
    private ResponseEntity<ApiResponse> getTransactionById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success("Transaction fetched successfully", this.bankingTransactionService.getTransactionById(id)));
    }

}