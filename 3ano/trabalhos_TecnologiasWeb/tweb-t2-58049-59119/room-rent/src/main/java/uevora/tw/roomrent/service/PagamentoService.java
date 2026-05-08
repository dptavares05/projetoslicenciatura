package uevora.tw.roomrent.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uevora.tw.roomrent.model.DadosMB;
import java.util.Locale;

@Service
public class PagamentoService {

    private static final String API_URL = "https://magno.di.uevora.pt/tweb/t2/mbref4payment";

    public DadosMB obterReferenciaExterna(double valorDoAnuncio) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Configurar cabeçalho (dizer que é um formulário)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Preparar os dados (amount=100.00) - Usa Locale.US para garantir o ponto "."
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("amount", String.format(Locale.US, "%.2f", valorDoAnuncio));

            // Juntar cabeçalho e dados
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            // Enviar pedido POST e receber a resposta
            return restTemplate.postForObject(API_URL, request, DadosMB.class);

        } catch (Exception e) {
            System.out.println("Erro ao contactar API MB: " + e.getMessage());
            // Se falhar, retorna dados de erro
            DadosMB erro = new DadosMB();
            erro.setEntidade("00000");
            erro.setReferencia("Erro de Ligação");
            erro.setValor("0.00");
            return erro;
        }
    }
}