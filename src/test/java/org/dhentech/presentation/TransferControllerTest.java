package org.dhentech.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.dhentech.application.dto.TransferRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldScheduleTransferAndReturn201() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.fee").value(12.0))
                .andExpect(jsonPath("$.sourceAccount").value("1234567890"))
                .andExpect(jsonPath("$.schedulingDate").value(LocalDate.now().toString()));
    }

    @Test
    void shouldReturn400WhenTransferDateExceedsFiftyDays() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().plusDays(51));

        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturn400WhenTransferDateIsInThePast() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().minusDays(1));

        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturn400WhenAccountFormatIsInvalid() throws Exception {
        TransferRequestDto request = buildRequest("123", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sourceAccount").exists());
    }

    @Test
    void shouldReturn400WhenAmountIsNegative() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("-100.00"), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.amount").exists());
    }

    @Test
    void shouldReturnEmptyListWhenNoTransfersScheduled() throws Exception {
        mockMvc.perform(get("/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturnAllScheduledTransfers() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().plusDays(5));
        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/transfers").contentType(MediaType.APPLICATION_JSON).content(body));
        mockMvc.perform(post("/transfers").contentType(MediaType.APPLICATION_JSON).content(body));

        mockMvc.perform(get("/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    private TransferRequestDto buildRequest(String source, String destination,
                                            BigDecimal amount, LocalDate transferDate) throws Exception {
        TransferRequestDto dto = new TransferRequestDto();
        setField(dto, "sourceAccount", source);
        setField(dto, "destinationAccount", destination);
        setField(dto, "amount", amount);
        setField(dto, "transferDate", transferDate);
        return dto;
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
