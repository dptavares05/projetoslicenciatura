package uevora.sd.client.rest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Scanner;

public class SensorRest {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println(">>> SENSOR REST (HTTP) <<<");
        
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

        double currentTemp = 25.0;
        double currentHum = 40.0;
        Random random = new Random();

        HttpClient client = HttpClient.newHttpClient();

        while (true) {
            try {
                currentTemp += (random.nextDouble() - 0.5);
                currentHum += (random.nextDouble() - 0.5) * 2;
                currentTemp = Math.max(15, Math.min(30, currentTemp));
                currentHum = Math.max(30, Math.min(80, currentHum));

                // Usar deviceId dinamico no JSON
                String json = String.format(java.util.Locale.US, """
                    {
                        "deviceId": "%s",
                        "temperature": %.2f,
                        "humidity": %.2f,
                        "timestamp": "%s"
                    }
                """, deviceId, currentTemp, currentHum, LocalDateTime.now().toString());

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/metrics/ingest"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println(">>> [" + deviceId + "] Enviado (Status " + response.statusCode() + "): T=" + String.format("%.2f", currentTemp));
                
                Thread.sleep(5000);

            } catch (Exception e) {
                System.err.println("Erro: " + e.getMessage());
                try { Thread.sleep(5000); } catch (InterruptedException i) {}
            }
        }
    }
}