package pl.cleankod.exchange.core.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.cleankod.exchange.core.domain.Account;
import pl.cleankod.exchange.core.exception.AccountNotFound;
import pl.cleankod.exchange.core.gateway.AccountRepository;

public class FindAccountUseCase {
    private final static Logger LOGGER = LoggerFactory.getLogger(FindAccountUseCase.class);

    private final AccountRepository accountRepository;

    public FindAccountUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account execute(Account.Id id) {
        LOGGER.info("Searching account by id='{}'", id);
        return accountRepository.find(id)
                .orElseThrow(() -> new AccountNotFound("Account not found for id" + id));
    }

    public Account execute(Account.Number number) {
        LOGGER.info("Searching account by number='{}'", number);
        return accountRepository.find(number)
                .orElseThrow(() -> new AccountNotFound("Account not found by number" + number));
    }
}
