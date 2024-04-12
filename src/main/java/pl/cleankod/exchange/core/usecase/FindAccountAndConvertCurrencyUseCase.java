package pl.cleankod.exchange.core.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.cleankod.exchange.core.domain.Account;
import pl.cleankod.exchange.core.domain.Money;
import pl.cleankod.exchange.core.exception.AccountNotFound;
import pl.cleankod.exchange.core.exception.CurrencyConversionException;
import pl.cleankod.exchange.core.gateway.AccountRepository;
import pl.cleankod.exchange.core.gateway.CurrencyConversionService;

import java.util.Currency;

public class FindAccountAndConvertCurrencyUseCase {
    private final static Logger LOGGER = LoggerFactory.getLogger(FindAccountAndConvertCurrencyUseCase.class);

    private final AccountRepository accountRepository;
    private final CurrencyConversionService currencyConversionService;
    private final Currency baseCurrency;

    public FindAccountAndConvertCurrencyUseCase(AccountRepository accountRepository,
                                                CurrencyConversionService currencyConversionService,
                                                Currency baseCurrency) {
        this.accountRepository = accountRepository;
        this.currencyConversionService = currencyConversionService;
        this.baseCurrency = baseCurrency;
    }

    public Account execute(Account.Id id, Currency targetCurrency) {
        LOGGER.info("Searching account by id='{}' and currency='{}'", id, targetCurrency);
        return accountRepository.find(id)
                .map(account -> new Account(account.id(), account.number(), convert(account.balance(), targetCurrency)))
                .orElseThrow(() -> new AccountNotFound("Account not found by id " + id));
    }

    public Account execute(Account.Number number, Currency targetCurrency) {
        LOGGER.info("Searching account by number='{}' and currency='{}'", number, targetCurrency);
        return accountRepository.find(number)
                .map(account -> new Account(account.id(), account.number(), convert(account.balance(), targetCurrency)))
                .orElseThrow(() -> new AccountNotFound("Account not found by number " + number));
    }

    private Money convert(Money money, Currency targetCurrency) {
        if (!baseCurrency.equals(targetCurrency)) {
            return money.convert(currencyConversionService, targetCurrency);
        }

        if (!money.currency().equals(targetCurrency)) {
            throw new CurrencyConversionException(money.currency(), targetCurrency);
        }

        return money;
    }
}
