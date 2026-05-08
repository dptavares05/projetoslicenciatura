package uevora.sd.server.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uevora.sd.server.model.DeviceEntity;
import uevora.sd.server.repository.DeviceRepository;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private DeviceRepository repository;

    // 1. CRIAR / REGISTAR DISPOSITIVO
    @PostMapping
    public ResponseEntity<DeviceEntity> createDevice(@RequestBody DeviceEntity device) {
        // Se já existir, atualiza. Se não, cria.
        DeviceEntity saved = repository.save(device);
        return ResponseEntity.ok(saved);
    }

    // 2. LISTAR TODOS
    @GetMapping
    public List<DeviceEntity> getAllDevices() {
        return repository.findAll();
    }

    // 3. OBTER UM DISPOSITIVO (Por ID)
    @GetMapping("/{id}")
    public ResponseEntity<DeviceEntity> getDevice(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. ATUALIZAR (Mudar sala, ativar/desativar)
    @PutMapping("/{id}")
    public ResponseEntity<DeviceEntity> updateDevice(@PathVariable String id, @RequestBody DeviceEntity updates) {
        return repository.findById(id)
                .map(device -> {
                    device.setProtocol(updates.getProtocol());
                    device.setRoom(updates.getRoom());
                    device.setDepartment(updates.getDepartment());
                    device.setFloor(updates.getFloor());
                    device.setBuilding(updates.getBuilding());
                    device.setActive(updates.isActive()); // Importante para ativar/desativar sensores
                    return ResponseEntity.ok(repository.save(device));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 5. ELIMINAR
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable String id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}