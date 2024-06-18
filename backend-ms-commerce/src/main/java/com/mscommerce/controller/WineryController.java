package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.winery.WineryDTO;
import com.mscommerce.service.implementation.WineryServiceImpl;
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
@RequestMapping("/winery")
@Tag(name = "Winery Controller", description = "Handles all winery related operations")
public class WineryController {

    private final WineryServiceImpl wineryServiceImpl;

    @GetMapping("/all")
    @Operation(summary = "Fetch all wineries", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of all wineries"),
            @ApiResponse(responseCode = "404", description = "Wineries not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<List<WineryDTO>> getAllWineries() throws ResourceNotFoundException {
        List<WineryDTO> wineryDTOs = wineryServiceImpl.getAllWineries();
        return ResponseEntity.ok().body(wineryDTOs);
    }

    @GetMapping("/id/{wineryId}")
    @Operation(summary = "Fetch winery by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the winery with the provided ID"),
            @ApiResponse(responseCode = "404", description = "Winery not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<WineryDTO> getWineryById(@PathVariable Integer wineryId) throws ResourceNotFoundException {
        WineryDTO wineryDTO = wineryServiceImpl.getWineryById(wineryId);
        return ResponseEntity.ok().body(wineryDTO);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new winery", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully created a new winery"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<WineryDTO> createWinery(@RequestBody WineryDTO wineryDTO) throws BadRequestException {
        WineryDTO createdWinery = wineryServiceImpl.createWinery(wineryDTO);
        return new ResponseEntity<>(createdWinery, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing winery", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the existing winery"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "Winery not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<WineryDTO> updateWinery(@RequestBody WineryDTO wineryDTO) throws BadRequestException, ResourceNotFoundException {
        WineryDTO updatedWinery = wineryServiceImpl.updateWinery(wineryDTO);
        return ResponseEntity.ok().body(updatedWinery);
    }

    @DeleteMapping("/delete/{wineryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a specific winery", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the specific winery"),
            @ApiResponse(responseCode = "404", description = "Winery not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<Void> deleteWinery(@PathVariable Integer wineryId) throws ResourceNotFoundException {
        wineryServiceImpl.deleteWinery(wineryId);
        return ResponseEntity.noContent().build();
    }
}
