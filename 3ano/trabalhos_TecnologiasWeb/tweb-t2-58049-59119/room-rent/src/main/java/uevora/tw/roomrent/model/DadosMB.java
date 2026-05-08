package uevora.tw.roomrent.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true) // Ignora campos extra se houver
public class DadosMB {// classe para mapear os dados do Multibanco recebidos da API de pagamento

    // O site devolve "mb_entity" mas nós renomiamos para "entidade" no nosso modelo
    @JsonProperty("mb_entity")
    private String entidade;

    @JsonProperty("mb_reference")
    private String referencia;

    @JsonProperty("mb_amount")
    private String valor;

    // --- Getters e Setters ---
    public String getEntidade() { return entidade; }
    public void setEntidade(String entidade) { this.entidade = entidade; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
}