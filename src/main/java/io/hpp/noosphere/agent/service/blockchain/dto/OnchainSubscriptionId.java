package io.hpp.noosphere.agent.service.blockchain.dto;

/**
 * Represents a subscription identifier for an on-chain subscription.
 * @param id The numeric ID of the subscription.
 */
public record OnchainSubscriptionId(long id, long interval) implements SubscriptionIdentifier {}
