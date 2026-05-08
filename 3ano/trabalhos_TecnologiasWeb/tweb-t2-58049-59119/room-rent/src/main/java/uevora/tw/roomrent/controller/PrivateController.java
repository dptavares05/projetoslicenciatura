package uevora.tw.roomrent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uevora.tw.roomrent.model.Anuncio;
import uevora.tw.roomrent.model.Utilizador;
import uevora.tw.roomrent.repository.AnuncioRepository;
import uevora.tw.roomrent.repository.UtilizadorRepository;
import uevora.tw.roomrent.repository.MensagemRepository;
import uevora.tw.roomrent.service.PagamentoService;
import uevora.tw.roomrent.model.DadosMB;

import java.util.List;
import uevora.tw.roomrent.model.Mensagem;

@Controller
public class PrivateController {// controlador para rotas privadas i.e. que precisam de autenticação

    @Autowired
    private AnuncioRepository anuncioRepo;

    @Autowired
    private UtilizadorRepository utilizadorRepo;
    
    @Autowired
    private PagamentoService pagamentoService;
    
    @Autowired
    private MensagemRepository mensagemRepo;

    // --- CRIAR ANÚNCIO ---
    @GetMapping("/anuncio/novo")
    public String novoAnuncio(Model model) {
        return "privado/criar-anuncio";
    }

    @PostMapping("/anuncio/novo")
        public String gravarAnuncio(
                @RequestParam String tipoAnuncio,
                @RequestParam String tipoAlojamento,
                @RequestParam String zona,
                @RequestParam double preco,
                @RequestParam String generoAlvo,
                @RequestParam String descricao) {

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Utilizador dono = utilizadorRepo.findByEmail(auth.getName());

            Anuncio a = new Anuncio();
            a.setAnunciante(dono);
            a.setTipoAnuncio(tipoAnuncio);
            a.setTipoAlojamento(tipoAlojamento);
            a.setZona(zona);
            a.setPreco(preco);
            a.setGeneroAlvo(generoAlvo);
            a.setDescricao(descricao);

            // se for oferta o anúncio é criado no estado INATIVO e fica à espera de aprovação Admin 
            if (tipoAnuncio.equals("OFERTA")) {
            
                a.setEstado("INATIVO"); 
            
                DadosMB mb = pagamentoService.obterReferenciaExterna(preco);
                a.setMbEntidade(mb.getEntidade());
                a.setMbReferencia(mb.getReferencia());
                a.setMbValor(mb.getValor());
                
            } else { // se não é oferta então é procura 

                // se é procura nós assumimos que não precisa de aprovação logo fica imediatamente ativo após criação 
                a.setEstado("ATIVO"); 
                // dadosMB ficam null em procura
                a.setMbEntidade(null);
                a.setMbReferencia(null);
                a.setMbValor(null);
            }
            
            a.setDataRegisto(java.time.LocalDate.now());

            anuncioRepo.save(a);

            // Redireciona para o detalhe do anúncio para mostrar os dados MB logo após criar inves da pagina de anuncios
            return "redirect:/anuncio/" + a.getId();
        }

    // --- GERIR MEUS ANÚNCIOS ---

    @GetMapping("/meus-anuncios")
    public String meusAnuncios(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Utilizador utilizador = utilizadorRepo.findByEmail(email);
        
        if (utilizador == null) {
            return "redirect:/login"; // Segurança contra erros
        }

        
        // Enviar o NOME do utilizador para a view
        model.addAttribute("nomeUtilizador", utilizador.getNome());

        // Pesquisar usando o ID do utilizador    
        List<Anuncio> meusAnuncios = anuncioRepo.findByAnuncianteId(utilizador.getId());
        
        model.addAttribute("listaAnuncios", meusAnuncios);
        
        return "privado/meus-anuncios";
    }
    
    // --- Mensagens --- 
    // --- LISTAR MENSAGENS ---
    @GetMapping("/mensagens")
    public String asMinhasMensagens(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Utilizador eu = utilizadorRepo.findByEmail(auth.getName());

        // procurar mensagens onde eu sou o destinatário
        List<Mensagem> mensagensRecebidas = mensagemRepo.findByDestinatarioIdOrderByDataEnvioDesc(eu.getId());

        // enviar para a view do modelo
        model.addAttribute("listaMensagens", mensagensRecebidas);// lista de mensagens recebidas
        model.addAttribute("nomeUtilizador", eu.getNome());// enviar o nome do utilizador para a view

        return "privado/mensagens";
    }


    // --- RESPONDER A MENSAGEM ---
    @PostMapping("/mensagens/responder")
    public String responderMensagem(@RequestParam Long mensagemOriginalId, @RequestParam String textoResposta) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Utilizador eu = utilizadorRepo.findByEmail(auth.getName());

        // Vamos buscar a mensagem a que estamos a responder para saber quem a enviou
        Mensagem msgOriginal = mensagemRepo.findById(mensagemOriginalId).orElse(null);

        if (msgOriginal != null && eu != null) {
            // A minha resposta é:
            // De: MIM (eu)
            // Para: O REMETENTE da mensagem original (ex: Maria)
            
            Mensagem resposta = new Mensagem();// nova mensagem de resposta
            resposta.setRemetente(eu);// eu sou o remetente
            resposta.setDestinatario(msgOriginal.getRemetente()); // o destinatário é quem enviou a mensagem original
            resposta.setAnuncio(msgOriginal.getAnuncio());// associar ao mesmo anúncio da mensagem original 
            resposta.setTexto(textoResposta);// texto da resposta
            resposta.setDataEnvio(java.time.LocalDateTime.now());// data de envio é a data atual
            
            mensagemRepo.save(resposta);// guardar a resposta na base de dados
        }

        return "redirect:/mensagens?sucesso=true";
    }
}