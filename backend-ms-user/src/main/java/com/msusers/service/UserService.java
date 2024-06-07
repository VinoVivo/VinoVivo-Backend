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

    /**
     * Retrieves the current authenticated user's information from the security context,
     * then fetches the user's details from the database using the user ID.
     * @return A User entity.
     */
    public User getCurrentUser() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = jwt.getSubject();

        Optional<User> userResponse = userRepositoryImpl.findById(userId);
        return userResponse.orElseThrow(() -> new NoSuchElementException("User not found for ID: " + userId));
    }

    /**
     * Retrieves a user's profile from Keycloak using the user ID.
     * @param userId A string representing the user's ID.
     * @return A UserRepresentation object containing the user's profile information.
     */
    public UserRepresentation getUserProfile(String userId) {
        // Retrieve the user's profile from Keycloak
        return keycloak.realm(realm).users().get(userId).toRepresentation();
    }

}
