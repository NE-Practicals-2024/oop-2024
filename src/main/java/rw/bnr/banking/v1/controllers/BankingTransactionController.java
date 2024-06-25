package rw.bnr.banking.v1.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import rw.bnr.banking.v1.enums.ETransactionType;
import rw.bnr.banking.v1.models.BankingTransaction;
import rw.bnr.banking.v1.models.Customer;
import rw.bnr.banking.v1.payload.request.CreateTransactionDTO;
import rw.bnr.banking.v1.payload.response.ApiResponse;
import rw.bnr.banking.v1.services.IBankingTransactionService;
import rw.bnr.banking.v1.services.ICustomerService;
import rw.bnr.banking.v1.standalone.ExcelService;
import rw.bnr.banking.v1.utils.Constants;

import java.io.IOException;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/transactions")
@RequiredArgsConstructor
public class BankingTransactionController {

    private final IBankingTransactionService bankingTransactionService;
    private final ExcelService excelService;
    private final ICustomerService customerService;

    @PostMapping("/create")
    private ResponseEntity<ApiResponse> createTransaction(@RequestBody @Valid CreateTransactionDTO dto, @RequestParam(value = "receiverAccount", required = false) String receiverAccount) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().toString());
        return ResponseEntity.created(uri).body(ApiResponse.success("Transaction created successfully", this.bankingTransactionService.createTransaction(dto, receiverAccount)));
    }

    @GetMapping("/all")
    private ResponseEntity<ApiResponse> getAllTransactions(@RequestParam(value = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page, @RequestParam(value = "size", defaultValue = Constants.DEFAULT_PAGE_SIZE) int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        return ResponseEntity.ok(ApiResponse.success("Transactions fetched successfully", this.bankingTransactionService.getAllTransactions(pageable)));
    }

    @GetMapping("/{id}")
    private ResponseEntity<ApiResponse> getTransactionById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Transaction fetched successfully", this.bankingTransactionService.getTransactionById(id)));
    }

    @GetMapping("/type/{type}")
    private ResponseEntity<ApiResponse> getAllTransactionsByType(@RequestParam(value = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page, @RequestParam(value = "size", defaultValue = Constants.DEFAULT_PAGE_SIZE) int limit, @PathVariable("type") ETransactionType type) {
        Pageable pageable = PageRequest.of(page, limit);
        return ResponseEntity.ok(ApiResponse.success("Transactions fetched successfully", this.bankingTransactionService.getAllTransactionsByType(pageable, type)));
    }

    @GetMapping("/customer")
    private ResponseEntity<ApiResponse> getAllTransactionsByCustomer(@RequestParam(value = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page, @RequestParam(value = "size", defaultValue = Constants.DEFAULT_PAGE_SIZE) int limit, @RequestParam(value = "customerId") UUID customerId) {
        Pageable pageable = PageRequest.of(page, limit);
        return ResponseEntity.ok(ApiResponse.success("Transactions fetched successfully", this.bankingTransactionService.getAllTransactionsByCustomer(pageable, customerId)));
    }

    @GetMapping("/download/{customerId}")
    public ResponseEntity<byte[]> downloadExcel(
            @PathVariable UUID customerId
    ) throws IOException {
        List<String> fileHeaders = Arrays.asList("#", "Customer Names", "Transaction Type", "Amount", "Your Account", "Receiver Accout", "Transaction Date");
        List<BankingTransaction> transactions = this.bankingTransactionService.getAllTransactionsByCustomer(customerId);
        List<List<String>> data = new java.util.ArrayList<>(Collections.emptyList());
        for (int i = 0; i < transactions.size(); i++) {
            BankingTransaction transaction = transactions.get(i);
            data.add(Arrays.asList(
                    String.valueOf(i + 1),
                    transaction.getCustomer().getFirstName() + " " + transaction.getCustomer().getLastName(),
                    transaction.getTransactionType().name(),
                    String.valueOf(transaction.getAmount()),
                    transaction.getCustomer().getAccount(),
                    transaction.getReceiver() != null ? transaction.getReceiver().getAccount() : "N/A",
                    transaction.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    )));
        }
        byte[] excelContent = excelService.generateExcelTransactions(fileHeaders, data);
        Customer customer = this.customerService.getById(customerId);
        String filename = customer.getFirstName() + "_" + customer.getLastName() + "_transactions.xlsx";
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelContent);
    }


}
