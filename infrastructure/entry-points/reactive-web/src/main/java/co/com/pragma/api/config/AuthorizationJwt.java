package co.com.pragma.api.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import reactor.core.publisher.Mono;


import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Log4j2
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class AuthorizationJwt implements WebFluxConfigurer {

    private final String secretKey;
    private final String jsonExpRoles;

    private static final String ROLE = "ROLE_";

    public AuthorizationJwt(@Value("${jwt.secret-key}") String secretKey,
                            @Value("${jwt.json-exp-roles}") String jsonExpRoles) {
        this.secretKey = secretKey;
        this.jsonExpRoles = jsonExpRoles;
    }

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorize -> authorize
                        .pathMatchers("webjars/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyExchange().authenticated() // Todas las demás rutas requieren autenticación
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwtSpec ->
                                jwtSpec
                                        .jwtDecoder(jwtDecoder())
                                        .jwtAuthenticationConverter(grantedAuthoritiesExtractor())
                        )
                );
        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        // Convertir secretKey a SecretKey
        SecretKey key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder.withSecretKey(key).build();

        // Validadores opcionales
        jwtDecoder.setJwtValidator(JwtValidators.createDefault());

        return jwtDecoder;
    }


    public Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        var jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = getRole(jwt.getClaims(), jsonExpRoles);
            if (role != null && !role.trim().isEmpty()) {
                String formattedRole = role.startsWith(ROLE) ? role : ROLE + role;
                return List.of(new SimpleGrantedAuthority(formattedRole));
            }
            log.warn("No role found in JWT for claim: {}", jsonExpRoles);
            return List.of();
        });
        return new ReactiveJwtAuthenticationConverterAdapter(jwtConverter);
    }

    private String getRole(Map<String, Object> claims, String jsonExpClaim){
        try {

            Object value = claims.get(jsonExpClaim);
            // Si el claim es directo (no anidado)
            if (value != null) {
                return value.toString();
            }

            // Si es path anidado (ej: "realm_access.role")
            if (jsonExpClaim.contains(".")) {
                String[] parts = jsonExpClaim.split("\\.", 2); // Split solo una vez
                Object nested = claims.get(parts[0]);
                if (nested instanceof Map) {
                    return getRole((Map<String, Object>) nested, parts[1]);
                }
            }

            return null;

        } catch (Exception  e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
