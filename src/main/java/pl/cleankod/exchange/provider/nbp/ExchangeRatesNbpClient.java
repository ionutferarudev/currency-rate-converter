package pl.cleankod.exchange.provider.nbp;

import feign.Param;
import feign.RequestLine;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cache.annotation.Cacheable;
import pl.cleankod.exchange.core.exception.ExchangeRatesServiceUnavailable;
import pl.cleankod.exchange.provider.nbp.model.RateWrapper;

import static pl.cleankod.config.SpringCachingConfig.EXCHANGE_RATE_CACHE_KEY;

public interface ExchangeRatesNbpClient {
    @RequestLine("GET /exchangerates/rates/{table}/{currency}/2022-02-08")
    @Cacheable(EXCHANGE_RATE_CACHE_KEY)
    @Retry(name = "nbpApiClient")
    @CircuitBreaker(name = "nbpApiClientCircuitBreaker", fallbackMethod = "fetchFallbackMethod")
    RateWrapper fetch(@Param("table") String table, @Param("currency") String currency);

    default RateWrapper fetchFallbackMethod(String table, String currency, Throwable e) {
        throw new ExchangeRatesServiceUnavailable("Fallback fetch exchange rates method was used since NBP service is unavailable", e);
    }
}
