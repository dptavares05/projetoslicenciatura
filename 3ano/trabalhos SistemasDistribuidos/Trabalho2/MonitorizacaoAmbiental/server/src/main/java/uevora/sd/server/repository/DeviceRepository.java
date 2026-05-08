package uevora.sd.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uevora.sd.server.model.DeviceEntity;
import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, String> {
    // O Spring cria o SQL disto automaticamente só pelo nome do método!
    List<DeviceEntity> findByRoom(String room);
    List<DeviceEntity> findByFloor(String floor);
    List<DeviceEntity> findByDepartment(String department);
    List<DeviceEntity> findByBuilding(String building);
}