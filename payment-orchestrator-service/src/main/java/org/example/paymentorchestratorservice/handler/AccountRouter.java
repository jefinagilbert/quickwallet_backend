package org.example.paymentorchestratorservice.handler;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountRouter {

    private final List<AccountHandler> handlers;

    public AccountRouter(List<AccountHandler> handlers) {
        this.handlers = handlers;
    }

    public AccountHandler resolve(String accountIdentifier) {
        if (accountIdentifier == null || accountIdentifier.isBlank()) {
            throw new IllegalArgumentException("Invalid account identifier: cannot be null or empty");
        }

        return handlers.stream()
                .filter(handler -> handler.supports(accountIdentifier))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No handler found for account identifier: " + accountIdentifier));
    }
}
