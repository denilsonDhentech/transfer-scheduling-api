package org.dhentech.application.dto;

import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransferUpdateRequestDto {

    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private LocalDate transferDate;

    public BigDecimal getAmount() { return amount; }

    public LocalDate getTransferDate() { return transferDate; }
}
