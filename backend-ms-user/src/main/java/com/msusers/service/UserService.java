package com.msusers.service;


import com.msusers.models.User;
import com.msusers.repositories.implementation.UserRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final Keycloak keycloak;
    private final UserRepositoryImpl userRepositoryImpl;

    @Value("${proyecto.keycloak.realm}")
    private String realm;

    public User getCurrentUser() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = jwt.getSubject();

        Optional<User> userResponse = userRepositoryImpl.findById(userId);
        return userResponse.orElseThrow(() -> new NoSuchElementException("User not found for ID: " + userId));
    }

    public UserRepresentation getUserProfile(String userId) {
        // Retrieve the user's profile from Keycloak
        return keycloak.realm(realm).users().get(userId).toRepresentation();
    }

    public void updateUserProfile(UserRepresentation updatedUser) {
        // Update the user's profile in Keycloak
        keycloak.realm(realm).users().get(updatedUser.getId()).update(updatedUser);
    }
}
