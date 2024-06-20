package com.msusers.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Status Controller", description = "Handles operations related to the status of the microservice")
public class Controller {

    @GetMapping("/")
    @Operation(summary = "Check the status of the microservice", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the status of the microservice"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<String> get() {
            return ResponseEntity.ok("Users microservice ON");
        }

}
