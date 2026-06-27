package org.dhentech.application.dto;

import org.dhentech.infrastructure.entity.TransferEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransferResponseDto {

    private Long id;
    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal amount;
    private BigDecimal fee;
    private LocalDate transferDate;
    private LocalDate schedulingDate;

    private TransferResponseDto() {
    }

    public static TransferResponseDto from(TransferEntity entity) {
        TransferResponseDto dto = new TransferResponseDto();
        dto.id = entity.getId();
        dto.sourceAccount = entity.getSourceAccount();
        dto.destinationAccount = entity.getDestinationAccount();
        dto.amount = entity.getAmount();
        dto.fee = entity.getFee();
        dto.transferDate = entity.getTransferDate();
        dto.schedulingDate = entity.getSchedulingDate();
        return dto;
    }

    public Long getId() { return id; }

    public String getSourceAccount() { return sourceAccount; }

    public String getDestinationAccount() { return destinationAccount; }

    public BigDecimal getAmount() { return amount; }

    public BigDecimal getFee() { return fee; }

    public LocalDate getTransferDate() { return transferDate; }

    public LocalDate getSchedulingDate() { return schedulingDate; }
}
