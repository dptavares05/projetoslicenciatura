package uevora.tw.roomrent.controller;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import uevora.tw.roomrent.model.Anuncio;
import uevora.tw.roomrent.model.Utilizador;
import uevora.tw.roomrent.model.Mensagem;
import uevora.tw.roomrent.repository.AnuncioRepository;
import uevora.tw.roomrent.repository.UtilizadorRepository;
import uevora.tw.roomrent.repository.MensagemRepository;

import java.util.List;

@Controller 
public class PublicController {

    @Autowired
    private AnuncioRepository anuncioRepo;
    @Autowired
    private UtilizadorRepository utilizadorRepo;
    @Autowired
    private MensagemRepository mensagemRepo; 
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    
    // --- INDEX ---
    @GetMapping("/")
    public String homepage(Model model) {// método para a página inicial
        List<Anuncio> ofertas = anuncioRepo.findTop3ByTipoAnuncioAndEstadoOrderByIdDesc("OFERTA", "ATIVO");// buscar as 3 últimas ofertas ativas
        List<Anuncio> procuras = anuncioRepo.findTop3ByTipoAnuncioAndEstadoOrderByIdDesc("PROCURA", "ATIVO");// buscar as 3 últimas procuras ativas

        //adicionar ao modelo para a view
        model.addAttribute("listaOfertas", ofertas);
        model.addAttribute("listaProcuras", procuras);
        return "index"; 
    }
    
    // --- PÁGINA DE OFERTAS (COM PAGINAÇÃO) ---
    @GetMapping("/ofertas")
    public String verTodasOfertas(@RequestParam(value = "keyword", required = false) String keyword,
                                  @RequestParam(value = "page", defaultValue = "0") int page, 
                                  Model model) {
        
        // Configura para mostrar 4 itens por página
        Pageable paging = PageRequest.of(page, 4);
        Page<Anuncio> paginaOfertas;
        
        if (keyword != null && !keyword.isEmpty()) {
            paginaOfertas = anuncioRepo.pesquisarOfertas(keyword, paging);
        } else {
            paginaOfertas = anuncioRepo.findByTipoAnuncioAndEstadoOrderByDataRegistoDesc("OFERTA", "ATIVO", paging);
        }
        
        model.addAttribute("listaOfertas", paginaOfertas); // Agora passamos a Página inteira
        return "ofertas"; 
    }

    // --- PÁGINA DE PROCURAS (COM PAGINAÇÃO) ---
    @GetMapping("/procuras")
    public String verTodasProcuras(@RequestParam(value = "keyword", required = false) String keyword,// palavra-chave de pesquisa
                                   @RequestParam(value = "page", defaultValue = "0") int page,// página atual
                                   Model model) {// modelo para enviar dados à view
        
        Pageable paging = PageRequest.of(page, 4);//  página atual com pagnação com 4 itens por página
        Page<Anuncio> paginaProcuras;// página de procuras

        if (keyword != null && !keyword.isEmpty()) {// se escrever-mos alguma coisa na searchbar (keyword)
            paginaProcuras = anuncioRepo.pesquisarProcuras(keyword, paging);// pesquisar procuras com o que escrevemos 
        } else {
            paginaProcuras = anuncioRepo.findByTipoAnuncioAndEstadoOrderByDataRegistoDesc("PROCURA", "ATIVO", paging);
        }
        
        model.addAttribute("listaProcuras", paginaProcuras);// enviar a página de procuras para a view
        return "procuras"; 
    }


    // --- LOGIN E REGISTO ---
    @GetMapping("/login") public String loginPage() { return "login"; }
    @GetMapping("/registar") public String mostrarRegisto() { return "registar"; }
    
    @PostMapping("/registar")
    public String processarRegisto(@RequestParam String email, @RequestParam String nome, @RequestParam String password, @RequestParam String contacto) {
        if (utilizadorRepo.findByEmail(email) != null) return "redirect:/registar?error=email_existe";
       
        // Encriptar a password antes de criar o objeto
        String passwordEncriptada = passwordEncoder.encode(password);
        
        Utilizador novo = new Utilizador(email, nome, passwordEncriptada, contacto, "USER");
        novo.setAprovado(false);
        utilizadorRepo.save(novo);
        return "redirect:/login?registado=true";
    }

    // --- DETALHES DO ANÚNCIO ---
@GetMapping("/anuncio/{id}")
    public String verDetalhesAnuncio(@PathVariable Long id, Model model) {
        
        // encontrar o anúncio pelo ID
        Anuncio anuncio = anuncioRepo.findById(id).orElse(null);

        // se não existe, volta para o início
        if (anuncio == null) {
            return "redirect:/";
        }

        // Se o anúncio estiver INATIVO
        if ("INATIVO".equals(anuncio.getEstado())) {
            
            // verificar quem está a tentar aceder
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // Se for um utilizador não logado, redireciona para o ecrã principal
            if (auth == null || !auth.isAuthenticated()) {
                return "redirect:/"; 
            }

            String emailQuemAcede = auth.getName();
            
            // Verificar se é ADMIN
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));
            
            // Verificar se é o DONO do anúncio
            boolean isDono = anuncio.getAnunciante().getEmail().equals(emailQuemAcede);

            // Se NÃO for Admin E NÃO for Dono, bloqueia o acesso redirecionando para o ecrã principal
            if (!isAdmin && !isDono) {
                return "redirect:/";
            }
        }

        model.addAttribute("anuncio", anuncio);
        return "detalhes-anuncio";
    }

    // --- ENVIAR MENSAGEM AO DONO DO ANÚNCIO ---
    @PostMapping("/anuncio/{id}/enviar-mensagem")
    public String enviarMensagem(@PathVariable Long id, @RequestParam String texto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();// obter autenticação do utilizador
        String email = auth.getName();// obter email do utilizador autenticado através do nome porque é o email que usamos como username
        
        if (email.equals("anonymousUser")) return "redirect:/login";// se não estiver autenticado, redirecionar para login

        Utilizador remetente = utilizadorRepo.findByEmail(email);// buscar o utilizador remetente pelo email
        Anuncio anuncio = anuncioRepo.findById(id).orElse(null);// buscar o anúncio pelo id
        if (anuncio != null && remetente != null) {
            // Se eu estou interessado, a mensagem é para o Dono do Anúncio
            Utilizador dono = anuncio.getAnunciante();
            
            // impedir de enviar mensagens para mim mesmo 
            if (!remetente.getId().equals(dono.getId())) {
                Mensagem novaMensagem = new Mensagem(texto, anuncio, remetente, dono);
                mensagemRepo.save(novaMensagem);
            }
        }

        return "redirect:/anuncio/" + id + "?enviada=true";
    }
    
    
}