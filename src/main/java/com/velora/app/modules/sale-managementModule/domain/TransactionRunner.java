package com.velora.app.modules.sale_managementModule.domain;

/**
 * Abstraction for running a unit of work in a single database transaction.
 */
public interface TransactionRunner {
    void runInTransaction(Runnable work);
}
