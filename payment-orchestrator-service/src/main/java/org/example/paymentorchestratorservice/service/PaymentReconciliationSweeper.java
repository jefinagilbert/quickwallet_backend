package org.example.paymentorchestratorservice.service;

import org.example.paymentorchestratorservice.entity.LedgerEntry;
import org.example.paymentorchestratorservice.entity.TransactionState;
import org.example.paymentorchestratorservice.entity.Transactions;
import org.example.paymentorchestratorservice.handler.AccountHandler;
import org.example.paymentorchestratorservice.handler.AccountRouter;
import org.example.paymentorchestratorservice.repository.LedgerEntryRepository;
import org.example.paymentorchestratorservice.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PaymentReconciliationSweeper {

    private static final Logger log = LoggerFactory.getLogger(PaymentReconciliationSweeper.class);
    private static final int MAX_RETRIES = 3;

    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccountRouter accountRouter;

    public PaymentReconciliationSweeper(TransactionRepository transactionRepository, LedgerEntryRepository ledgerEntryRepository, AccountRouter accountRouter) {
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.accountRouter = accountRouter;
    }

    @Scheduled(fixedDelay = 30000)
    public void reconcilePendingLedgers() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(45);
        List<Transactions> hanging = transactionRepository.findByStatusAndUpdatedAtBefore(TransactionState.PROCESSING, cutoff);

        for (Transactions tx : hanging) {
            try {
                LedgerEntry debitEntry = new LedgerEntry(
                        tx.getId(),
                        tx.getSourceAccount(),
                        LedgerEntry.EntryDirection.DEBIT,
                        tx.getAmount()
                );

                LedgerEntry creditEntry = new LedgerEntry(
                        tx.getId(),
                        tx.getDestinationAccount(),
                        LedgerEntry.EntryDirection.CREDIT,
                        tx.getAmount()
                );

                ledgerEntryRepository.saveAll(List.of(debitEntry, creditEntry));
                tx.setStatus(TransactionState.COMPLETED);
                tx.setDescription("Ledger entries finalized by sweeper");
                transactionRepository.save(tx);
            } catch (Exception e) {
                log.error("Sweeper failed to write ledger entries for tx {}: {}", tx.getId(), e.getMessage());
            }
        }



    }

    @Scheduled(fixedDelay = 30000)
    public void reconcileFailedRollbacks() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(45);
        List<Transactions> abandonedDebits =
                transactionRepository.findByStatusAndUpdatedAtBefore(TransactionState.SOURCE_DEBITED, cutoff);

        for (Transactions tx : abandonedDebits) {
            if (tx.getRetryCount() >= MAX_RETRIES) {
                tx.setStatus(TransactionState.MANUAL_INTERVENTION_REQUIRED);
                tx.setDescription("Rollback failed after " + MAX_RETRIES + " automated attempts");
                transactionRepository.save(tx);
                continue;
            }

            try {
                tx.incrementRetryCount();
                AccountHandler sourceHandler = accountRouter.resolve(tx.getSourceAccount());
                sourceHandler.credit(tx.getSourceUserId(), tx.getSourceAccount(), tx.getAmount());

                tx.setStatus(TransactionState.ROLLED_BACK);
                tx.setDescription("Compensated via background sweeper");
                transactionRepository.save(tx);

            } catch (Exception e) {
                log.error("Sweeper rollback failed for tx {}: {}", tx.getId(), e.getMessage());
                transactionRepository.save(tx);
            }
        }
    }
}
