package uevora.sd.server.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "metrics") // Nome da tabela na BD
public class MetricEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceId;
    private Double temperature;
    private Double humidity;
    private LocalDateTime timestamp;

    // Construtores
    public MetricEntity() {}

    public MetricEntity(String deviceId, Double temperature, Double humidity, String timestamp) {
        this.deviceId = deviceId;
        this.temperature = temperature;
        this.humidity = humidity;
        // Converte a String de data para objeto Data real
        this.timestamp = LocalDateTime.parse(timestamp);
    }

    // Getters e Setters (Podes gerar com Alt+Insert, mas aqui ficam eles)
    public Long getId() { return id; }
    public String getDeviceId() { return deviceId; }
    public Double getTemperature() { return temperature; }
    public Double getHumidity() { return humidity; }
    public LocalDateTime getTimestamp() { return timestamp; }
}