package uevora.sd.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "devices")
public class DeviceEntity {

    @Id
    private String id; // Ex: "sensor-sala-aula-01"

    private String protocol; // "MQTT", "GRPC", "REST"
    private String room;     // "Sala 101"
    private String department; 
    private String floor;    
    private String building; 
    private boolean active;  // Se for false, ignoramos os dados

    public DeviceEntity() {}

    public DeviceEntity(String id, String protocol, String room, String department, String floor, String building, boolean active) {
        this.id = id;
        this.protocol = protocol;
        this.room = room;
        this.department = department;
        this.floor = floor;
        this.building = building;
        this.active = active;
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}