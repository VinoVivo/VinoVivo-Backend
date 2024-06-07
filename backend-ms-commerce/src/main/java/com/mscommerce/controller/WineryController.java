package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.winery.WineryDTO;
import com.mscommerce.service.implementation.WineryServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/winery")
@RequiredArgsConstructor
public class WineryController {

    private final WineryServiceImpl wineryServiceImpl;

    @GetMapping("/all")
    public ResponseEntity<List<WineryDTO>> getAllWineries() throws ResourceNotFoundException {
        List<WineryDTO> wineryDTOs = wineryServiceImpl.getAllWineries();
        return ResponseEntity.ok().body(wineryDTOs);
    }

    @GetMapping("/id/{wineryId}")
    public ResponseEntity<WineryDTO> getWineryById(@PathVariable Integer wineryId) throws ResourceNotFoundException {
        WineryDTO wineryDTO = wineryServiceImpl.getWineryById(wineryId);
        return ResponseEntity.ok().body(wineryDTO);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WineryDTO> createWinery(@RequestBody WineryDTO wineryDTO) throws BadRequestException {
        WineryDTO createdWinery = wineryServiceImpl.createWinery(wineryDTO);
        return new ResponseEntity<>(createdWinery, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WineryDTO> updateWinery(@RequestBody WineryDTO wineryDTO) throws BadRequestException, ResourceNotFoundException {
        WineryDTO updatedWinery = wineryServiceImpl.updateWinery(wineryDTO);
        return ResponseEntity.ok().body(updatedWinery);
    }

    @DeleteMapping("/delete/{wineryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWinery(@PathVariable Integer wineryId) throws ResourceNotFoundException {
        wineryServiceImpl.deleteWinery(wineryId);
        return ResponseEntity.noContent().build();
    }
}
