package lendrix.web.app.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepositDto {

    private String accountCode;   // The account you want to deposit into
    private BigDecimal amount;    // Amount to deposit
    private String paymentMethod; // Optional: "Stripe", "Bank Transfer", "Mobile Money", etc.

}
