package org.dhentech.application;

import org.dhentech.application.dto.TransferRequestDto;
import org.dhentech.application.dto.TransferResponseDto;
import org.dhentech.application.dto.FeeSimulationResponseDto;
import org.dhentech.domain.TransferStatus;
import org.dhentech.domain.exception.NoApplicableFeeException;
import org.dhentech.domain.exception.TransferCancellationException;
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
    void shouldReturnPendingStatusForFutureTransfer() {
        TransferEntity entity = buildEntity(1L, new BigDecimal("12.00"));
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        TransferResponseDto response = service.findById(1L);

        assertEquals(TransferStatus.PENDING, response.getStatus());
    }

    @Test
    void shouldReturnExecutedStatusWhenTransferDateIsToday() {
        TransferEntity entity = buildEntityWithDate(1L, new BigDecimal("12.00"), LocalDate.now());
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        TransferResponseDto response = service.findById(1L);

        assertEquals(TransferStatus.EXECUTED, response.getStatus());
    }

    @Test
    void shouldReturnCancelledStatusWhenTransferIsCancelled() {
        TransferEntity entity = buildEntity(1L, new BigDecimal("12.00"));
        entity.cancel();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        TransferResponseDto response = service.findById(1L);

        assertEquals(TransferStatus.CANCELLED, response.getStatus());
    }

    @Test
    void shouldThrowTransferNotFoundWhenIdDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TransferNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void shouldSimulateAndReturnFeeAndDaysWithoutPersisting() {
        setFieldSilently(request, "transferDate", LocalDate.now().plusDays(5));

        FeeSimulationResponseDto response = service.simulate(request);

        assertEquals(new BigDecimal("12.00"), response.getFee());
        assertEquals(5L, response.getDays());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldSimulateZeroDayTransferWithFixedFeeAndPercent() {
        setFieldSilently(request, "transferDate", LocalDate.now());

        FeeSimulationResponseDto response = service.simulate(request);

        assertEquals(new BigDecimal("28.00"), response.getFee());
        assertEquals(0L, response.getDays());
    }

    @Test
    void shouldThrowWhenSimulatingWithInvalidDate() {
        setFieldSilently(request, "transferDate", LocalDate.now().plusDays(51));

        assertThrows(NoApplicableFeeException.class, () -> service.simulate(request));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldCancelPendingTransferWithFutureDate() {
        TransferEntity entity = buildEntity(1L, new BigDecimal("12.00"));
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.cancel(1L);

        assertEquals(TransferStatus.CANCELLED, entity.getStatus());
        verify(repository, times(1)).save(entity);
    }

    @Test
    void shouldThrowWhenCancellingAlreadyCancelledTransfer() {
        TransferEntity entity = buildEntity(1L, new BigDecimal("12.00"));
        entity.cancel();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        assertThrows(TransferCancellationException.class, () -> service.cancel(1L));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCancellingTransferAfterExecutionDate() {
        TransferEntity entity = buildEntityWithDate(1L, new BigDecimal("12.00"), LocalDate.now());
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        assertThrows(TransferCancellationException.class, () -> service.cancel(1L));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowTransferNotFoundWhenCancellingNonExistentTransfer() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TransferNotFoundException.class, () -> service.cancel(99L));
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
        return buildEntityWithDate(id, fee, LocalDate.now().plusDays(5));
    }

    private TransferEntity buildEntityWithDate(Long id, BigDecimal fee, LocalDate transferDate) {
        TransferEntity entity = new TransferEntity(
                "1234567890", "0987654321",
                new BigDecimal("1000.00"), fee,
                transferDate, LocalDate.now()
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
