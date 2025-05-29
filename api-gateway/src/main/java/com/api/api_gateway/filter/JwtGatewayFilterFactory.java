package com.api.api_gateway.filter;

import com.api.api_gateway.dto.UserInfoDTO;
import com.api.api_gateway.exception.InvalidTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class JwtGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtGatewayFilterFactory.Config> {

    private final WebClient webClient;

    public JwtGatewayFilterFactory(WebClient.Builder webClientBuilder,
                                   @Value("${auth.service.url}") String authServiceUrl) {
        super(Config.class);
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();

    }

    public static class Config {
        private boolean includeUserInfo = true;

        public boolean isIncludeUserInfo() {
            return includeUserInfo;
        }

        public void setIncludeUserInfo(boolean includeUserInfo) {
            this.includeUserInfo = includeUserInfo;
        }
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (token == null || !token.startsWith("Bearer ")) {
                return Mono.error(InvalidTokenException::new);
            }

            return webClient.get()
                    .uri("/validate")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(UserInfoDTO.class)
                    .flatMap(userInfo -> {
                        if(config.isIncludeUserInfo() && userInfo!=null){
                            return chain.filter(
                                    exchange.mutate()
                                            .request(r -> r.headers(headers -> {
                                                headers.add("X-User-Id", userInfo.id());
                                                headers.add("X-User-Email", userInfo.email());
                                                headers.add("X-User-Role", userInfo.role());
                                            }))
                                    .build()
                            );
                        }
                        return chain.filter(exchange);
                    })
                    .onErrorResume(WebClientResponseException.Unauthorized.class, e ->
                            Mono.error(InvalidTokenException::new));
        };
    }
}

