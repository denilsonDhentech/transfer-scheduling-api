package org.dhentech.presentation;

import org.dhentech.application.TransferService;
import org.dhentech.application.dto.FeeSimulationResponseDto;
import org.dhentech.application.dto.PagedTransferResponseDto;
import org.dhentech.application.dto.TransferRequestDto;
import org.dhentech.application.dto.TransferResponseDto;
import org.dhentech.application.dto.TransferUpdateRequestDto;
import org.dhentech.domain.TransferStatus;
import org.dhentech.infrastructure.export.TransferCsvExporter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final TransferService service;
    private final TransferCsvExporter csvExporter;

    public TransferController(TransferService service, TransferCsvExporter csvExporter) {
        this.service = service;
        this.csvExporter = csvExporter;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponseDto schedule(@Valid @RequestBody TransferRequestDto request) {
        return service.schedule(request);
    }

    @PostMapping("/simulate")
    public FeeSimulationResponseDto simulate(@Valid @RequestBody TransferRequestDto request) {
        return service.simulate(request);
    }

    @GetMapping("/{id}")
    public TransferResponseDto findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PatchMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) {
        service.cancel(id);
    }

    @GetMapping
    public PagedTransferResponseDto findAll(
            @RequestParam(required = false) TransferStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String sourceAccount,
            @RequestParam(required = false) String destinationAccount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.findPaged(status, from, to, sourceAccount, destinationAccount, page, size);
    }

    @PatchMapping("/{id}")
    public TransferResponseDto update(@PathVariable Long id,
                                      @Valid @RequestBody TransferUpdateRequestDto request) {
        return service.update(id, request);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"agendamentos.csv\"");
        return ResponseEntity.ok().headers(headers).body(csvExporter.toCsv(service.findAll()));
    }
}
