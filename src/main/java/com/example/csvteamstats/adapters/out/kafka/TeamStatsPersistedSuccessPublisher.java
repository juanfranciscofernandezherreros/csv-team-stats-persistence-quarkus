package com.example.csvteamstats.adapters.out.kafka;
import com.example.csvteamstats.application.port.out.PersistedSuccessEvents;
import com.example.csvteamstats.domain.model.TeamStats;
import com.fernandez.basketball.persistence.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.apache.kafka.clients.producer.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
@ApplicationScoped public class TeamStatsPersistedSuccessPublisher implements PersistedSuccessEvents {
 @Inject KafkaProducer<Object,Object> producer;
 @ConfigProperty(name="app.kafka.success") String topic;
 @Override public void publishBatch(List<TeamStats> stats){
 if(stats==null||stats.isEmpty())return;
 Map<String,Long> counts=stats.stream().map(TeamStats::getSourceEventId).filter(x->x!=null&&!x.isBlank()).collect(Collectors.groupingBy(x->x,LinkedHashMap::new,Collectors.counting()));
 counts.forEach((id,count)->{
 var key=PersistedEventKey.newBuilder().setSourceEventId(id).build();
 var value=PersistedEventValue.newBuilder().setSourceEventId(id).setImportId(null).setDatasetType("TEAM_STATS").setPersistedAtEpochMillis(Instant.now().toEpochMilli()).setPersistedRecords(count).setContractVersion("1.6.0").build();
 try{producer.send(new ProducerRecord<>(topic,key,value)).get();}
 catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}
 catch(java.util.concurrent.ExecutionException e){throw new IllegalStateException(e);}
 });}
}