package uevora.sd.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.uevora.sd.grpc.MetricRequest;
import pt.uevora.sd.grpc.MetricResponse;
import pt.uevora.sd.grpc.MetricsServiceGrpc;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Scanner;

public class SensorGrpc {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println(">>> SENSOR gRPC (Alta Performance) <<<");
        
        // Loop para obrigar a introduzir um ID
        String deviceId = "";
        while (deviceId.isBlank()) {
            System.out.print("Introduza o ID deste sensor: ");
            deviceId = scanner.nextLine().trim();
            if (deviceId.isBlank()) {
                System.out.println("Erro: O ID nao pode ser vazio!");
            }
        }
        
        System.out.println(">>> A iniciar como: " + deviceId);
        
        double currentTemp = 22.0; 
        double currentHum = 50.0;  
        Random random = new Random();

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        try {
            MetricsServiceGrpc.MetricsServiceBlockingStub stub = MetricsServiceGrpc.newBlockingStub(channel);

            while (true) {
                // Variacao Gradual
                double tempChange = (random.nextDouble() - 0.5); 
                double humChange = (random.nextDouble() - 0.5) * 2; 

                currentTemp += tempChange;
                currentHum += humChange;
                
                // Intervalos de Temp 15 a 30 e Hum 30% a 80%
                if (currentTemp < 15) currentTemp = 15;
                if (currentTemp > 30) currentTemp = 30;
                if (currentHum < 30) currentHum = 30;
                if (currentHum > 80) currentHum = 80;

                System.out.printf(">>> [%s] A enviar: Temp %.2f | Hum %.2f%n", deviceId, currentTemp, currentHum);
                
                MetricRequest request = MetricRequest.newBuilder()
                        .setDeviceId(deviceId)
                        .setTemperature(currentTemp)
                        .setHumidity(currentHum)
                        .setTimestamp(LocalDateTime.now().toString())
                        .build();

                try {
                    MetricResponse response = stub.submitMetric(request);
                    System.out.println("   [Servidor]: " + response.getMessage());
                } catch (Exception e) {
                    System.err.println("   [Erro]: Falha ao enviar (Servidor offline?)");
                }

                Thread.sleep(5000);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}