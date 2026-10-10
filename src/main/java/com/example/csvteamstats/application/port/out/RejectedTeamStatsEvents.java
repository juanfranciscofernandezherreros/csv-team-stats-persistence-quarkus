package com.example.csvteamstats.application.port.out;

import com.example.csvteamstats.domain.model.TeamStatsRejection;

public interface RejectedTeamStatsEvents {
    void publish(TeamStatsRejection rejection);
}
