package org.dhentech.application;

import org.dhentech.application.dto.FeeSimulationResponseDto;
import org.dhentech.application.dto.TransferRequestDto;
import org.dhentech.application.dto.TransferResponseDto;
import org.dhentech.domain.FeeCalculator;
import org.dhentech.domain.TransferStatus;
import org.dhentech.domain.exception.TransferCancellationException;
import org.dhentech.domain.exception.TransferNotFoundException;
import org.dhentech.infrastructure.entity.TransferEntity;
import org.dhentech.infrastructure.repository.TransferRepository;
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

    public List<TransferResponseDto> findAll() {
        return repository.findAll().stream()
                .map(TransferResponseDto::from)
                .collect(Collectors.toList());
    }
}
