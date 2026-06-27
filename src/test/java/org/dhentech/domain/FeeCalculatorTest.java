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


    @Test
    void shouldApplyFixedPlusPercentWhenSameDay() {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal fee = calculator.calculate(amount, today, today);
        assertEquals(new BigDecimal("28.00"), fee);
    }


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


    @Test
    void shouldApplyEightPointTwoPercentWhenElevenDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal fee = calculator.calculate(amount, today, today.plusDays(11));
        assertEquals(new BigDecimal("82.00"), fee);
    }

    @Test
    void shouldApplyEightPointTwoPercentWhenTwentyDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal fee = calculator.calculate(amount, today, today.plusDays(20));
        assertEquals(new BigDecimal("82.00"), fee);
    }


    @Test
    void shouldApplySixPointNinePercentWhenTwentyOneDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal fee = calculator.calculate(amount, today, today.plusDays(21));
        assertEquals(new BigDecimal("69.00"), fee);
    }

    @Test
    void shouldApplySixPointNinePercentWhenThirtyDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal fee = calculator.calculate(amount, today, today.plusDays(30));
        assertEquals(new BigDecimal("69.00"), fee);
    }


    @Test
    void shouldApplyFourPointSevenPercentWhenThirtyOneDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal fee = calculator.calculate(amount, today, today.plusDays(31));
        assertEquals(new BigDecimal("47.00"), fee);
    }

    @Test
    void shouldApplyFourPointSevenPercentWhenFortyDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal fee = calculator.calculate(amount, today, today.plusDays(40));
        assertEquals(new BigDecimal("47.00"), fee);
    }


    @Test
    void shouldApplyOnePointSevenPercentWhenFortyOneDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal fee = calculator.calculate(amount, today, today.plusDays(41));
        assertEquals(new BigDecimal("17.00"), fee);
    }

    @Test
    void shouldApplyOnePointSevenPercentWhenFiftyDaysAhead() {
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal fee = calculator.calculate(amount, today, today.plusDays(50));
        assertEquals(new BigDecimal("17.00"), fee);
    }


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
