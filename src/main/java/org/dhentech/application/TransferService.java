package org.dhentech.application;

import org.dhentech.application.dto.TransferRequestDto;
import org.dhentech.application.dto.TransferResponseDto;
import org.dhentech.domain.FeeCalculator;
import org.dhentech.infrastructure.entity.TransferEntity;
import org.dhentech.infrastructure.repository.TransferRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    public List<TransferResponseDto> findAll() {
        return repository.findAll().stream()
                .map(TransferResponseDto::from)
                .collect(Collectors.toList());
    }
}
