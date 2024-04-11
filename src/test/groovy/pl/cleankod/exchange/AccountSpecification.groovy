package pl.cleankod.exchange

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.apache.http.HttpResponse
import pl.cleankod.BaseApplicationSpecification
import pl.cleankod.exchange.core.domain.Account
import pl.cleankod.exchange.core.domain.Money

import java.nio.charset.StandardCharsets

class AccountSpecification extends BaseApplicationSpecification {

    private static WireMockServer wireMockServer = new WireMockServer(
            WireMockConfiguration.options()
                    .port(8081)
    )

    def setupSpec() {
        wireMockServer.start()
        WireMock.configureFor("localhost", 8081)

        def body = '{"table":"A","currency":"euro","code":"EUR","rates":[{"no":"026/A/NBP/2022","effectiveDate":"2022-02-08","mid":4.5452}]}'
        wireMockServer.stubFor(
                WireMock.get("/exchangerates/rates/A/EUR/2022-02-08")
                        .willReturn(WireMock.ok(body))
        )
    }

    def cleanupSpec() {
        wireMockServer.stop()
    }

    def "should return an account by ID"() {
        given:
        def accountId = "fa07c538-8ce4-11ec-9ad5-4f5a625cd744"

        when:
        Account response = get("/accounts/${accountId}", Account)

        then:
        response == new Account(
                Account.Id.of(accountId),
                Account.Number.of("65 1090 1665 0000 0001 0373 7343"),
                Money.of("123.45", "PLN")
        )
    }

    def "should return an account by ID with different currency"() {
        given:
        def accountId = "fa07c538-8ce4-11ec-9ad5-4f5a625cd744"
        def currency = "EUR"

        when:
        Account response = get("/accounts/${accountId}?currency=${currency}", Account)

        then:
        response == new Account(
                Account.Id.of(accountId),
                Account.Number.of("65 1090 1665 0000 0001 0373 7343"),
                Money.of("27.13", currency)
        )
    }

    def "should call NBP API only once then use the values from the cache for the rest of calls"() {
        given:
        def accountId = "fa07c538-8ce4-11ec-9ad5-4f5a625cd744"
        def currency = "EUR"

        when:
        Account response = get("/accounts/${accountId}?currency=${currency}", Account)
        Account response1 = get("/accounts/${accountId}?currency=${currency}", Account)
        Account response2 = get("/accounts/${accountId}?currency=${currency}", Account)

        then:
        response == response1 && response1 == response2 && response2 == new Account(
                Account.Id.of(accountId),
                Account.Number.of("65 1090 1665 0000 0001 0373 7343"),
                Money.of("27.13", currency)
        )
        WireMock.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/exchangerates/rates/A/EUR/2022-02-08")))
    }

    def "should return 500 internal server error when NBP API fails with any status code"() {
        given:
        def accountId = "fa07c538-8ce4-11ec-9ad5-4f5a625cd744"
        wireMockServer.stubFor(
                WireMock.get("/exchangerates/rates/A/" + currency + "/2022-02-08")
                        .willReturn(WireMock.status(nbpApiStatusCode))
        )

        when:
        def response = getResponse("/accounts/${accountId}?currency=${currency}")

        then:
        response.getStatusLine().getStatusCode() == 500

        where:
        nbpApiStatusCode << [400, 401, 404, 500, 503]
        currency << ["A", "B", "C", "D", "E"]
    }

    def "should retry to call NBP API 3 times when it fails with 500 status code"() {
        given:
        def accountId = "fa07c538-8ce4-11ec-9ad5-4f5a625cd744"
        def currency = "USD"

        wireMockServer.stubFor(
                WireMock.get("/exchangerates/rates/A/USD/2022-02-08")
                        .willReturn(WireMock.serverError())
        )

        when:
        def response = getResponse("/accounts/${accountId}?currency=${currency}")

        then:
        response.getStatusLine().getStatusCode() == 500

        WireMock.verify(3, WireMock.getRequestedFor(WireMock.urlEqualTo("/exchangerates/rates/A/USD/2022-02-08")))
    }

    def "circuit breaker should be in state open and stop the requests after the NBP API fails more than 4 times with 500 status code"() {
        given:
        def accountId = "fa07c538-8ce4-11ec-9ad5-4f5a625cd744"
        def currency = "USD"

        wireMockServer.stubFor(
                WireMock.get("/exchangerates/rates/A/USD/2022-02-08")
                        .willReturn(WireMock.serverError())
        )

        when:
        def response = getResponse("/accounts/${accountId}?currency=${currency}") // 1 + 3 retries -> fallback method
        def response2 = getResponse("/accounts/${accountId}?currency=${currency}") // fallback method
        def response3 = getResponse("/accounts/${accountId}?currency=${currency}") // fallback method

        then:
        response.getStatusLine().getStatusCode() == 500
        response2.getStatusLine().getStatusCode() == 500
        response3.getStatusLine().getStatusCode() == 500

        WireMock.verify(4, WireMock.getRequestedFor(WireMock.urlEqualTo("/exchangerates/rates/A/USD/2022-02-08")))
    }

    def "should return an account by number"() {
        given:
        def accountNumberValue = "75 1240 2034 1111 0000 0306 8582"
        def accountNumberUrlEncoded = URLEncoder.encode(accountNumberValue, StandardCharsets.UTF_8)

        when:
        Account response = get("/accounts/number=${accountNumberUrlEncoded}", Account)

        then:
        response == new Account(
                Account.Id.of("78743420-8ce9-11ec-b0d0-57b77255c208"),
                Account.Number.of(accountNumberValue),
                Money.of("456.78", "EUR")
        )
    }

    def "should return an account by number with different currency"() {
        given:
        def accountNumberValue = "75 1240 2034 1111 0000 0306 8582"
        def accountNumberUrlEncoded = URLEncoder.encode(accountNumberValue, StandardCharsets.UTF_8)

        when:
        HttpResponse response = getResponse("/accounts/number=${accountNumberUrlEncoded}?currency=PLN")

        then:
        response.getStatusLine().getStatusCode() == 400
        transformError(response).message() == "Cannot convert currency from EUR to PLN."
    }

    def "should not find an account by ID"() {
        given:
        def accountId = "ac270f3a-8d08-11ec-8b91-9bcdf6e2522a"

        when:
        def response = getResponse("/accounts/${accountId}")

        then:
        response.getStatusLine().getStatusCode() == 404
    }

    def "should not find an account by number"() {
        given:
        def accountNumber = URLEncoder.encode("11 1750 0009 0000 0000 2156 6004", StandardCharsets.UTF_8)

        when:
        def response = getResponse("/accounts/number=${accountNumber}")

        then:
        response.getStatusLine().getStatusCode() == 404
    }
}
