package com.msusers.repositories.implementation;

import com.msusers.models.User;
import com.msusers.repositories.IUserRepository;
import lombok.RequiredArgsConstructor;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements IUserRepository {

    private final Keycloak keycloak;

    @Value("${proyecto.keycloak.realm}")
    private String realm;

    @Override
    public Optional<User> findById(String id) {
        UserRepresentation userRepresentation = keycloak
                .realm(realm)
                .users()
                .get(id)
                .toRepresentation();
        return Optional.of(fromRepresentation(userRepresentation));
    }

    private User fromRepresentation(UserRepresentation userRepresentation) {
        Map<String, List<String>> attributes = userRepresentation.getAttributes();
        return User.builder()
                .id(userRepresentation.getId())
                .userName(userRepresentation.getUsername())
                .email(userRepresentation.getEmail())
                .firstName(userRepresentation.getFirstName())
                .lastName(userRepresentation.getLastName())
                .dni(getFirstAttribute(attributes, "dni"))
                .cellphone(getFirstAttribute(attributes, "cellphone"))
                .state(getFirstAttribute(attributes, "state"))
                .city(getFirstAttribute(attributes, "city"))
                .address(getFirstAttribute(attributes, "address"))
                .photo(getFirstAttribute(attributes, "photo"))
                .build();
    }

    private String getFirstAttribute(Map<String, List<String>> attributes, String attributeName) {
        if (attributes == null) {
            return null;
        }
        List<String> attributeValues = attributes.get(attributeName);
        return attributeValues != null && !attributeValues.isEmpty() ? attributeValues.get(0) : null;
    }
}
