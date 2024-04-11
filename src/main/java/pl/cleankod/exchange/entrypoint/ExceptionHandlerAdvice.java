package pl.cleankod.exchange.entrypoint;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.cleankod.exchange.core.exception.AccountNotFound;
import pl.cleankod.exchange.core.exception.CurrencyConversionException;
import pl.cleankod.exchange.core.exception.ExchangeRatesServiceUnavailable;
import pl.cleankod.exchange.entrypoint.model.ApiError;

@ControllerAdvice
public class ExceptionHandlerAdvice {

    private final static Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler({
            CurrencyConversionException.class,
            IllegalArgumentException.class
    })
    protected ResponseEntity<ApiError> handleBadRequest(CurrencyConversionException ex) {
        return ResponseEntity.badRequest().body(new ApiError(ex.getMessage()));
    }

    @ExceptionHandler({
            AccountNotFound.class
    })
    protected ResponseEntity<Void> handleAccountNotFound(AccountNotFound ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler({
            FeignException.class, ExchangeRatesServiceUnavailable.class
    })
    protected ResponseEntity<Void> handleNbpClientException(RuntimeException ex) {
        LOGGER.error("handleNbpClientException", ex);
        return ResponseEntity.internalServerError().build();
    }
}
