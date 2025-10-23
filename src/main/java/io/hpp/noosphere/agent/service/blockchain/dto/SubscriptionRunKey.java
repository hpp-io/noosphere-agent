package io.hpp.noosphere.agent.service.blockchain.dto;

/**
 * Represents the unique key for a specific run of a subscription at a given interval.
 * Used as a key in maps tracking pending transactions and retry attempts.
 */
public record SubscriptionRunKey(SubscriptionIdentifier subscriptionId, long interval) {}
