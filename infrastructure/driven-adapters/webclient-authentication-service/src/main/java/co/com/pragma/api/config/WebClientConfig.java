package co.com.pragma.api.config;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Configuration
public class WebClientConfig {

    private final String url;

    private final int timeout;

    public WebClientConfig(@Value("${adapter.restconsumer.url}") String url,
                              @Value("${adapter.restconsumer.timeout}") int timeout) {
        this.url = url;
        this.timeout = timeout;
    }


    @Bean
    public WebClient getWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .clientConnector(getClientHttpConnector())
                .filter((request, next) ->
                        ReactiveSecurityContextHolder.getContext()
                                .map(SecurityContext::getAuthentication)
                                .map(auth -> {
                                    String tokenValue;
                                    if (auth instanceof JwtAuthenticationToken) {
                                        Jwt jwt = ((JwtAuthenticationToken) auth).getToken();
                                        tokenValue = jwt.getTokenValue(); // <-- aquí obtienes el string del JWT
                                    } else {
                                        tokenValue = auth.getCredentials().toString(); // fallback
                                    }

                                    return ClientRequest.from(request)
                                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValue)
                                            .build();
                                })
                                .defaultIfEmpty(request)
                                .flatMap(next::exchange)
                )
                .build();
    }

    private ClientHttpConnector getClientHttpConnector() {

        return new ReactorClientHttpConnector(HttpClient.create()
                .compress(true)
                .keepAlive(true)
                .option(CONNECT_TIMEOUT_MILLIS, timeout)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(timeout, MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(timeout, MILLISECONDS));
                }));
    }
}