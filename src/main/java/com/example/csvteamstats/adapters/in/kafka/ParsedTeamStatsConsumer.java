package com.example.csvteamstats.adapters.in.kafka;
import com.example.csvteamstats.application.port.in.ProcessTeamStatsBatchUseCase;
import com.example.csvteamstats.domain.model.*;
import com.example.csvteamstats.avro.TeamStatsValue;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.ShutdownEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.util.*;
@ApplicationScoped public class ParsedTeamStatsConsumer {
 private static final Logger log=Logger.getLogger(ParsedTeamStatsConsumer.class);
 @Inject ProcessTeamStatsBatchUseCase service;
 @Inject AvroTeamStatsMapper mapper;
 @ConfigProperty(name="app.kafka.bootstrap") String bootstrap;
 @ConfigProperty(name="app.kafka.registry") String registry;
 @ConfigProperty(name="app.kafka.group") String group;
 @ConfigProperty(name="app.kafka.input") String input;
 @ConfigProperty(name="app.kafka.dlt") String dlt;
 @ConfigProperty(name="app.kafka.max-poll") int maxPoll;
 @ConfigProperty(name="app.kafka.attempts") int attempts;
 @ConfigProperty(name="app.kafka.backoff") long backoff;
 private volatile boolean running;
 private Thread worker;
 void start(@Observes StartupEvent event){running=true;worker=new Thread(this::consume,"team-stats-consumer");worker.start();}
 void stop(@Observes ShutdownEvent event){running=false;if(worker!=null){worker.interrupt();try{worker.join(10000);}catch(InterruptedException e){Thread.currentThread().interrupt();}}}
 void consume(){
 Properties p=new Properties();p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrap);p.put(ConsumerConfig.GROUP_ID_CONFIG,group);
 p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,ByteArrayDeserializer.class);p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,ByteArrayDeserializer.class);
 p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");p.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,maxPoll);
 Properties dp=new Properties();dp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrap);dp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,ByteArraySerializer.class);dp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,ByteArraySerializer.class);dp.put(ProducerConfig.ACKS_CONFIG,"all");
 Map<String,Object> cfg=Map.of("schema.registry.url",registry,"specific.avro.reader",true);
 try(KafkaConsumer<byte[],byte[]> c=new KafkaConsumer<>(p);KafkaProducer<byte[],byte[]> dead=new KafkaProducer<>(dp);KafkaAvroDeserializer decoder=new KafkaAvroDeserializer()){
 decoder.configure(cfg,false);
 c.subscribe(List.of(input));
 while(running){
 ConsumerRecords<byte[],byte[]> records=c.poll(Duration.ofMillis(1000));
 if(records.isEmpty())continue;
 boolean done=false;
 for(int attempt=1;attempt<=Math.max(1,attempts)&&running;attempt++){
 try{
 List<IncomingTeamStats> batch=new ArrayList<>();
 for(var record:records){
 try{
 TeamStatsValue val=(TeamStatsValue)decoder.deserialize(record.topic(),record.headers(),record.value());
 if(val!=null)batch.add(new IncomingTeamStats(mapper.toDomain(val),new MessageOrigin(record.topic(),record.partition(),record.offset())));
 }catch(org.apache.kafka.common.errors.SerializationException|ClassCastException ex){toDlt(dead,record,"deserialize");}
 }
 service.processBatch(batch);
 c.commitSync();done=true;break;
 }catch(Exception e){
 log.errorf(e,"event=batch_failure attempt=%d",attempt);
 if(attempt<attempts){try{Thread.sleep(backoff);}catch(InterruptedException x){Thread.currentThread().interrupt();break;}}
 }
 }
 if(!done&&running){
 try{for(var record:records)toDlt(dead,record,"processing");c.commitSync();}
 catch(Exception e){log.error("DLT publication failed; offsets uncommitted",e);running=false;}
 }
 }
 }catch(Exception e){if(running)log.error("Consumer stopped",e);}
 }
 private void toDlt(KafkaProducer<byte[],byte[]> dead,ConsumerRecord<byte[],byte[]> r,String stage)throws Exception{
 ProducerRecord<byte[],byte[]> target=new ProducerRecord<>(dlt,r.key(),r.value());
 target.headers().add("original-topic",r.topic().getBytes(StandardCharsets.UTF_8));
 target.headers().add("original-partition",Integer.toString(r.partition()).getBytes(StandardCharsets.UTF_8));
 target.headers().add("original-offset",Long.toString(r.offset()).getBytes(StandardCharsets.UTF_8));
 target.headers().add("failure-stage",stage.getBytes(StandardCharsets.UTF_8));
 dead.send(target).get();
 }
}