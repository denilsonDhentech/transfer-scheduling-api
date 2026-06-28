package org.dhentech.infrastructure.entity;

import org.dhentech.domain.TransferStatus;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transfers")
public class TransferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_account", nullable = false, length = 10)
    private String sourceAccount;

    @Column(name = "destination_account", nullable = false, length = 10)
    private String destinationAccount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fee;

    @Column(name = "transfer_date", nullable = false)
    private LocalDate transferDate;

    @Column(name = "scheduling_date", nullable = false)
    private LocalDate schedulingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransferStatus status;

    public TransferEntity() {
    }

    public TransferEntity(String sourceAccount, String destinationAccount,
                          BigDecimal amount, BigDecimal fee,
                          LocalDate transferDate, LocalDate schedulingDate) {
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.fee = fee;
        this.transferDate = transferDate;
        this.schedulingDate = schedulingDate;
        this.status = TransferStatus.PENDING;
    }

    public void cancel() {
        this.status = TransferStatus.CANCELLED;
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
