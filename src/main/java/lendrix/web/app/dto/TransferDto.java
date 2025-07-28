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
public class TransferDto {

    private String recipientAccountNumber;

    private BigDecimal amount;

    private String senderAccountCode;


    public String getSenderAccountCode() {
        return senderAccountCode;
    }

    public String getRecipientAccountNumber() {
        return recipientAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
