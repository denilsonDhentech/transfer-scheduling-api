package org.dhentech.infrastructure.export;

import org.dhentech.application.dto.TransferResponseDto;
import org.dhentech.infrastructure.entity.TransferEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransferCsvExporterTest {

    private final TransferCsvExporter exporter = new TransferCsvExporter();

    @Test
    void shouldGenerateCsvWithHeaderAndDataRow() {
        TransferResponseDto dto = TransferResponseDto.from(buildEntity(1L, new BigDecimal("12.00")));

        String csv = new String(exporter.toCsv(List.of(dto)), StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("id,sourceAccount,destinationAccount,amount,fee,transferDate,schedulingDate,status\n"));
        assertTrue(csv.contains("1234567890"));
        assertTrue(csv.contains("12.00"));
        assertTrue(csv.contains("PENDING"));
    }

    @Test
    void shouldGenerateCsvWithOnlyHeaderWhenListIsEmpty() {
        String csv = new String(exporter.toCsv(List.of()), StandardCharsets.UTF_8);

        assertEquals("id,sourceAccount,destinationAccount,amount,fee,transferDate,schedulingDate,status\n", csv);
    }

    @Test
    void shouldGenerateOneLinePerTransfer() {
        TransferResponseDto dto = TransferResponseDto.from(buildEntity(1L, new BigDecimal("12.00")));

        String csv = new String(exporter.toCsv(List.of(dto, dto)), StandardCharsets.UTF_8);

        long dataLines = csv.lines().filter(l -> !l.startsWith("id")).count();
        assertEquals(2, dataLines);
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

    private void setFieldSilently(Object target, String name, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
