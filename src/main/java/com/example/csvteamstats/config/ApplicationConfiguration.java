package com.example.csvteamstats.config;
import com.example.csvteamstats.application.service.TeamStatsApplicationService;
import com.example.csvteamstats.application.port.out.*;
import com.example.csvteamstats.domain.validation.TeamStatsFunctionalValidator;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
public class ApplicationConfiguration {
 @Produces @Singleton TeamStatsFunctionalValidator validator(){return new TeamStatsFunctionalValidator();}
 @Produces @Singleton TeamStatsApplicationService service(TeamStatsStore store,PersistedSuccessEvents successes,RejectedTeamStatsEvents rejected,TeamStatsFunctionalValidator validator){
 return new TeamStatsApplicationService(store,successes,rejected,validator);}
}