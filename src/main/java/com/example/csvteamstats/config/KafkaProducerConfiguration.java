package com.example.csvteamstats.config;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.Properties;
public class KafkaProducerConfiguration {
 @Produces @Singleton KafkaProducer<Object,Object> producer(@ConfigProperty(name="app.kafka.bootstrap") String bootstrap,@ConfigProperty(name="app.kafka.registry") String registry){
 Properties p=new Properties();
 p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrap);
 p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,KafkaAvroSerializer.class);
 p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,KafkaAvroSerializer.class);
 p.put(ProducerConfig.ACKS_CONFIG,"all");
 p.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,true);
 p.put("schema.registry.url",registry);
 return new KafkaProducer<>(p);
 }
}