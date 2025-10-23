package io.hpp.noosphere.agent.service.blockchain.dto;

/**
 * Represents a subscription identifier for an off-chain delegated subscription.
 * It's uniquely identified by the owner's address and a nonce.
 * @param owner The address of the subscription owner.
 * @param nonce The nonce of the delegated subscription signature.
 */
public record DelegatedSubscriptionId(String owner, int nonce) implements SubscriptionIdentifier {}
