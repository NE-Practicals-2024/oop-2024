package rw.bnr.banking.v1.payload.request;


import jakarta.validation.constraints.NotNull;
import rw.bnr.banking.v1.validators.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

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
    @Pattern(regexp = "[0-9]{9,12}", message = "Your phone is not a valid tel we expect 2507***, or 07*** or 7***")
    private String mobile;

    @NotNull(message = "Date of birth should not be empty")
    private LocalDate dob;

    @NotNull
    private Double balance;

    @ValidPassword
    private String password;
}
