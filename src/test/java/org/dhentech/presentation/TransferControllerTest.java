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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
                .andExpect(jsonPath("$.schedulingDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
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
    void shouldReturnTransferWhenGetById() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().plusDays(5));
        String body = objectMapper.writeValueAsString(request);

        String response = mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/transfers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.sourceAccount").value("1234567890"))
                .andExpect(jsonPath("$.fee").value(12.0));
    }

    @Test
    void shouldReturnExecutedStatusWhenTransferDateIsToday() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now());

        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("EXECUTED"));
    }

    @Test
    void shouldReturn404WhenTransferNotFound() throws Exception {
        mockMvc.perform(get("/transfers/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldExportCsvWithCorrectHeadersAndContent() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().plusDays(5));
        mockMvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        String csv = mockMvc.perform(get("/transfers/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")))
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(header().string("Content-Disposition", containsString("agendamentos.csv")))
                .andReturn().getResponse().getContentAsString();

        assertTrue(csv.startsWith("id,sourceAccount,destinationAccount,amount,fee,transferDate,schedulingDate,status\n"));
        assertTrue(csv.contains("1234567890"));
        assertTrue(csv.contains("PENDING"));
    }

    @Test
    void shouldExportCsvWithOnlyHeaderWhenNoTransfers() throws Exception {
        String csv = mockMvc.perform(get("/transfers/export"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(csv.startsWith("id,sourceAccount,destinationAccount,amount,fee,transferDate,schedulingDate,status\n"));
        assertFalse(csv.contains("\n1"));
    }

    @Test
    void shouldSimulateTransferAndReturnFeeAndDays() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/transfers/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fee").value(12.0))
                .andExpect(jsonPath("$.days").value(5));
    }

    @Test
    void shouldReturn400WhenSimulatingWithDateInThePast() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().minusDays(1));

        mockMvc.perform(post("/transfers/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturn400WhenSimulatingWithDateExceedingFiftyDays() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().plusDays(51));

        mockMvc.perform(post("/transfers/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldCancelTransferAndReturn204() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().plusDays(5));
        String response = mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/transfers/{id}/cancel", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/transfers/{id}", id))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldReturn422WhenCancellingAlreadyCancelledTransfer() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().plusDays(5));
        String response = mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();
        mockMvc.perform(patch("/transfers/{id}/cancel", id));

        mockMvc.perform(patch("/transfers/{id}/cancel", id))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturn422WhenCancellingTransferAfterExecutionDate() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now());
        String response = mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/transfers/{id}/cancel", id))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturn404WhenCancellingNonExistentTransfer() throws Exception {
        mockMvc.perform(patch("/transfers/{id}/cancel", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturnEmptyPageWhenNoTransfersScheduled() throws Exception {
        mockMvc.perform(get("/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldReturnPagedTransfers() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().plusDays(5));
        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/transfers").contentType(MediaType.APPLICATION_JSON).content(body));
        mockMvc.perform(post("/transfers").contentType(MediaType.APPLICATION_JSON).content(body));

        mockMvc.perform(get("/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldFilterByStatus() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().plusDays(5));
        mockMvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/transfers").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        mockMvc.perform(get("/transfers").param("status", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void shouldFilterBySourceAccount() throws Exception {
        TransferRequestDto r1 = buildRequest("1111111111", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().plusDays(5));
        TransferRequestDto r2 = buildRequest("2222222222", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/transfers").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(r1)));
        mockMvc.perform(post("/transfers").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(r2)));

        mockMvc.perform(get("/transfers").param("sourceAccount", "1111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].sourceAccount").value("1111111111"));
    }

    @Test
    void shouldReturnPageWithSizeOne() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().plusDays(5));
        String body = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/transfers").contentType(MediaType.APPLICATION_JSON).content(body));
        mockMvc.perform(post("/transfers").contentType(MediaType.APPLICATION_JSON).content(body));

        mockMvc.perform(get("/transfers").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void shouldUpdateTransferAndReturn200() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().plusDays(5));
        String created = mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(created).get("id").asLong();

        String updateBody = "{\"transferDate\": \"" + LocalDate.now().plusDays(15) + "\"}";
        mockMvc.perform(patch("/transfers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fee").value(82.0))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldReturn422WhenEditingCancelledTransfer() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().plusDays(5));
        String created = mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(created).get("id").asLong();
        mockMvc.perform(patch("/transfers/{id}/cancel", id));

        mockMvc.perform(patch("/transfers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 2000.00}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturn400WhenEditingWithInvalidDate() throws Exception {
        TransferRequestDto request = buildRequest("1234567890", "0987654321",
                new BigDecimal("1000.00"), LocalDate.now().plusDays(5));
        String created = mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(patch("/transfers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transferDate\": \"" + LocalDate.now().plusDays(51) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturn404WhenEditingNonExistentTransfer() throws Exception {
        mockMvc.perform(patch("/transfers/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 2000.00}"))
                .andExpect(status().isNotFound());
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
