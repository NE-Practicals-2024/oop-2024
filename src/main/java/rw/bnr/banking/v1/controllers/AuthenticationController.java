package rw.bnr.banking.v1.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import rw.bnr.banking.v1.payload.request.InitiateAccountVerificationDTO;
import rw.bnr.banking.v1.payload.request.InitiatePasswordResetDTO;
import rw.bnr.banking.v1.payload.request.LoginDTO;
import rw.bnr.banking.v1.payload.request.ResetPasswordDTO;
import rw.bnr.banking.v1.payload.response.ApiResponse;
import rw.bnr.banking.v1.services.IAuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final IAuthService authService;

    @PostMapping(path = "/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", this.authService.login(dto.getEmail(), dto.getPassword())));
    }

    @PostMapping(path = "/initiate-reset-password")
    public ResponseEntity<ApiResponse> initiateResetPassword(@RequestBody @Valid InitiatePasswordResetDTO dto) {
        this.authService.initiateResetPassword(dto.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Please check your mail and activate account"));
    }


    @PostMapping(path = "/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody @Valid ResetPasswordDTO dto) {
      this.authService.resetPassword(dto.getEmail(),dto.getPasswordResetCode(), dto.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password successfully reset"));
    }

    @PutMapping("/initiate-account-verification")
    private ResponseEntity<ApiResponse> initiateAccountVerification(@RequestBody @Valid InitiateAccountVerificationDTO dto) {
        this.authService.initiateAccountVerification(dto.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Verification code sent to email, expiring in 6 hours"));
    }

    @PatchMapping("/verify-account/{verificationCode}")
    private ResponseEntity<ApiResponse> verifyAccount(
            @PathVariable("verificationCode") String verificationCode
    ) {
        this.authService.verifyAccount(verificationCode);
        return ResponseEntity.ok(ApiResponse.success("Account verified successfully, you can now login"));
    }
}