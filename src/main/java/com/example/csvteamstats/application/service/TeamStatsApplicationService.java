package com.example.csvteamstats.application.service;

import com.example.csvteamstats.application.port.in.PersistTeamStatsUseCase;
import com.example.csvteamstats.application.port.in.ProcessTeamStatsBatchUseCase;
import com.example.csvteamstats.application.port.in.ProcessTeamStatsBatchUseCase.BatchProcessingResult;
import com.example.csvteamstats.application.port.out.PersistedSuccessEvents;
import com.example.csvteamstats.application.port.out.RejectedTeamStatsEvents;
import com.example.csvteamstats.application.port.out.TeamStatsStore;
import com.example.csvteamstats.domain.model.IncomingTeamStats;
import com.example.csvteamstats.domain.model.TeamStats;
import com.example.csvteamstats.domain.model.TeamStatsRejection;
import com.example.csvteamstats.domain.validation.TeamStatsFunctionalValidator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Implements the use cases and depends only on application ports and domain types. */
public class TeamStatsApplicationService implements PersistTeamStatsUseCase, ProcessTeamStatsBatchUseCase {
    private final TeamStatsStore store;
    private final PersistedSuccessEvents successEvents;
    private final RejectedTeamStatsEvents rejectedEvents;
    private final TeamStatsFunctionalValidator validator;

    public TeamStatsApplicationService(
            TeamStatsStore store,
            PersistedSuccessEvents successEvents,
            RejectedTeamStatsEvents rejectedEvents,
            TeamStatsFunctionalValidator validator) {
        this.store = store;
        this.successEvents = successEvents;
        this.rejectedEvents = rejectedEvents;
        this.validator = validator;
    }

    @Override
    public void persist(TeamStats stats) {
        if (stats == null) {
            throw new IllegalArgumentException("TeamStats must not be null");
        }
        store.upsert(stats);
    }

    @Override
    public void persistBatch(List<TeamStats> stats) {
        if (stats == null || stats.isEmpty()) {
            return;
        }
        store.upsertBatch(stats);
    }

    @Override
    public BatchProcessingResult processBatch(List<IncomingTeamStats> records) {
        if (records == null || records.isEmpty()) {
            return new BatchProcessingResult(0, 0, 0);
        }
        List<TeamStats> valid = new ArrayList<>();
        List<TeamStatsRejection> rejected = new ArrayList<>();
        for (IncomingTeamStats record : records) {
            var failure = validator.validate(record.stats());
            if (failure.isPresent()) {
                rejected.add(new TeamStatsRejection(record.stats(), failure.get(), record.origin()));
            } else {
                valid.add(record.stats());
            }
        }
        List<TeamStats> distinct = distinctByNaturalKey(valid);
        persistBatch(distinct);
        successEvents.publishBatch(distinct);
        rejected.forEach(rejectedEvents::publish);
        return new BatchProcessingResult(records.size(), distinct.size(), rejected.size());
    }

    /** PostgreSQL cannot upsert the same unique key twice in one rewritten JDBC batch. */
    private List<TeamStats> distinctByNaturalKey(List<TeamStats> stats) {
        Map<NaturalKey, TeamStats> distinct = new LinkedHashMap<>();
        for (TeamStats stat : stats) {
            distinct.put(new NaturalKey(stat.getMatchId(), stat.getPeriod(), stat.getCategory(), stat.getMetric()), stat);
        }
        return new ArrayList<>(distinct.values());
    }

    private record NaturalKey(String matchId, String period, String category, String metric) {}
}
