package ru.dreadblade.czarbank.util;

import org.apache.commons.lang3.RandomStringUtils;

public class RecoveryCodeTestUtils {
    public static String generateRandomRecoveryCode() {
        return RandomStringUtils.randomAlphabetic(4) + "-" + RandomStringUtils.randomAlphabetic(4) + "-" +
                RandomStringUtils.randomAlphabetic(4) + "-" + RandomStringUtils.randomAlphabetic(4);
    }
}
