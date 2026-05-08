package uevora.sd.server.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import pt.uevora.sd.grpc.MetricRequest;
import pt.uevora.sd.grpc.MetricResponse;
import pt.uevora.sd.grpc.MetricsServiceGrpc;
import uevora.sd.server.model.MetricEntity;
import uevora.sd.server.service.MetricsProcessor;

@GrpcService
public class MetricsGrpcService extends MetricsServiceGrpc.MetricsServiceImplBase {

    @Autowired
    private MetricsProcessor processor; // Usamos o processador em vez do repositório direto

    @Override
    public void submitMetric(MetricRequest request, StreamObserver<MetricResponse> responseObserver) {
        // 1. Converter para a nossa Entidade
        MetricEntity entity = new MetricEntity(
                request.getDeviceId(),
                request.getTemperature(),
                request.getHumidity(),
                request.getTimestamp()
        );

        // 2. Tentar processar (Validar e Gravar)
        boolean sucesso = processor.processMetric(entity);

        // 3. Preparar a resposta
        String mensagemResposta;
        if (sucesso) {
            mensagemResposta = "Dados aceites e gravados.";
        } else {
            mensagemResposta = "ERRO: Dispositivo não registado ou inativo.";
        }

        MetricResponse response = MetricResponse.newBuilder()
                .setMessage(mensagemResposta)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}