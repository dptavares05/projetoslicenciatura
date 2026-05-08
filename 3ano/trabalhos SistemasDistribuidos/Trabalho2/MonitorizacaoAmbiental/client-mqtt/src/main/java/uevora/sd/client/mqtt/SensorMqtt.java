package uevora.sd.client.mqtt;

import com.google.gson.JsonObject;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Scanner;

public class SensorMqtt {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String broker = "tcp://localhost:1883";
        String topic = "sensor/leituras";
        
        System.out.println(">>> SENSOR MQTT (IoT Simples) <<<");
        
        // Loop para obrigar a introduzir um ID
        String deviceId = "";
        while (deviceId.isBlank()) {
            System.out.print("Introduza o ID deste sensor: ");
            deviceId = scanner.nextLine().trim();
            if (deviceId.isBlank()) {
                System.out.println("Erro: O ID nao pode ser vazio!");
            }
        }
        
        String clientId = "Client-" + deviceId + "-" + System.currentTimeMillis();
        
        System.out.println(">>> A iniciar como: " + deviceId);
        
        double currentTemp = 18.0; 
        double currentHum = 65.0;
        Random random = new Random();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setAutomaticReconnect(true);

            System.out.println(">>> A ligar ao broker MQTT...");
            sampleClient.connect(connOpts);
            System.out.println(">>> Ligado! A iniciar envio continuo...");

            while (true) {
                currentTemp += (random.nextDouble() - 0.5);
                currentHum += (random.nextDouble() - 0.5) * 2;
                currentTemp = Math.max(15, Math.min(30, currentTemp));
                currentHum = Math.max(30, Math.min(80, currentHum));

                JsonObject json = new JsonObject();
                json.addProperty("deviceId", deviceId);
                json.addProperty("temperature", Math.round(currentTemp * 100.0) / 100.0);
                json.addProperty("humidity", Math.round(currentHum * 100.0) / 100.0);
                json.addProperty("timestamp", LocalDateTime.now().toString());

                String content = json.toString();
                MqttMessage message = new MqttMessage(content.getBytes());
                message.setQos(1);
                
                sampleClient.publish(topic, message);
                System.out.println(">>> [" + deviceId + "] Publicado: " + content);

                Thread.sleep(5000);
            }
            
        } catch (Exception me) {
            System.err.println("ERRO MQTT: " + me.getMessage());
            me.printStackTrace();
        }
    }
}