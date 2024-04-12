package pl.cleankod.exchange.entrypoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import pl.cleankod.exchange.core.domain.Account;
import pl.cleankod.exchange.entrypoint.coreadapter.AccountUseCaseAdapter;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final static Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

    private final AccountUseCaseAdapter accountUseCaseAdapter;

    public AccountController(AccountUseCaseAdapter accountUseCaseAdapter) {
        this.accountUseCaseAdapter = accountUseCaseAdapter;
    }

    @GetMapping(path = "/{id}")
    public Account findAccountById(@PathVariable String id, @RequestParam(required = false) String currency) {
        LOGGER.info("Received get by id='{}' and currency='{}'", id, currency);
        return accountUseCaseAdapter.findAccountById(id, currency);
    }

    @GetMapping(path = "/number={number}")
    public Account findAccountByNumber(@PathVariable String number, @RequestParam(required = false) String currency) {
        LOGGER.info("Received get by number='{}' and currency='{}'", number, currency);
        return accountUseCaseAdapter.findAccountByNumber(number, currency);
    }
}
