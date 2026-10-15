package br.com.joalheria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Map;

@Configuration
public class SecurityConfig {
    @Bean
    AuthenticationProvider donaAuthenticationProvider(JdbcTemplate jdbc) {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String login = authentication.getName();
                String senha = String.valueOf(authentication.getCredentials());
                // Reutiliza a procedure de login e a mesma SHA2 do banco original.
                List<Map<String,Object>> usuarios = jdbc.queryForList("CALL PROC_LOGIN_USUARIO(?, ?)", login, senha);
                if (usuarios.isEmpty() || !"DONA".equals(usuarios.get(0).get("A01_perfil"))) {
                    throw new BadCredentialsException("Usuário ou senha inválidos");
                }
                return UsernamePasswordAuthenticationToken.authenticated(login, null,
                    List.of(new SimpleGrantedAuthority("ROLE_DONA")));
            }
            @Override
            public boolean supports(Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider donaAuthenticationProvider) throws Exception {
        http.authenticationProvider(donaAuthenticationProvider)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/favicon.ico", "/error").permitAll()
                .anyRequest().hasRole("DONA"))
            .formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true).failureUrl("/login?erro"))
            .logout(logout -> logout.logoutUrl("/sair").logoutSuccessUrl("/login?saiu")
                .invalidateHttpSession(true).deleteCookies("JSESSIONID"));
        return http.build();
    }
}
