package pl.cleankod.exchange.core.exception;

public class ExchangeRatesServiceUnavailable extends RuntimeException {

    public ExchangeRatesServiceUnavailable(String message, Throwable cause) {
        super(message, cause);
    }
}
