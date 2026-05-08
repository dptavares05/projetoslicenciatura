package uevora.tw.roomrent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uevora.tw.roomrent.model.Anuncio;
import uevora.tw.roomrent.model.Utilizador;
import uevora.tw.roomrent.repository.AnuncioRepository;
import uevora.tw.roomrent.repository.UtilizadorRepository;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
public class AdminController {

    @Autowired
    private UtilizadorRepository utilizadorRepo;

    @Autowired
    private AnuncioRepository anuncioRepo;

    //  Dashboard de admin: Mostrar utilizadores e  anúncios pendentes
    @GetMapping("/admin")
    public String PainelDeAdmin(Model model) {
            //verificar quem está a entrar no painel
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            //verificar se quem está a entrar é admin
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));
        
            if (!isAdmin) {return "redirect:/";}  // quem  não é admin não entra na dashboard, volta para o ecrã principal
        
        
        //Mostrar Utilizadores Pendentes
        model.addAttribute("pendentes", utilizadorRepo.findByAprovado(false));
        
        // Mostrar anúncios com estado "INATIVO"
        List<Anuncio> anunciosPendentes = anuncioRepo.findByEstado("INATIVO");//
        model.addAttribute("anunciosPendentes", anunciosPendentes);//
        
        return "admin/dashboard";// retorna a view do dashboard
    }

    // Açãod de Aprovar um utilizador
    @PostMapping("/admin/aprovar")
    public String aprovarUtilizador(@RequestParam Long id) {
        Utilizador u = utilizadorRepo.findById(id).orElse(null);
        if (u != null) {
            u.setAprovado(true);
            utilizadorRepo.save(u);
        }
        return "redirect:/admin";
    }

    // Alterar estado do anúncio (ATIVO / INATIVO)
    @PostMapping("/admin/anuncio/estado")
    public String alterarEstadoAnuncio(@RequestParam Long id, @RequestParam String novoEstado) {
        Anuncio a = anuncioRepo.findById(id).orElse(null);
        if (a != null) {
            a.setEstado(novoEstado);
            anuncioRepo.save(a);
        }
        return "redirect:/admin";
    }
}