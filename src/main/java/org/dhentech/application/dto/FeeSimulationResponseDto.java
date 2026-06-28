package org.dhentech.application.dto;

import java.math.BigDecimal;

public class FeeSimulationResponseDto {

    private BigDecimal fee;
    private long days;

    public FeeSimulationResponseDto(BigDecimal fee, long days) {
        this.fee = fee;
        this.days = days;
    }

    public BigDecimal getFee() { return fee; }

    public long getDays() { return days; }
}
