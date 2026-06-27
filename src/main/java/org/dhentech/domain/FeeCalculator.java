package org.dhentech.domain;

import org.dhentech.domain.exception.NoApplicableFeeException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

public class FeeCalculator {

    private static final List<FeeRule> RULES = Arrays.asList(
            new FeeRule(0,  0,  new BigDecimal("3.00"),  new BigDecimal("0.025")),
            new FeeRule(1,  10, new BigDecimal("12.00"), BigDecimal.ZERO),
            new FeeRule(11, 20, BigDecimal.ZERO,         new BigDecimal("0.082")),
            new FeeRule(21, 30, BigDecimal.ZERO,         new BigDecimal("0.069")),
            new FeeRule(31, 40, BigDecimal.ZERO,         new BigDecimal("0.047")),
            new FeeRule(41, 50, BigDecimal.ZERO,         new BigDecimal("0.017"))
    );

    public BigDecimal calculate(BigDecimal amount, LocalDate schedulingDate, LocalDate transferDate) {
        long days = ChronoUnit.DAYS.between(schedulingDate, transferDate);

        if (days < 0) {
            throw new NoApplicableFeeException("Transfer date cannot be in the past. No applicable fee.");
        }

        return RULES.stream()
                .filter(rule -> rule.applies(days))
                .findFirst()
                .map(rule -> rule.calculate(amount))
                .orElseThrow(() -> new NoApplicableFeeException(
                        "No applicable fee for transfers more than 50 days ahead."));
    }

    private static class FeeRule {

        private final long min;
        private final long max;
        private final BigDecimal fixed;
        private final BigDecimal percent;

        FeeRule(long min, long max, BigDecimal fixed, BigDecimal percent) {
            this.min = min;
            this.max = max;
            this.fixed = fixed;
            this.percent = percent;
        }

        boolean applies(long days) {
            return days >= min && days <= max;
        }

        BigDecimal calculate(BigDecimal amount) {
            return fixed.add(amount.multiply(percent)).setScale(2, RoundingMode.HALF_UP);
        }
    }
}
