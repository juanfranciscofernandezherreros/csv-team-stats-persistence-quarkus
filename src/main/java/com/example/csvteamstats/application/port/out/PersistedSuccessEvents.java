package com.example.csvteamstats.application.port.out;

import com.example.csvteamstats.domain.model.TeamStats;
import java.util.List;

public interface PersistedSuccessEvents {
    void publishBatch(List<TeamStats> stats);
}
