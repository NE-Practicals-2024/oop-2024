package rw.bnr.banking.v1.payload.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import rw.bnr.banking.v1.enums.ETransactionType;

import java.util.UUID;

@Data
public class CreateTransactionDTO {

    @NotNull
    @DecimalMin(value = "0.1", inclusive = false)
    private Double amount;

    @NotNull
    private ETransactionType transactionType;

}
