package uevora.tw.roomrent.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Mensagem {// classe para mensagens entre utilizadores sobre anúncios

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    private LocalDateTime dataEnvio;

    @ManyToOne 
    @JoinColumn(name = "anuncio_id")
    private Anuncio anuncio;

    @ManyToOne
    @JoinColumn(name = "remetente_id")
    private Utilizador remetente; // Quem escreve


    @ManyToOne
    @JoinColumn(name = "destinatario_id")
    private Utilizador destinatario; // Quem recebe

    public Mensagem() {
        this.dataEnvio = LocalDateTime.now();
    }

    public Mensagem(String texto, Anuncio anuncio, Utilizador remetente, Utilizador destinatario) {
        this.texto = texto;
        this.anuncio = anuncio;
        this.remetente = remetente;
        this.destinatario = destinatario;
        this.dataEnvio = LocalDateTime.now();
    }

    // --- Getters e Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }

    public Anuncio getAnuncio() { return anuncio; }
    public void setAnuncio(Anuncio anuncio) { this.anuncio = anuncio; }

    public Utilizador getRemetente() { return remetente; }
    public void setRemetente(Utilizador remetente) { this.remetente = remetente; }

    public Utilizador getDestinatario() { return destinatario; }
    public void setDestinatario(Utilizador destinatario) { this.destinatario = destinatario; }
}