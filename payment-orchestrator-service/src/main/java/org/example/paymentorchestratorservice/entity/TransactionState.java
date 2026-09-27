package org.example.paymentorchestratorservice.entity;

public enum TransactionState {
    PENDING,
    SOURCE_DEBITED,
    DESTINATION_CREDITED,
    COMPLETED,
    FAILED,
    ROLLED_BACK,
    PROCESSING,
    MANUAL_INTERVENTION_REQUIRED
}
