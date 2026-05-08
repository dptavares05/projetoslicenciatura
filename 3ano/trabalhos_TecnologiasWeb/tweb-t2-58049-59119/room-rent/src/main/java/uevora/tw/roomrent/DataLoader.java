package uevora.tw.roomrent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import uevora.tw.roomrent.model.Anuncio;
import uevora.tw.roomrent.model.Utilizador;
import uevora.tw.roomrent.repository.AnuncioRepository;
import uevora.tw.roomrent.repository.UtilizadorRepository;

import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UtilizadorRepository utilizadorRepo;

    @Autowired
    private AnuncioRepository anuncioRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        if (utilizadorRepo.count() == 0) { // Só carrega os dados se a tabela de utilizadores estiver vazia
            System.out.println("--- A LIMPAR DADOS ANTIGOS ---");
            // 1. Apagar dados antigos para garantir que as passwords ficam encriptadas
            anuncioRepo.deleteAll();
            utilizadorRepo.deleteAll(); 

            System.out.println("--- A INICIAR DATA LOADER ---");

            // 2. Criar Utilizador ADMIN
            // Login: admin@roomrent.pt | Pass: admin
            Utilizador admin = new Utilizador("admin@roomrent.pt", "Administrador", 
                    passwordEncoder.encode("admin"), 
                    "000000000", "ADMIN");
            admin.setAprovado(true);
            utilizadorRepo.save(admin);

            // 3. Criar Utilizador João Senhorio
            // Login: joao@mail.pt | Pass: 12345
            Utilizador senhorio = new Utilizador("joao@mail.pt", "João Senhorio", 
                    passwordEncoder.encode("12345"), 
                    "910000001", "USER");
            senhorio.setAprovado(true);
            utilizadorRepo.save(senhorio);

            // 4. Criar Utilizador Maria Estudante
            // Login: maria@mail.pt | Pass: abcde
            Utilizador estudante = new Utilizador("maria@mail.pt", "Maria Estudante", 
                    passwordEncoder.encode("abcde"), 
                    "960000002", "USER");
            estudante.setAprovado(true);
            utilizadorRepo.save(estudante);

            // 5. Criar Utilizador Pendente (Teste) 
            // Login: teste@mail.pt | Pass: 12345
            Utilizador pendente = new Utilizador("teste@mail.pt", "Utilizador Pendente", 
                    passwordEncoder.encode("12345"), 
                    "960000003", "USER");
                    pendente.setAprovado(false); // <--- APROVADO = FALSE
                    utilizadorRepo.save(pendente);

            // 6. Criar Ofertas
            criarAnuncio(senhorio, "OFERTA", "Quarto", "Évora (Centro)", 250.0, "Indiferente", "Quarto com muita luz na praça Giraldo.", "ATIVO");
            criarAnuncio(senhorio, "OFERTA", "T1", "Évora (Malagueira)", 400.0, "Casal", "T1 mobilado com AC.", "ATIVO");

            // 7. Criar Procuras
            criarAnuncio(estudante, "PROCURA", "Quarto", "Évora (Horta das Figueiras)", 220.0, "Feminino", "Procuro quarto em casa só de raparigas.", "ATIVO");

            System.out.println("--- DADOS CARREGADOS COM SUCESSO ---");
        }
    }
    
    private void criarAnuncio(Utilizador u, String tipo, String alojamento, String zona, double preco, String genero, String desc, String estado) {
        Anuncio a = new Anuncio();
        a.setAnunciante(u);
        a.setTipoAnuncio(tipo);
        a.setTipoAlojamento(alojamento);
        a.setZona(zona);
        a.setPreco(preco);
        a.setGeneroAlvo(genero);
        a.setDescricao(desc);
        a.setEstado(estado); 
        a.setDataRegisto(LocalDate.now());
        
        anuncioRepo.save(a);
    }
}