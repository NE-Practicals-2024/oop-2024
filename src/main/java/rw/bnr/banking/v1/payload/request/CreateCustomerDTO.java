package rw.bnr.banking.v1.payload.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import rw.bnr.banking.v1.validators.ValidPassword;

import java.time.LocalDate;

@Data
public class CreateCustomerDTO {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\+250\\d{9}$", message = "Your phone is not a valid tel we expect +2507******")
    private String mobile;

    // It should not be a future date
    @PastOrPresent(message = "Date of birth should be in the past")
    @NotNull(message = "Date of birth should not be empty")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @NotNull
    @DecimalMin(value = "0.1", message = "Balance should be greater than 0", inclusive = false)
    private Double balance;

    @ValidPassword
    private String password;
}
