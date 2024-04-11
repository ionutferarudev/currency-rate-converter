package pl.cleankod.exchange.provider.nbp

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import feign.Feign
import feign.FeignException
import feign.httpclient.ApacheHttpClient
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import pl.cleankod.exchange.provider.nbp.model.Rate
import pl.cleankod.exchange.provider.nbp.model.RateWrapper
import spock.lang.Specification

class ExchangeRatesNbpClientTest extends Specification {

    private static ExchangeRatesNbpClient exchangeRatesNbpClient

    private static WireMockServer wireMockServer = new WireMockServer(
            WireMockConfiguration.options()
                    .port(8082)
    )

    def setupSpec() {
        wireMockServer.start()

        String nbpApiBaseUrl = "http://localhost:8082"

        exchangeRatesNbpClient = Feign.builder()
                .client(new ApacheHttpClient())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(ExchangeRatesNbpClient.class, nbpApiBaseUrl)
    }

    def "should return a the rates from NBP"() {
        given:
        def table = "A"
        def currency = "EUR"
        def body = '{"table":"A","currency":"euro","code":"EUR","rates":[{"no":"026/A/NBP/2022","effectiveDate":"2022-02-08","mid":4.5452}]}'
        wireMockServer.stubFor(
                WireMock.get("/exchangerates/rates/A/EUR/2022-02-08")
                        .willReturn(WireMock.ok(body))
        )

        when:
        RateWrapper response = exchangeRatesNbpClient.fetch(table, currency)

        then:
        response == new RateWrapper("A", "euro", "EUR",
                List.of(new Rate("026/A/NBP/2022", "2022-02-08", BigDecimal.valueOf(4.5452))))
    }

    def "should throw FeignException.InternalServerError when NBP API fails with 500"() {
        given:
        def table = "A"
        def currency = "ERR500"
        wireMockServer.stubFor(
                WireMock.get("/exchangerates/rates/A/ERR500/2022-02-08")
                        .willReturn(WireMock.serverError())
        )

        when:
        exchangeRatesNbpClient.fetch(table, currency)

        then:
        thrown(FeignException.InternalServerError)
    }

    def "should throw FeignException.BadRequest when NBP API fails with 400"() {
        given:
        def table = "A"
        def currency = "ERR400"
        wireMockServer.stubFor(
                WireMock.get("/exchangerates/rates/A/ERR400/2022-02-08")
                        .willReturn(WireMock.badRequest())
        )

        when:
        exchangeRatesNbpClient.fetch(table, currency)

        then:
        thrown(FeignException.BadRequest)
    }
}
