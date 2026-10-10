package com.example.csvteamstats.adapters.out.persistence;
import com.example.csvteamstats.application.port.out.TeamStatsStore;
import com.example.csvteamstats.domain.model.TeamStats;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
@ApplicationScoped public class TeamStatsPersistenceAdapter implements TeamStatsStore {
 private static final String UPSERT="""
 INSERT INTO team_stats (match_id,period,category,metric,home_team,home_value,away_team,away_value,source_url,source_event_id)
 VALUES (?,?,?,?,?,?,?,?,?,?)
 ON CONFLICT (match_id,period,category,metric) DO UPDATE SET
 home_team=EXCLUDED.home_team,home_value=EXCLUDED.home_value,away_team=EXCLUDED.away_team,
 away_value=EXCLUDED.away_value,source_url=EXCLUDED.source_url,source_event_id=EXCLUDED.source_event_id,
 updated_at=CURRENT_TIMESTAMP
 """;
 @Inject DataSource dataSource;
 @Override @Transactional public void upsert(TeamStats s){upsertBatch(List.of(s));}
 @Override @Transactional public void upsertBatch(List<TeamStats> stats){
 if(stats==null||stats.isEmpty())return;
 try(Connection connection=dataSource.getConnection();PreparedStatement ps=connection.prepareStatement(UPSERT)){
 for(TeamStats s:stats){
 Object[] values={s.getMatchId(),s.getPeriod(),s.getCategory(),s.getMetric(),s.getHomeTeam(),s.getHomeValue(),s.getAwayTeam(),s.getAwayValue(),s.getSourceUrl(),s.getSourceEventId()};
 for(int i=0;i<values.length;i++)ps.setObject(i+1,values[i]);
 ps.addBatch();
 }
 ps.executeBatch();
 }catch(SQLException e){throw new IllegalStateException("PostgreSQL batch failure",e);}
 }
}