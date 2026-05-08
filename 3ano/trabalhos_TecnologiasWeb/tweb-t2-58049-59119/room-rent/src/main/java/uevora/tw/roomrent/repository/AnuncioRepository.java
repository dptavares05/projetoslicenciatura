 package uevora.tw.roomrent.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uevora.tw.roomrent.model.Anuncio;
import uevora.tw.roomrent.model.Utilizador;
import java.util.List;

public interface AnuncioRepository extends JpaRepository<Anuncio, Long> {

    // --- MÉTODOS LISTA (Index e Admin) ---
    List<Anuncio> findTop3ByTipoAnuncioAndEstadoOrderByIdDesc(String tipoAnuncio, String estado);
    
    // Este é o método que o Admin usa agora (procura INATIVO)
    List<Anuncio> findByEstado(String estado);
    
    List<Anuncio> findByAnunciante(Utilizador anunciante);
    List<Anuncio> findByAnuncianteId(Long anuncianteId);

    // --- MÉTODOS PAGINAÇÃO ---
    Page<Anuncio> findByTipoAnuncioAndEstadoOrderByDataRegistoDesc(String tipoAnuncio, String estado, Pageable pageable);

    // --- PESQUISA DE PROCURAS ---
    @Query("SELECT a FROM Anuncio a WHERE a.tipoAnuncio = 'PROCURA' AND a.estado = 'ATIVO' AND " +
           "(LOWER(a.zona) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.tipoAlojamento) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.anunciante.nome) LIKE LOWER(CONCAT('%', :keyword, '%')))") 
    Page<Anuncio> pesquisarProcuras(@Param("keyword") String keyword, Pageable pageable);// método para pesquisar procuras com base na keyword que metemos na search bar das procuras

    // --- PESQUISA DE OFERTAS  ---
    @Query("SELECT a FROM Anuncio a WHERE a.tipoAnuncio = 'OFERTA' AND a.estado = 'ATIVO' AND " +
           "(LOWER(a.zona) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.tipoAlojamento) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.anunciante.nome) LIKE LOWER(CONCAT('%', :keyword, '%')))") 
    Page<Anuncio> pesquisarOfertas(@Param("keyword") String keyword, Pageable pageable);// método para pesquisar ofertas com base na keyword que metemos na search bar das ofertas

}