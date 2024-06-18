package com.msusers.controller;

import com.msusers.models.User;
import com.msusers.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "User Controller", description = "Handles all user related operations")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    @Operation(summary = "Find current user's profile", description = "Fetches the profile of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the user profile"),
            @ApiResponse(responseCode = "403", description = "Accessing the user profile you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The user profile you were trying to reach is not found")
    })
    public ResponseEntity<User> findUserById() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @ResponseBody
    @GetMapping("/kcprofile")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Find Keycloak user's profile", description = "Fetches the Keycloak profile of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved Keycloak user profile"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the Keycloak user profile"),
            @ApiResponse(responseCode = "403", description = "Accessing the Keycloak user profile you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The Keycloak user profile you were trying to reach is not found")
    })
    public UserRepresentation userProfile(@AuthenticationPrincipal Jwt jwt) {
        // Get the authenticated user's ID from the JWT
        String userId = jwt.getSubject();

        // Retrieve the user's profile from the service layer
        UserRepresentation userRepresentation = userService.getUserProfile(userId);

        return userRepresentation;
    }
}
