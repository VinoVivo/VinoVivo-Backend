package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.type.TypeDTO;
import com.mscommerce.service.implementation.TypeServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/type")
@Tag(name = "Type Controller", description = "Handles all type related operations")
public class TypeController {

    private final TypeServiceImpl typeServiceImpl;

    @GetMapping("/all")
    @Operation(summary = "Fetch all types", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of all types"),
            @ApiResponse(responseCode = "404", description = "Types not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<List<TypeDTO>> getAllTypes() throws ResourceNotFoundException {
        List<TypeDTO> typeDTOs = typeServiceImpl.getAllTypes();
        return ResponseEntity.ok().body(typeDTOs);
    }

    @GetMapping("/id/{typeId}")
    @Operation(summary = "Fetch type by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the type with the provided ID"),
            @ApiResponse(responseCode = "404", description = "Type not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<TypeDTO> getTypeById(@PathVariable Integer typeId) throws ResourceNotFoundException {
        TypeDTO typeDTO = typeServiceImpl.getTypeById(typeId);
        return ResponseEntity.ok().body(typeDTO);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new type", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully created a new type"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<TypeDTO> createType(@RequestBody TypeDTO typeDTO) throws BadRequestException {
        TypeDTO createdType = typeServiceImpl.createType(typeDTO);
        return new ResponseEntity<>(createdType, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing type", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the existing type"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "Type not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<TypeDTO> updateType(@RequestBody TypeDTO typeDTO) throws BadRequestException, ResourceNotFoundException {
        TypeDTO updatedType = typeServiceImpl.updateType(typeDTO);
        return ResponseEntity.ok().body(updatedType);
    }

    @DeleteMapping("/delete/{typeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a specific type", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the specific type"),
            @ApiResponse(responseCode = "404", description = "Type not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<Void> deleteType(@PathVariable Integer typeId) throws ResourceNotFoundException {
        typeServiceImpl.deleteType(typeId);
        return ResponseEntity.noContent().build();
    }
}
