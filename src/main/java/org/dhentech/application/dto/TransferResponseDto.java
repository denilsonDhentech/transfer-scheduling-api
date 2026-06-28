package org.dhentech.application.dto;

import org.dhentech.domain.TransferStatus;
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
    private TransferStatus status;

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
        dto.status = resolveStatus(entity);
        return dto;
    }

    private static TransferStatus resolveStatus(TransferEntity entity) {
        if (entity.getStatus() == TransferStatus.CANCELLED) {
            return TransferStatus.CANCELLED;
        }
        if (!entity.getTransferDate().isAfter(LocalDate.now())) {
            return TransferStatus.EXECUTED;
        }
        return TransferStatus.PENDING;
    }

    public Long getId() { return id; }

    public String getSourceAccount() { return sourceAccount; }

    public String getDestinationAccount() { return destinationAccount; }

    public BigDecimal getAmount() { return amount; }

    public BigDecimal getFee() { return fee; }

    public LocalDate getTransferDate() { return transferDate; }

    public LocalDate getSchedulingDate() { return schedulingDate; }

    public TransferStatus getStatus() { return status; }
}
