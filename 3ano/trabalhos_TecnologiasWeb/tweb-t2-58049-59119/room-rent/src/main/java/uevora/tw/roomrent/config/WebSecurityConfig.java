package uevora.tw.roomrent.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index", "/ofertas", "/procuras", "/login", "/registar", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
                
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureHandler(customFailureHandler()) 
                .permitAll()
            )
                
             
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .exceptionHandling(e -> e
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    // Quando der 403, envia para a home em vez de mostrar erro
                    response.sendRedirect("/"); 
                })
            );
        return http.build();
    }

    // --- MANIPULADOR DE ERROS CUSTOM---
    @Bean
    public AuthenticationFailureHandler customFailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                                AuthenticationException exception) throws IOException, ServletException {
                
                // Se a conta não estiver ativada (isEnabled() == false no Utilizador)
                if (exception instanceof DisabledException) {
                    setDefaultFailureUrl("/login?error=not_approved");
                } else {
                    // Outros erros de autenticacao(ex: password errada)
                    setDefaultFailureUrl("/login?error=bad_credentials");
                }
                
                super.onAuthenticationFailure(request, response, exception);
            }
        };
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}