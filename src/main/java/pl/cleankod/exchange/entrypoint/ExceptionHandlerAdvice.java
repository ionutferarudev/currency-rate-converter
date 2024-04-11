package pl.cleankod.exchange.entrypoint;

import feign.FeignException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.cleankod.exchange.core.exception.AccountNotFound;
import pl.cleankod.exchange.core.exception.CurrencyConversionException;
import pl.cleankod.exchange.entrypoint.model.ApiError;

@ControllerAdvice
public class ExceptionHandlerAdvice {

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
            FeignException.class
    })
    protected ResponseEntity<Void> handleNbpClientException(FeignException ex) {
        return ResponseEntity.internalServerError().build();
    }
}
