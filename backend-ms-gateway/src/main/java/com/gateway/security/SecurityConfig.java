package com.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange(auth -> auth
                        .pathMatchers("/",
                                "/ms-commerce/",
                                "/ms-users/",
                                "/ms-commerce/product/type/all",
                                "/ms-commerce/product/random",
                                "/ms-commerce/product/id/**",
                                "/ms-commerce/product/winery/**",
                                "/ms-commerce/product/variety/**",
                                "/ms-commerce/product/type/**",
                                "/ms-commerce/winery/all",
                                "/ms-commerce/winery/id/**",
                                "/ms-commerce/variety/all",
                                "/ms-commerce/variety/id/**",
                                "/ms-commerce/type/all",
                                "/ms-commerce/type/id/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()));
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        return http.build();
    }
}
