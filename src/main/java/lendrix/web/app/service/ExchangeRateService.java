package lendrix.web.app.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Getter
public class ExchangeRateService {

    private final RestTemplate restTemplate;

    private final Map<String, Double> rates = new HashMap<>();

    private final Set<String> CURRENCIES = Set.of(
        "GHS", "USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "NZD",
        "CNY", "HKD", "SGD", "INR", "ZAR", "BRL", "RUB", "KRW", "SEK",
        "NOK", "DKK", "MYR", "PHP", "IDR", "THB", "MXN", "TRY", "AED",
        "SAR", "EGP", "NGN", "KES", "TZS", "UGX"
    );

    @Value("${currencyApi.apiKey}")
    private String apiKey;

    public void getExchangeRate() {
        String CURRENCY_API = "https://api.freecurrencyapi.com/v1/latest?apikey="; //https://api.currencyapi.com/v3/latest?apikey=
        var response = restTemplate.getForEntity(CURRENCY_API + apiKey, JsonNode.class);
        var data = Objects.requireNonNull(response.getBody()).get("data");

        for (var currency : CURRENCIES) {
            rates.put(currency, data.get(currency).get("value").doubleValue());
        }

        System.out.println(rates);
    }
}


