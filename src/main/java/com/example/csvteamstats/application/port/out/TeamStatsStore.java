package com.example.csvteamstats.application.port.out;

import com.example.csvteamstats.domain.model.TeamStats;
import java.util.List;

public interface TeamStatsStore {
    void upsert(TeamStats stats);
    void upsertBatch(List<TeamStats> stats);
}
