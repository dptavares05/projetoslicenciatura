package uevora.tw.roomrent.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import uevora.tw.roomrent.model.Utilizador;
import uevora.tw.roomrent.repository.UtilizadorRepository;

@Service
public class AutenticacaoService implements UserDetailsService {

    @Autowired
    private UtilizadorRepository utilizadorRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {// metodo para carregar o user pelo email
        // ir à base de dados buscar o utilizador pelo email
        Utilizador user = utilizadorRepo.findByEmail(email);

        if (user == null) {// se não existir na bd, lançar exceção
            throw new UsernameNotFoundException("Utilizador não encontrado com o email: " + email);
        }

        // Retornamos o próprio objeto 'user' da tua base de dados. pois ao implementar userdetails o spring verifica automaticamente o user  com o isEnabled()
        return user; 
    }
}