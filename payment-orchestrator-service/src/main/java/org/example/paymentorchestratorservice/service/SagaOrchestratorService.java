package org.example.paymentorchestratorservice.service;

import org.example.paymentorchestratorservice.entity.LedgerEntry;
import org.example.paymentorchestratorservice.entity.TransactionRequest;
import org.example.paymentorchestratorservice.entity.TransactionState;
import org.example.paymentorchestratorservice.entity.Transactions;
import org.example.paymentorchestratorservice.handler.AccountHandler;
import org.example.paymentorchestratorservice.handler.AccountRouter;
import org.example.paymentorchestratorservice.repository.LedgerEntryRepository;
import org.example.paymentorchestratorservice.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SagaOrchestratorService {

    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccountRouter accountRouter;

    public SagaOrchestratorService(TransactionRepository transactionRepository, LedgerEntryRepository ledgerEntryRepository, AccountRouter accountRouter) {
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.accountRouter = accountRouter;
    }

    public Transactions sendMoney(UUID sourceUserId, String idempotencyKey, TransactionRequest request) {
        if(request.sourceId().startsWith("wal_") && request.destinationId().startsWith("wal_")) {
            return walletToWallet(idempotencyKey, sourceUserId, request);
        } else if(request.sourceId().startsWith("wal_") && request.destinationId().startsWith("mer_")) {
            // changes in the future, may be tax will be added
            return walletToWallet(idempotencyKey, sourceUserId, request);
        } else if(request.sourceId().startsWith("mer_") && request.destinationId().startsWith("mer_")) {
            // changes in the future, may be tax will be added
            throw new RuntimeException("Feature Not Available");
        } else if(request.sourceId().startsWith("mer_") && request.destinationId().startsWith("wal_")) {
            // changes in the future, may be tax will be added
            throw new RuntimeException("Feature Not Available");
        } else {
            throw new RuntimeException("Feature Not Available");
        }
    }


    public Transactions walletToWallet(String idempotencyKey, UUID sourceUserId, TransactionRequest request) {
        Optional<Transactions> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);

        if(existing.isPresent()) {
            return existing.get();
        }

        AccountHandler source = accountRouter.resolve(request.sourceId());
        AccountHandler destination = accountRouter.resolve(request.destinationId());

        Transactions tx = new Transactions(
                idempotencyKey,
                sourceUserId,
                request.sourceId(),
                request.destinationId(),
                request.amount(),
                "Paying to "+request.destinationId()
        );

        try {
            source.debit(sourceUserId, request.sourceId(), request.amount());
            tx.setStatus(TransactionState.SOURCE_DEBITED);
            transactionRepository.save(tx);

            destination.credit(request.destinationUserId(), request.destinationId(), request.amount());
            tx.setStatus(TransactionState.DESTINATION_CREDITED);
            transactionRepository.save(tx);

            handleLedgerEntry(tx, request);
            return tx;
        } catch (Exception e) {
            handleRollBack(tx, sourceUserId, request, source);
            return tx;
        }
    }

    private void handleRollBack(Transactions tx, UUID userId, TransactionRequest request, AccountHandler sourceHandler) {
        if(tx.getStatus() == TransactionState.SOURCE_DEBITED) {
            try {
                sourceHandler.credit(userId, request.sourceId(), request.amount());
                tx.setStatus(TransactionState.ROLLED_BACK);
            } catch (Exception e) {
                tx.setStatus(TransactionState.MANUAL_INTERVENTION_REQUIRED);
            }
        } else {
            tx.setStatus(TransactionState.FAILED);
        }
        tx.setDescription("Failed to Send");
        transactionRepository.save(tx);
    }

    private void handleLedgerEntry(Transactions tx, TransactionRequest request) {
        try {
            LedgerEntry debitEntry = new LedgerEntry(
                    tx.getId(),
                    request.sourceId(),
                    LedgerEntry.EntryDirection.DEBIT,
                    request.amount()
            );
            LedgerEntry creditEntry = new LedgerEntry(
                    tx.getId(),
                    request.destinationId(),
                    LedgerEntry.EntryDirection.CREDIT,
                    request.amount()
            );

            ledgerEntryRepository.saveAll(List.of(debitEntry, creditEntry));
            tx.setStatus(TransactionState.COMPLETED);
        } catch (Exception e) {
            tx.setStatus(TransactionState.PROCESSING);
        }
        transactionRepository.save(tx);
    }
}
