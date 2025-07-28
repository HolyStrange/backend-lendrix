package lendrix.web.app.util;

import java.math.BigDecimal;

public class FraudDetectionUtil {
    public static boolean isSuspicious(BigDecimal amount) {
        // Example: flag if amount > $10,000
        return amount.compareTo(new BigDecimal("10000")) > 0;
    }
}