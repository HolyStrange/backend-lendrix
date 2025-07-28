package lendrix.web.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {


    private String code;

    private String label;

    private char symbol;

    private String accountName; // This will be used to create an account with an accountName

    private double initialBalance; // Initial balance for the account
}
