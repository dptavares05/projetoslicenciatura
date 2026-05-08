package uevora.sd.server.mqtt;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import uevora.sd.server.model.MetricEntity;
import uevora.sd.server.service.MetricsProcessor;

@Configuration
public class MqttConfig {

    @Autowired
    private MetricsProcessor processor;

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter("tcp://localhost:1883", "serverClient", "sensor/leituras");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return message -> {
            try {
                String payload = (String) message.getPayload();
                System.out.println("=== MQTT RECEBIDO: " + payload);

                Gson gson = new Gson();
                JsonObject json = gson.fromJson(payload, JsonObject.class);

                MetricEntity entity = new MetricEntity(
                        json.get("deviceId").getAsString(),
                        json.get("temperature").getAsDouble(),
                        json.get("humidity").getAsDouble(),
                        json.get("timestamp").getAsString()
                );

                // Chama o processador para validar antes de gravar
                processor.processMetric(entity);

            } catch (Exception e) {
                System.err.println("Erro MQTT: " + e.getMessage());
            }
        };
    }
}