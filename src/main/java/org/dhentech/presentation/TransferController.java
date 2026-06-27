package org.dhentech.presentation;

import org.dhentech.application.TransferService;
import org.dhentech.application.dto.TransferRequestDto;
import org.dhentech.application.dto.TransferResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final TransferService service;

    public TransferController(TransferService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponseDto schedule(@Valid @RequestBody TransferRequestDto request) {
        return service.schedule(request);
    }

    @GetMapping
    public List<TransferResponseDto> findAll() {
        return service.findAll();
    }
}
