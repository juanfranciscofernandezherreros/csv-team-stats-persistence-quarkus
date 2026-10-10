package com.example.csvteamstats.adapters.out.kafka;
import com.example.csvteamstats.application.port.out.RejectedTeamStatsEvents;
import com.example.csvteamstats.domain.model.*;
import com.fernandez.basketball.teamstats.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.apache.kafka.clients.producer.*;
import java.time.Instant;
@ApplicationScoped public class TeamStatsRejectedPublisher implements RejectedTeamStatsEvents {
 @Inject KafkaProducer<Object,Object> producer;
 @ConfigProperty(name="app.kafka.rejected") String topic;
 @Override public void publish(TeamStatsRejection r){
 TeamStats s=r.stats();MessageOrigin o=r.origin();
 var key=TeamStatsRejectedKey.newBuilder().setSourceEventId(n(s.getSourceEventId())).setMatchId(n(s.getMatchId())).build();
 var value=TeamStatsRejectedValue.newBuilder().setSourceEventId(n(s.getSourceEventId())).setMatchId(n(s.getMatchId())).setPeriod(n(s.getPeriod())).setCategory(n(s.getCategory())).setMetric(n(s.getMetric())).setReasonCode(r.failure().reasonCode()).setReasonDetail(r.failure().reasonDetail()).setOriginalTopic(o.topic()).setOriginalPartition(o.partition()).setOriginalOffset(o.offset()).setRejectedAtEpochMillis(Instant.now().toEpochMilli()).setContractVersion("1.6.0").build();
 try{producer.send(new ProducerRecord<>(topic,key,value)).get();}
 catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}
 catch(java.util.concurrent.ExecutionException e){throw new IllegalStateException(e);}
 }
 private String n(String s){return s==null?"":s;}
}