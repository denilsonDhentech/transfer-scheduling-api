package org.dhentech.domain;

import org.dhentech.domain.exception.NoApplicableFeeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FeeCalculatorTest {

    private FeeCalculator calculator;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        calculator = new FeeCalculator();
        today = LocalDate.now();
    }

    // --- same day (0 days) ---

    @Test
    void shouldApplyFixedPlusPercentWhenSameDay() {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal fee = calculator.calculate(amount, today, today);
        // 3.00 + 2.5% of 1000 = 3.00 + 25.00 = 28.00
        assertEquals(new BigDecimal("28.00"), fee);
    }

    // --- 1 to 10 days ---

    @Test
    void shouldApplyFixedTwelveWhenOneDayAhead() {
        BigDecimal fee = calculator.calculate(new BigDecimal("500.00"), today, today.plusDays(1));
        assertEquals(new BigDecimal("12.00"), fee);
    }

    @Test
    void shouldApplyFixedTwelveWhenTenDaysAhead() {
        BigDecimal fee = calculator.calculate(new BigDecimal("500.00"), today, today.plusDays(10));
        assertEquals(new BigDecimal("12.00"), fee);
    }

    // --- 11 to 20 days ---

    @Test
    void shouldApplyEightPointTwoPercentWhenElevenDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal fee = calculator.calculate(amount, today, today.plusDays(11));
        // 8.2% of 1000 = 82.00
        assertEquals(new BigDecimal("82.00"), fee);
    }

    @Test
    void shouldApplyEightPointTwoPercentWhenTwentyDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal fee = calculator.calculate(amount, today, today.plusDays(20));
        assertEquals(new BigDecimal("82.00"), fee);
    }

    // --- 21 to 30 days ---

    @Test
    void shouldApplySixPointNinePercentWhenTwentyOneDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal fee = calculator.calculate(amount, today, today.plusDays(21));
        // 6.9% of 1000 = 69.00
        assertEquals(new BigDecimal("69.00"), fee);
    }

    @Test
    void shouldApplySixPointNinePercentWhenThirtyDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal fee = calculator.calculate(amount, today, today.plusDays(30));
        assertEquals(new BigDecimal("69.00"), fee);
    }

    // --- 31 to 40 days ---

    @Test
    void shouldApplyFourPointSevenPercentWhenThirtyOneDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal fee = calculator.calculate(amount, today, today.plusDays(31));
        // 4.7% of 1000 = 47.00
        assertEquals(new BigDecimal("47.00"), fee);
    }

    @Test
    void shouldApplyFourPointSevenPercentWhenFortyDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal fee = calculator.calculate(amount, today, today.plusDays(40));
        assertEquals(new BigDecimal("47.00"), fee);
    }

    // --- 41 to 50 days ---

    @Test
    void shouldApplyOnePointSevenPercentWhenFortyOneDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal fee = calculator.calculate(amount, today, today.plusDays(41));
        // 1.7% of 1000 = 17.00
        assertEquals(new BigDecimal("17.00"), fee);
    }

    @Test
    void shouldApplyOnePointSevenPercentWhenFiftyDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal fee = calculator.calculate(amount, today, today.plusDays(50));
        assertEquals(new BigDecimal("17.00"), fee);
    }

    // --- edge cases: no applicable fee ---

    @Test
    void shouldThrowWhenFiftyOneDaysAhead() {
        assertThrows(NoApplicableFeeException.class,
                () -> calculator.calculate(new BigDecimal("1000.00"), today, today.plusDays(51)));
    }

    @Test
    void shouldThrowWhenDateIsInThePast() {
        assertThrows(NoApplicableFeeException.class,
                () -> calculator.calculate(new BigDecimal("1000.00"), today, today.minusDays(1)));
    }
}
