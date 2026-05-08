package uevora.tw.roomrent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uevora.tw.roomrent.model.Mensagem;
import java.util.List;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    // Encontrar mensagens enviadas POR um utilizador específico
    List<Mensagem> findByRemetenteId(Long id);

    // Encontrar mensagens relativas a um anúncio específico
    List<Mensagem> findByAnuncioId(Long id);

    //  Encontrar mensagens para o SENHORIO/ANUNCIANTE
    //  Queremos mensagens onde o Anuncio associado pertence a este Anunciante
    List<Mensagem> findByAnuncio_Anunciante_Id(Long id);
    
    List<Mensagem> findByDestinatarioIdOrderByDataEnvioDesc(Long destinatarioId);
}