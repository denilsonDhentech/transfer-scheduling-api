package org.dhentech.application;

import org.dhentech.application.dto.TransferRequestDto;
import org.dhentech.application.dto.TransferResponseDto;
import org.dhentech.domain.exception.NoApplicableFeeException;
import org.dhentech.domain.exception.TransferNotFoundException;
import org.dhentech.infrastructure.entity.TransferEntity;
import org.dhentech.infrastructure.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository repository;

    @InjectMocks
    private TransferService service;

    private TransferRequestDto request;

    @BeforeEach
    void setUp() throws Exception {
        request = new TransferRequestDto();
        setField(request, "sourceAccount", "1234567890");
        setField(request, "destinationAccount", "0987654321");
        setField(request, "amount", new BigDecimal("1000.00"));
        setField(request, "transferDate", LocalDate.now().plusDays(5));
    }

    @Test
    void shouldCalculateFeeAndPersistTransfer() {
        TransferEntity saved = buildEntity(1L, new BigDecimal("12.00"));
        when(repository.save(any())).thenReturn(saved);

        TransferResponseDto response = service.schedule(request);

        assertEquals(1L, response.getId());
        assertEquals(new BigDecimal("12.00"), response.getFee());
        assertEquals("1234567890", response.getSourceAccount());
        verify(repository, times(1)).save(any(TransferEntity.class));
    }

    @Test
    void shouldThrowWhenTransferDateIsInThePast() {
        setFieldSilently(request, "transferDate", LocalDate.now().minusDays(1));

        assertThrows(NoApplicableFeeException.class, () -> service.schedule(request));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowWhenTransferDateExceedsFiftyDays() {
        setFieldSilently(request, "transferDate", LocalDate.now().plusDays(51));

        assertThrows(NoApplicableFeeException.class, () -> service.schedule(request));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldReturnTransferWhenFoundById() {
        TransferEntity entity = buildEntity(1L, new BigDecimal("12.00"));
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        TransferResponseDto response = service.findById(1L);

        assertEquals(1L, response.getId());
        assertEquals(new BigDecimal("12.00"), response.getFee());
    }

    @Test
    void shouldThrowTransferNotFoundWhenIdDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TransferNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void shouldReturnAllScheduledTransfers() {
        when(repository.findAll()).thenReturn(List.of(
                buildEntity(1L, new BigDecimal("12.00")),
                buildEntity(2L, new BigDecimal("82.00"))
        ));

        List<TransferResponseDto> result = service.findAll();

        assertEquals(2, result.size());
    }

    private TransferEntity buildEntity(Long id, BigDecimal fee) {
        TransferEntity entity = new TransferEntity(
                "1234567890", "0987654321",
                new BigDecimal("1000.00"), fee,
                LocalDate.now().plusDays(5), LocalDate.now()
        );
        setFieldSilently(entity, "id", id);
        return entity;
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void setFieldSilently(Object target, String name, Object value) {
        try {
            setField(target, name, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
