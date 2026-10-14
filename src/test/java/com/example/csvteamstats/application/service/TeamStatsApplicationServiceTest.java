package com.example.csvteamstats.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.csvteamstats.application.port.out.PersistedSuccessEvents;
import com.example.csvteamstats.application.port.out.RejectedTeamStatsEvents;
import com.example.csvteamstats.application.port.out.TeamStatsStore;
import com.example.csvteamstats.domain.model.IncomingTeamStats;
import com.example.csvteamstats.domain.model.MessageOrigin;
import com.example.csvteamstats.domain.model.TeamStats;
import com.example.csvteamstats.domain.model.TeamStatsRejection;
import com.example.csvteamstats.domain.validation.TeamStatsFunctionalValidator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TeamStatsApplicationServiceTest {
    @Test
    void collapsesDuplicateNaturalKeysBeforeUpsertAndSuccessPublication() {
        CapturingStore store = new CapturingStore();
        CapturingSuccess success = new CapturingSuccess();
        TeamStatsApplicationService service = new TeamStatsApplicationService(
                store, success, rejection -> {}, new TeamStatsFunctionalValidator());
        TeamStats first = stats("60");
        TeamStats replacement = stats("62");

        var result = service.processBatch(List.of(
                new IncomingTeamStats(first, new MessageOrigin("parsed", 0, 1)),
                new IncomingTeamStats(replacement, new MessageOrigin("parsed", 0, 2))));

        assertEquals(1, result.persisted());
        assertEquals(List.of(replacement), store.batch);
        assertEquals(List.of(replacement), success.batch);
    }

    private static TeamStats stats(String homeValue) {
        TeamStats stats = new TeamStats();
        stats.setSourceEventId("event"); stats.setMatchId("match"); stats.setPeriod("Overall");
        stats.setCategory("Scoring"); stats.setMetric("Points"); stats.setHomeTeam("Home");
        stats.setHomeValue(homeValue); stats.setAwayTeam("Away"); stats.setAwayValue("55");
        return stats;
    }

    private static final class CapturingStore implements TeamStatsStore {
        private List<TeamStats> batch = new ArrayList<>();
        @Override public void upsert(TeamStats stats) { batch = List.of(stats); }
        @Override public void upsertBatch(List<TeamStats> stats) { batch = List.copyOf(stats); }
    }

    private static final class CapturingSuccess implements PersistedSuccessEvents {
        private List<TeamStats> batch = new ArrayList<>();
        @Override public void publishBatch(List<TeamStats> stats) { batch = List.copyOf(stats); }
    }
}
