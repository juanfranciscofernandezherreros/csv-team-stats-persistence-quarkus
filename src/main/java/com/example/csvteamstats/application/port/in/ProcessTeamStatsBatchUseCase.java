package com.example.csvteamstats.application.port.in;

import com.example.csvteamstats.domain.model.IncomingTeamStats;
import java.util.List;

public interface ProcessTeamStatsBatchUseCase {
    BatchProcessingResult processBatch(List<IncomingTeamStats> records);

    record BatchProcessingResult(int received, int persisted, int rejected) {}
}
