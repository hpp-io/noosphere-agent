package io.hpp.noosphere.agent.service.blockchain.dto;

/**
 * A data container holding whether a delegated subscription already exists on-chain and its corresponding ID.
 * @param exists true if the subscription exists
 * @param subscriptionId the ID of the subscription if it exists
 */
public record ExistingDelegateSubscription(boolean exists, long subscriptionId) {}
