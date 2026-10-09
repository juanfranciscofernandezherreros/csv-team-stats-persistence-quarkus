package com.example.csvteamstats.domain.model;

/** Kafka coordinates retained for a functional rejection, without Kafka types in the domain. */
public record MessageOrigin(String topic, int partition, long offset) {}
