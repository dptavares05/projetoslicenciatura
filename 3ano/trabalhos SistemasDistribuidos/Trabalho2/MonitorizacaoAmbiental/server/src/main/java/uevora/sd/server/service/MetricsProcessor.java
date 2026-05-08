package uevora.sd.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uevora.sd.server.model.DeviceEntity;
import uevora.sd.server.model.MetricEntity;
import uevora.sd.server.repository.DeviceRepository;
import uevora.sd.server.repository.MetricRepository;

import java.util.Optional;

@Service
public class MetricsProcessor {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MetricRepository metricRepository;

    public boolean processMetric(MetricEntity metric) {
        // 1. Validar se o dispositivo existe na tabela 'devices'
        Optional<DeviceEntity> deviceOpt = deviceRepository.findById(metric.getDeviceId());

        if (deviceOpt.isEmpty()) {
            System.out.println(">>> ALERTA: Dados rejeitados! Dispositivo desconhecido: " + metric.getDeviceId());
            return false;
        }

        DeviceEntity device = deviceOpt.get();

        // 2. Validar se o dispositivo está ativo
        if (!device.isActive()) {
            System.out.println(">>> ALERTA: Dados rejeitados! Dispositivo inativo: " + metric.getDeviceId());
            return false;
        }

        // 3. Tudo OK -> Gravar
        metricRepository.save(metric);
        System.out.println(">>> SUCESSO: Dados validados e gravados (" + metric.getDeviceId() + ")");
        return true;
    }
}