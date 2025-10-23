package io.hpp.noosphere.agent.service.blockchain.dto;

/**
 * A data container for the status of a transaction.
 * @param found   true if the transaction was found on-chain.
 * @param success true if the transaction was successful (status == 1).
 */
public record TxStatus(boolean found, boolean success) {}
