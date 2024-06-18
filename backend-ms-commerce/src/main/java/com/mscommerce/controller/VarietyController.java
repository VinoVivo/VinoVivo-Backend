package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.variety.VarietyDTO;
import com.mscommerce.service.implementation.VarietyServiceImpl;
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
@RequestMapping("/variety")
@Tag(name = "Variety Controller", description = "Handles all variety related operations")
public class VarietyController {

    private final VarietyServiceImpl varietyServiceImpl;

    @GetMapping("/all")
    @Operation(summary = "Fetch all varieties", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of all varieties"),
            @ApiResponse(responseCode = "404", description = "Varieties not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<List<VarietyDTO>> getAllVarieties() throws ResourceNotFoundException {
        List<VarietyDTO> varietyDTOs = varietyServiceImpl.getAllVarieties();
        return ResponseEntity.ok().body(varietyDTOs);
    }

    @GetMapping("/id/{varietyId}")
    @Operation(summary = "Fetch variety by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the variety with the provided ID"),
            @ApiResponse(responseCode = "404", description = "Variety not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<VarietyDTO> getVarietyById(@PathVariable Integer varietyId) throws ResourceNotFoundException {
        VarietyDTO varietyDTO = varietyServiceImpl.getVarietyById(varietyId);
        return ResponseEntity.ok().body(varietyDTO);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new variety", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully created a new variety"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<VarietyDTO> createVariety(@RequestBody VarietyDTO varietyDTO) throws BadRequestException {
        VarietyDTO createdVariety = varietyServiceImpl.createVariety(varietyDTO);
        return new ResponseEntity<>(createdVariety, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing variety", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the existing variety"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "Variety not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<VarietyDTO> updateVariety(@RequestBody VarietyDTO varietyDTO) throws BadRequestException, ResourceNotFoundException {
        VarietyDTO updatedVariety = varietyServiceImpl.updateVariety(varietyDTO);
        return ResponseEntity.ok().body(updatedVariety);
    }

    @DeleteMapping("/delete/{varietyId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a specific variety", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the specific variety"),
            @ApiResponse(responseCode = "404", description = "Variety not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<Void> deleteVariety(@PathVariable Integer varietyId) throws ResourceNotFoundException {
        varietyServiceImpl.deleteVariety(varietyId);
        return ResponseEntity.noContent().build();
    }
}
