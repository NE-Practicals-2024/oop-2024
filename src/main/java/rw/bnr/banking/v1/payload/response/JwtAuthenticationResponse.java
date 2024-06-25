package rw.bnr.banking.v1.payload.response;

import lombok.Getter;
import lombok.Setter;
import rw.bnr.banking.v1.models.Customer;

@Getter
@Setter
public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Customer customer;

    public JwtAuthenticationResponse(String accessToken, Customer customer) {
        this.accessToken = accessToken;
        this.customer = customer;
    }
}



