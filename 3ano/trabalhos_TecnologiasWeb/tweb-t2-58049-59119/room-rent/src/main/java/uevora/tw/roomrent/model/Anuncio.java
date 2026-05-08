
package uevora.tw.roomrent.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Anuncio {// classe para anúncios de oferta ou procura de alojamento

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipoAnuncio;     // Oferta ou Procura
    private String tipoAlojamento;  // Quarto, T0, T1...
    private String zona;
    private double preco;
    private String generoAlvo;      // Masculino, Feminino, Indiferente
    
    @Column(length = 1000)
    private String descricao;
    
    @Column(name = "mb_entidade")
    private String mbEntidade;

    @Column(name = "mb_referencia")
    private String mbReferencia;

    @Column(name = "mb_valor")
    private String mbValor;
    
    private LocalDate dataRegisto;
    private String estado;          // INATIVO, AtIVO

    // Dono do anúncio
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "utilizador_id")
    private Utilizador anunciante;

    // Mensagens associadas a este anúncio
    @OneToMany(mappedBy = "anuncio", cascade = CascadeType.ALL)
    private List<Mensagem> mensagens;

    @PrePersist
    protected void aoCriar() {
        if (this.dataRegisto == null) this.dataRegisto = LocalDate.now();
        if (this.estado == null) this.estado = "INATIVO";
    }

    // --- Getters e Setters (Resumido para os campos principais) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoAnuncio() { return tipoAnuncio; }
    public void setTipoAnuncio(String tipoAnuncio) { this.tipoAnuncio = tipoAnuncio; }

    public String getTipoAlojamento() { return tipoAlojamento; }
    public void setTipoAlojamento(String tipoAlojamento) { this.tipoAlojamento = tipoAlojamento; }
    
    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }
    
    public String getGeneroAlvo() { return generoAlvo; }
    public void setGeneroAlvo(String generoAlvo) { this.generoAlvo = generoAlvo; }
    
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDate getDataRegisto() { return dataRegisto; }
    public void setDataRegisto(LocalDate dataRegisto) { this.dataRegisto = dataRegisto; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Utilizador getAnunciante() { return anunciante; }
    public void setAnunciante(Utilizador anunciante) { this.anunciante = anunciante; }

    public List<Mensagem> getMensagens() { return mensagens; }
    public void setMensagens(List<Mensagem> mensagens) { this.mensagens = mensagens; }
    
    public String getMbEntidade() { return mbEntidade; }
    public void setMbEntidade(String mbEntidade) { this.mbEntidade = mbEntidade; }

    public String getMbReferencia() { return mbReferencia; }
    public void setMbReferencia(String mbReferencia) { this.mbReferencia = mbReferencia; }

    public String getMbValor() { return mbValor; }
    public void setMbValor(String mbValor) { this.mbValor = mbValor; }
}