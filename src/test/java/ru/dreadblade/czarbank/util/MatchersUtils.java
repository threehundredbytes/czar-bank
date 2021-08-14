package ru.dreadblade.czarbank.util;

import org.hamcrest.Matcher;
import org.hamcrest.number.BigDecimalCloseTo;

import java.math.BigDecimal;

public class MatchersUtils {
    public static final BigDecimal DEFAULT_PRECISION = new BigDecimal("0.01");

    public static Matcher<BigDecimal> closeTo(BigDecimal value) {
        return BigDecimalCloseTo.closeTo(value, DEFAULT_PRECISION);
    }
}
