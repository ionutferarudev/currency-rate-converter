package pl.cleankod.exchange.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.cleankod.exchange.core.domain.Money;
import pl.cleankod.exchange.core.gateway.CurrencyConversionService;
import pl.cleankod.exchange.provider.nbp.ExchangeRatesNbpClient;
import pl.cleankod.exchange.provider.nbp.model.RateWrapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public class CurrencyConversionNbpService implements CurrencyConversionService {
    private final static Logger LOGGER = LoggerFactory.getLogger(CurrencyConversionNbpService.class);

    private final ExchangeRatesNbpClient exchangeRatesNbpClient;

    public CurrencyConversionNbpService(ExchangeRatesNbpClient exchangeRatesNbpClient) {
        this.exchangeRatesNbpClient = exchangeRatesNbpClient;
    }

    @Override
    public Money convert(Money money, Currency targetCurrency) {
        LOGGER.info("Exchanging from '{}' to '{}'", money.currency(), targetCurrency);
        RateWrapper rateWrapper = exchangeRatesNbpClient.fetch("A", targetCurrency.getCurrencyCode());
        BigDecimal midRate = rateWrapper.rates().get(0).mid().setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal calculatedRate = money.amount().divide(midRate, RoundingMode.HALF_EVEN);
        return new Money(calculatedRate, targetCurrency);
    }
}
