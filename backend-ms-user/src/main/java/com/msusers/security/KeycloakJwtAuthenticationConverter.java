package com.msusers.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.*;
import java.util.stream.Collectors;


public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt source) {
        try {
            Collection<? extends GrantedAuthority> authorities = extractRealmRoles(source);
            return new JwtAuthenticationToken(source, authorities);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting JWT", e);
        }
    }

    private Collection<? extends GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, List<String>> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || realmAccess.isEmpty()) {
            return new HashSet<>();
        }
        List<String> roles = realmAccess.get("roles");
        if (roles == null || roles.isEmpty()) {
            return new HashSet<>();
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase().replace("-", "_")))
                .collect(Collectors.toSet());
    }
}
