package pl.cleankod.exchange.entrypoint;

import org.springframework.web.bind.annotation.*;
import pl.cleankod.exchange.core.domain.Account;
import pl.cleankod.exchange.entrypoint.coreadapter.AccountUseCaseAdapter;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountUseCaseAdapter accountUseCaseAdapter;

    public AccountController(AccountUseCaseAdapter accountUseCaseAdapter) {
        this.accountUseCaseAdapter = accountUseCaseAdapter;
    }

    @GetMapping(path = "/{id}")
    public Account findAccountById(@PathVariable String id, @RequestParam(required = false) String currency) {
        return accountUseCaseAdapter.findAccountById(id, currency);
    }

    @GetMapping(path = "/number={number}")
    public Account findAccountByNumber(@PathVariable String number, @RequestParam(required = false) String currency) {
        return accountUseCaseAdapter.findAccountByNumber(number, currency);
    }
}
