package org.dhentech.domain;

import org.dhentech.domain.exception.NoApplicableFeeException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FeeCalculator {

    private static final BigDecimal FIXED_SAME_DAY = new BigDecimal("3.00");
    private static final BigDecimal PERCENT_SAME_DAY = new BigDecimal("0.025");
    private static final BigDecimal FIXED_1_TO_10 = new BigDecimal("12.00");
    private static final BigDecimal PERCENT_11_TO_20 = new BigDecimal("0.082");
    private static final BigDecimal PERCENT_21_TO_30 = new BigDecimal("0.069");
    private static final BigDecimal PERCENT_31_TO_40 = new BigDecimal("0.047");
    private static final BigDecimal PERCENT_41_TO_50 = new BigDecimal("0.017");

    public BigDecimal calculate(BigDecimal amount, LocalDate schedulingDate, LocalDate transferDate) {
        long days = ChronoUnit.DAYS.between(schedulingDate, transferDate);

        if (days < 0) {
            throw new NoApplicableFeeException(
                    "Transfer date cannot be in the past. No applicable fee.");
        }
        if (days == 0) {
            return FIXED_SAME_DAY.add(amount.multiply(PERCENT_SAME_DAY)).setScale(2, RoundingMode.HALF_UP);
        }
        if (days <= 10) {
            return FIXED_1_TO_10.setScale(2, RoundingMode.HALF_UP);
        }
        if (days <= 20) {
            return amount.multiply(PERCENT_11_TO_20).setScale(2, RoundingMode.HALF_UP);
        }
        if (days <= 30) {
            return amount.multiply(PERCENT_21_TO_30).setScale(2, RoundingMode.HALF_UP);
        }
        if (days <= 40) {
            return amount.multiply(PERCENT_31_TO_40).setScale(2, RoundingMode.HALF_UP);
        }
        if (days <= 50) {
            return amount.multiply(PERCENT_41_TO_50).setScale(2, RoundingMode.HALF_UP);
        }

        throw new NoApplicableFeeException(
                "No applicable fee for transfers more than 50 days ahead.");
    }
}
