package uco.test.api.infraestructura.input.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SeguridadConfiguracion {

    @Bean
    SecurityFilterChain seguridad(HttpSecurity http) throws Exception {
        http.cors(cors -> {})
            // API sin estado: cada peticion trae su token de Auth0 en la cabecera Authorization,
            // no hay cookies de sesion, por eso CSRF no aplica.
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(rutas -> rutas
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(recurso -> recurso
                .jwt(jwt -> {})
                .authenticationEntryPoint((req, res, ex) -> responder(res, 401, "Debes iniciar sesion"))
                .accessDeniedHandler((req, res, ex) -> responder(res, 403, motivo(ex))))
            .exceptionHandling(errores -> errores
                .authenticationEntryPoint((req, res, ex) -> responder(res, 401, "Debes iniciar sesion"))
                .accessDeniedHandler((req, res, ex) -> responder(res, 403, motivo(ex))));
        return http.build();
    }

    /** Usa el mensaje propio de PermisosDocente; el generico de Spring ("Access Denied") se traduce. */
    private static String motivo(Exception ex) {
        String mensaje = ex.getMessage();
        return mensaje == null || mensaje.equals("Access Denied") ? "Acceso no permitido" : mensaje;
    }

    /**
     * Valida la firma, la fecha, el emisor (tenant de Auth0) y la audiencia (la API) del token.
     * Las claves publicas se descargan de Auth0 la primera vez que llega un token.
     * Para pruebas se puede indicar una clave publica local con app.auth.clave-publica.
     */
    @Bean
    JwtDecoder jwtDecoder(@Value("${app.auth.issuer}") String issuer,
                          @Value("${app.auth.audience}") String audience,
                          @Value("${app.auth.clave-publica:#{null}}") Resource clavePublica) throws IOException {
        NimbusJwtDecoder decoder;
        if (clavePublica != null) {
            try (InputStream entrada = clavePublica.getInputStream()) {
                decoder = NimbusJwtDecoder.withPublicKey(RsaKeyConverters.x509().convert(entrada)).build();
            }
        } else {
            decoder = NimbusJwtDecoder.withJwkSetUri(issuer + ".well-known/jwks.json").build();
        }
        OAuth2TokenValidator<Jwt> validarAudiencia = new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,
                audiencias -> audiencias != null && audiencias.contains(audience));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer), validarAudiencia));
        return decoder;
    }

    private static void responder(jakarta.servlet.http.HttpServletResponse res, int estado, String mensaje)
            throws IOException {
        res.setStatus(estado);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String seguro = mensaje.replace("\\", "\\\\").replace("\"", "\\\"");
        res.getWriter().write("{\"mensaje\":\"" + seguro + "\"}");
    }
}
