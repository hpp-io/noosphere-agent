package io.hpp.noosphere.agent.service.blockchain.dto;

/**
 * A marker interface to represent a unique subscription identifier, which can be
 * either an on-chain ID or an off-chain delegated ID.
 */
public sealed interface SubscriptionIdentifier permits OnchainSubscriptionId, DelegatedSubscriptionId {}
