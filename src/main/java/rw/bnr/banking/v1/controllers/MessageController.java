package rw.bnr.banking.v1.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.bnr.banking.v1.payload.response.ApiResponse;
import rw.bnr.banking.v1.services.IMessageService;
import rw.bnr.banking.v1.utils.Constants;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final IMessageService messageService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getMessageById(
            @PathVariable("id") UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success("Message fetched successfully", this.messageService.getMessageById(id)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllMessages(
            @RequestParam(value = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = Constants.DEFAULT_PAGE_SIZE) int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);
        return ResponseEntity.ok(ApiResponse.success("Messages fetched successfully", this.messageService.findAllMessages(pageable)));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse> getAllMessages(
            @RequestParam(value = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = Constants.DEFAULT_PAGE_SIZE) int limit,
            @PathVariable("customerId") UUID customerId
    ) {
        Pageable pageable = PageRequest.of(page, limit);
        return ResponseEntity.ok(ApiResponse.success("Messages fetched successfully", this.messageService.findAllMessagesByCustomer(pageable, customerId)));
    }

}
