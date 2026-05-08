package uevora.tw.roomrent.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
public class Utilizador implements UserDetails {// classe para utilizadores do sistema e implementamos UserDetails para Spring Security para poder usar autenticação com Spring Security

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;
    private String nome;
    private String password;
    private String contacto;
    
    //ADMIN ou USER
    private String perfil;
    
    // por default , como ainda não foi aprovado pelo admin, começa a false
    private boolean aprovado = false;

    @OneToMany(mappedBy = "anunciante", cascade = CascadeType.ALL)
    private List<Anuncio> anuncios;

    @OneToMany(mappedBy = "remetente", cascade = CascadeType.ALL)
    private List<Mensagem> mensagensEnviadas;

    // --- Construtores ---
    public Utilizador() {}

    public Utilizador(String email, String nome, String password, String contacto, String perfil) {
        this.email = email;
        this.nome = nome;
        this.password = password;
        this.contacto = contacto;
        this.perfil = perfil;
    }

    // --- SUBSTITUIR MÉTODOS DEFAULT DO SPRING SECURITY ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Converte o "ADMIN" ou "USER" para permissões do Spring
        if (this.perfil == null) return Collections.emptyList();
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.perfil));// retorna a role do utilizador com prefixo "ROLE_"
    }

    @Override
    public String getUsername() {
        return this.email; // na nossa implementação o "username" usado no login é o email
    }

    @Override
    public boolean isEnabled() {
        return this.aprovado; // Só permite login se o utilizador estiver aprovado
    }

    // Estes 3 métodos indicam se a conta expirou ou está bloqueada.
    // Para este projeto, retornamos sempre 'true' (conta válida).
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }


    // --- Getters e Setters Normais ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }

    public boolean isAprovado() { return aprovado; }
    public void setAprovado(boolean aprovado) { this.aprovado = aprovado; }

    public List<Anuncio> getAnuncios() { return anuncios; }
    public void setAnuncios(List<Anuncio> anuncios) { this.anuncios = anuncios; }

    public List<Mensagem> getMensagensEnviadas() { return mensagensEnviadas; }
    public void setMensagensEnviadas(List<Mensagem> mensagensEnviadas) { this.mensagensEnviadas = mensagensEnviadas; }
}