package com.msusers.controller;


import com.msusers.models.User;
import com.msusers.service.UserService;
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
public class UserController {

    private final UserService userService;


    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    public ResponseEntity<User> findUserById() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @ResponseBody
    @GetMapping("/kcprofile")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    public UserRepresentation userProfile(@AuthenticationPrincipal Jwt jwt) {
        // Get the authenticated user's ID from the JWT
        String userId = jwt.getSubject();

        // Retrieve the user's profile from the service layer
        UserRepresentation userRepresentation = userService.getUserProfile(userId);

        return userRepresentation;
    }

    @PostMapping("/profile/update")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    public String updateUserProfile(@ModelAttribute UserRepresentation updatedUser) {
        // Update the user's profile via the service layer
        userService.updateUserProfile(updatedUser);

        // Redirect the user to the profile page
        return "redirect:/profile";
    }
}
