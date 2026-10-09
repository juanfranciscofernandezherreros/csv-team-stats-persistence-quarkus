package com.example.csvteamstats.domain.model;

import com.example.csvteamstats.domain.validation.TeamStatsFunctionalValidator.Failure;

public record TeamStatsRejection(TeamStats stats, Failure failure, MessageOrigin origin) {}
