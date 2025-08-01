package lendrix.web.app.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConvertDto {

    private String fromCurrency;
    private String toCurrency;
    private double amount;

}
