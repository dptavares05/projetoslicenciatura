
package uevora.tw.roomrent.repository;
import java.util.*;

import uevora.tw.roomrent.model.Utilizador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilizadorRepository extends JpaRepository<Utilizador, Long> {// Repositório para a entidade Utilizador

    Utilizador findByEmail(String email);// método para encontrar um utilizador pelo email
    List<Utilizador> findByNomeContaining(String nome);// método para encontrar utilizadores cujo nome contém uma string específica
    
    List<Utilizador> findByAprovado(boolean aprovado);// método para encontrar utilizadores pelo estado de aprovação
}