package uco.test.api.infraestructura.input.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import uco.test.api.infraestructura.output.repositorios.estructurados.UsuarioRepositorioJPA;

@Configuration
@EnableMethodSecurity
public class SeguridadConfiguracion {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    UserDetailsService usuarios(UsuarioRepositorioJPA repositorio) {
        return nombre -> {
            var usuario = repositorio.findByUsuario(nombre)
                    .orElseThrow(() -> new UsernameNotFoundException("Credenciales incorrectas"));
            return User.withUsername(usuario.getUsuario()).password(usuario.getClaveHash()).roles("DOCENTE").build();
        };
    }

    @Bean
    SecurityFilterChain seguridad(HttpSecurity http) throws Exception {
        http.cors(cors -> {})
            .authorizeHttpRequests(rutas -> rutas
                .requestMatchers("/", "/index.html", "/app.js", "/styles.css", "/favicon.svg",
                        "/api/v1/auth/csrf", "/api/v1/auth/login", "/error").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(errores -> errores
                .authenticationEntryPoint((req, res, ex) -> responder(res, 401, "Debes iniciar sesion"))
                .accessDeniedHandler((req, res, ex) -> responder(res, 403, "Acceso no permitido o token CSRF invalido")))
            .requestCache(cache -> cache.disable())
            .formLogin(login -> login.loginProcessingUrl("/api/v1/auth/login")
                .usernameParameter("usuario").passwordParameter("contrasena")
                .successHandler((req, res, auth) -> responder(res, 200, "Sesion iniciada"))
                .failureHandler((req, res, ex) -> responder(res, 401, "Credenciales incorrectas")))
            .logout(logout -> logout.logoutUrl("/api/v1/auth/logout")
                .invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID")
                .logoutSuccessHandler((req, res, auth) -> responder(res, 200, "Sesion cerrada")));
        // CSRF permanece habilitado, incluida la autenticacion y el cierre de sesion.
        return http.build();
    }

    private static void responder(jakarta.servlet.http.HttpServletResponse res, int estado, String mensaje)
            throws java.io.IOException {
        res.setStatus(estado);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"mensaje\":\"" + mensaje + "\"}");
    }
}
