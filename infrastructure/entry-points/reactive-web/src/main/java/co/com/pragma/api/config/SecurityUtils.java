package co.com.pragma.api.config;


import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component
public class SecurityUtils {

    /**
     * Obtiene el ID del usuario desde el JWT.
     */
    public  Mono<String> getDocument() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(JwtAuthenticationToken.class)
                .map(auth -> auth.getToken().getSubject());
    }


    /**
     * Obtiene un único rol del usuario desde el JWT (el primero si hay varios).
     */
    public  Mono<String> getUserRole() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(JwtAuthenticationToken.class)
                .map(auth -> {
                    // Obtener el claim como String (no como lista)
                    String role = auth.getToken().getClaimAsString("role");
                    return (role != null && !role.trim().isEmpty()) ? role : null;
                });
    }

    /**
     * Obtiene el token JWT completo en formato String.
     */
    public Mono<String> getUserToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(JwtAuthenticationToken.class)
                .map(auth -> auth.getToken().getTokenValue());
    }
}
