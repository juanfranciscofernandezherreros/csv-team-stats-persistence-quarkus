package com.example.csvteamstats.adapters.in.kafka;

import com.example.csvteamstats.avro.TeamStatsValue;
import com.example.csvteamstats.domain.model.TeamStats;
import jakarta.enterprise.context.ApplicationScoped;

/** Translates the external Avro contract into the internal domain model. */
@ApplicationScoped
public class AvroTeamStatsMapper {
    public TeamStats toDomain(TeamStatsValue value) {
        TeamStats stats = new TeamStats();
        stats.setSourceEventId(value.getSourceEventId());
        stats.setMatchId(value.getMatchId());
        stats.setPeriod(value.getPeriod());
        stats.setCategory(value.getCategory());
        stats.setMetric(value.getMetric());
        stats.setHomeTeam(value.getHomeTeam());
        stats.setHomeValue(value.getHomeValue());
        stats.setAwayTeam(value.getAwayTeam());
        stats.setAwayValue(value.getAwayValue());
        stats.setSourceUrl(value.getSourceUrl());
        return stats;
    }
}
