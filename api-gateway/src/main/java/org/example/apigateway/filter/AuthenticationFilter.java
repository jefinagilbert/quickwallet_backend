package org.example.apigateway.filter;

import org.example.apigateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;

    @Override
    public String name() {
        return "AuthenticationFilter"; // Tells Spring exactly what to look for in the YAML
    }

    public AuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            // Check if the request has an Authorization header
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Extract the token
            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            } else {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            try {
                // Validate the token
                jwtUtil.validateToken(authHeader);

                String userId = jwtUtil.extractSubject(authHeader);

                org.springframework.http.server.reactive.ServerHttpRequest mutatedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-Auth-User-Id", userId)
                    .build();
                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                // Token is expired or tampered with
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

        });
    }

    public static class Config {
        // Configuration properties can go here if needed
    }
}