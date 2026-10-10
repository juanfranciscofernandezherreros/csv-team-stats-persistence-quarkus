package com.example.csvteamstats.application.port.in;

import com.example.csvteamstats.domain.model.TeamStats;
import java.util.List;

public interface PersistTeamStatsUseCase {
    void persist(TeamStats stats);
    void persistBatch(List<TeamStats> stats);
}
