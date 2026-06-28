package org.dhentech.application;

import org.dhentech.application.dto.FeeSimulationResponseDto;
import org.dhentech.application.dto.PagedTransferResponseDto;
import org.dhentech.application.dto.TransferRequestDto;
import org.dhentech.application.dto.TransferResponseDto;
import org.dhentech.application.dto.TransferUpdateRequestDto;
import org.dhentech.domain.FeeCalculator;
import org.dhentech.domain.TransferStatus;
import org.dhentech.domain.exception.TransferCancellationException;
import org.dhentech.domain.exception.TransferEditException;
import org.dhentech.domain.exception.TransferNotFoundException;
import org.dhentech.infrastructure.entity.TransferEntity;
import org.dhentech.infrastructure.repository.TransferRepository;
import org.dhentech.infrastructure.repository.TransferSpecification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransferService {

    private final TransferRepository repository;
    private final FeeCalculator feeCalculator;

    public TransferService(TransferRepository repository) {
        this.repository = repository;
        this.feeCalculator = new FeeCalculator();
    }

    public TransferResponseDto schedule(TransferRequestDto request) {
        LocalDate schedulingDate = LocalDate.now();
        BigDecimal fee = feeCalculator.calculate(request.getAmount(), schedulingDate, request.getTransferDate());

        TransferEntity entity = new TransferEntity(
                request.getSourceAccount(),
                request.getDestinationAccount(),
                request.getAmount(),
                fee,
                request.getTransferDate(),
                schedulingDate
        );

        return TransferResponseDto.from(repository.save(entity));
    }

    public TransferResponseDto findById(Long id) {
        return repository.findById(id)
                .map(TransferResponseDto::from)
                .orElseThrow(() -> new TransferNotFoundException(id));
    }

    public FeeSimulationResponseDto simulate(TransferRequestDto request) {
        LocalDate schedulingDate = LocalDate.now();
        BigDecimal fee = feeCalculator.calculate(request.getAmount(), schedulingDate, request.getTransferDate());
        long days = ChronoUnit.DAYS.between(schedulingDate, request.getTransferDate());
        return new FeeSimulationResponseDto(fee, days);
    }

    public void cancel(Long id) {
        TransferEntity entity = repository.findById(id)
                .orElseThrow(() -> new TransferNotFoundException(id));

        if (entity.getStatus() == TransferStatus.CANCELLED) {
            throw new TransferCancellationException("Transfer is already cancelled.");
        }

        if (!entity.getTransferDate().isAfter(LocalDate.now())) {
            throw new TransferCancellationException("Transfer cannot be cancelled after execution date.");
        }

        entity.cancel();
        repository.save(entity);
    }

    public PagedTransferResponseDto findPaged(TransferStatus status, LocalDate from, LocalDate to,
                                              String sourceAccount, String destinationAccount,
                                              int page, int size) {
        Specification<TransferEntity> spec = TransferSpecification.withFilters(status, from, to, sourceAccount, destinationAccount);
        return PagedTransferResponseDto.from(repository.findAll(spec, PageRequest.of(page, size)));
    }

    public TransferResponseDto update(Long id, TransferUpdateRequestDto request) {
        TransferEntity entity = repository.findById(id)
                .orElseThrow(() -> new TransferNotFoundException(id));

        if (entity.getStatus() == TransferStatus.CANCELLED) {
            throw new TransferEditException("Only PENDING transfers can be edited.");
        }

        if (!entity.getTransferDate().isAfter(LocalDate.now())) {
            throw new TransferEditException("Transfer cannot be edited after execution date.");
        }

        BigDecimal newAmount = request.getAmount() != null ? request.getAmount() : entity.getAmount();
        LocalDate newTransferDate = request.getTransferDate() != null ? request.getTransferDate() : entity.getTransferDate();
        BigDecimal newFee = feeCalculator.calculate(newAmount, LocalDate.now(), newTransferDate);

        entity.update(newAmount, newFee, newTransferDate);
        return TransferResponseDto.from(repository.save(entity));
    }

    public List<TransferResponseDto> findAll() {
        return repository.findAll().stream()
                .map(TransferResponseDto::from)
                .collect(Collectors.toList());
    }

}
