package rw.bnr.banking.v1.payload.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import rw.bnr.banking.v1.enums.ETransactionType;

@Data
public class CreateTransactionDTO {

    @NotNull
    @DecimalMin(value = "0.1", inclusive = false)
    private Double amount;

    @NotNull
    private ETransactionType transactionType;

}
