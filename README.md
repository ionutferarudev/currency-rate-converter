# About
This is an example project that calculates the amount balance to a given currency. It is based on the [cleankod/architecture-archetype](https://github.com/cleankod/architecture-archetype) concept.

# Requirements
* JDK 17
* Gradle 7.4 (you can use the gradle wrapper instead)

# REST API
## Get account
Endpoints:
* `GET /accounts/{id}`
* `GET /accounts/number={number}`

Parameters:
* `currency` (not required) - calculate the account balance based on the today's average currency rate.

Sample request by ID:
```
http://localhost:8080/accounts/fa07c538-8ce4-11ec-9ad5-4f5a625cd744?currency=EUR
```

Sample request by account number:
```
http://localhost:8080/accounts/number=65+1090+1665+0000+0001+0373+7343?currency=PLN
```

Will produce:
```json
{
    "balance": {
        "amount": 27.27,
        "currency": "EUR"
    },
    "id": {
        "value": "fa07c538-8ce4-11ec-9ad5-4f5a625cd744"
    },
    "number": {
        "value": "65 1090 1665 0000 0001 0373 7343"
    }
}
```

# Assumptions and design decisions
## Black-box testing
Black-box testing is mostly used in order to favor refactoring. It is much simpler to completely change the underlying
implementation of a use case without changing the tests.

## Framework-less tests
Only the `BaseApplicationSpecification` contains library-specific code but no framework-specific initialization.
This approach eases the migration to other potential framework or toolset. The whole specification for the project
stays the same.

## Framework-less modules' core
Wherever possible, no Framework-specific or library-specific stuff was used inside the actual modules' core.
This also eases potential framework change or upgrade. The framework upgrade could also be more seamless for all
of those changes that are not backwards compatible because framework specific stuff is kept in one place and the
business logic is not polluted.

## Value-objects
There is no simple value passed around in the project. Every business value is encapsulated within a value-object.
It increases readability, enables nice methods override
(instead of: `findAccountById(String id)`, `findAccountByNumber(String accountNumber)`,
you can use: `find(Account.Id id)`, `find(Account.Number number)`), encapsulates internal data type.

Also, value-objects are responsible for a little more than just plain data holding.

# To do
* Rounding when calculating the amount is not done correctly for this type of operation (we're loosing money!) and it is done in the wrong place.
  * Fixed the rounding problem, by using the default rounding method (HALF_EVEN) when calculating money. As improvement, we could consider using JSR 354: [Money and Currency API](https://jcp.org/en/jsr/detail?id=354)
* Investigate whether it is possible to implement the value-object serialization, to avoid `value` nested field in JSON. See [#10](https://github.com/cleankod/currency-rate-converter/pull/10) as a starting point. Or maybe there is a better solution to the problem at hand?
  * I think that the solution proposed fits just right, because it doesn't pollute the domain records with annotations or any framework specific code.
* Move parameter-specific logic outside the controller.
  * Moved the logic into an account core adapter.
  * Replaced optional with exception -> AccountNotFound
* Better error handling, especially of potential errors from NBP API.
  * Added exception handling for the NBP API client + E2E integration test
  * Added Integration test for the NBP API client
* Caching the NBP API results.
  * Implemented caching using the default implementation from spring (Concurrent HashMap) + TTL at midnight
  * Added integration test for caching
* Circuit-breaker for the NBP API client.
  * Added Resilience4j and implemented retry + circuit breaker for the NBP API
  * Added integration tests for the retry + circuit breaker
* Better logging with traceability.
  * Added logging on the e2e flow + logging exception messages in the controller advice
* Replace exceptions with `Result` (`either`) which improves the overall methods API readability and forces error handling. Look into [cleankod/architecture-archetype](https://github.com/cleankod/architecture-archetype) as a starting point.
  * I already cleaned up the API and I increased the readability, by decoupling the happy path from the errors ones - which were implemented by throwing exception then catching and treating them in the controller advice.
  * The solution proposed is interesting as well and as mentioned, it forces you to error handling. Although, from my point of view it becomes too verbose.
* Test coverage report.
  * We can achieve this using the jacoco plugin, which can integrate easily with sonar later. 
  * By running locally the tests with coverage in IntelliJ, the report says 95% coverage, which is pretty good.
* Auto generating REST API docs.
  * We could use springdoc-openapi to generate the REST API spec, but first we need to write the documentation of the API using the specific annotations. -  this will be the code first approach
  * We could also write the openapi schema of the REST API ourselves and then generate the code based on it. - this is the API first approach
* Integration tests with the real NBP API.
  * I wrote additional integration tests using Wiremock for the responses from NBP API
* Replace Spring Framework with a different one.
  * Interesting idea, we can definitely do so, since the core implementation doesn't contain any framework specific code. 
* The proposed architecture is not perfect. Suggest improvements.
  * The architecture itself it's a pretty good one, there is a clear separation of responsibilities, also a separation between the domain and integrations (other APIs or the database)
  * Of course, the separation of classes in packages could be improved - I also did some of that
  * I'd also suggest using API first approach
