package org.dhentech.application.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransferRequestDto {

    @NotNull
    @Pattern(regexp = "\\d{10}", message = "Account must be exactly 10 digits")
    private String sourceAccount;

    @NotNull
    @Pattern(regexp = "\\d{10}", message = "Account must be exactly 10 digits")
    private String destinationAccount;

    @NotNull
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull
    private LocalDate transferDate;

    public String getSourceAccount() { return sourceAccount; }

    public String getDestinationAccount() { return destinationAccount; }

    public BigDecimal getAmount() { return amount; }

    public LocalDate getTransferDate() { return transferDate; }
}
