package com.velora.app.core.domain.salemanagement;

/**
 * Abstraction for running a unit of work in a single database transaction.
 */
public interface TransactionRunner {
    void runInTransaction(Runnable work);
}
