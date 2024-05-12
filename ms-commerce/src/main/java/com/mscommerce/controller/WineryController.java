package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.WineryDTO;
import com.mscommerce.service.WineryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/winery")
@RequiredArgsConstructor
public class WineryController {

    private final WineryService wineryService;

    @GetMapping("/all")
    public ResponseEntity<List<WineryDTO>> getAllWineries() throws ResourceNotFoundException {
        List<WineryDTO> wineryDTOs = wineryService.getAllWineries();
        return ResponseEntity.ok().body(wineryDTOs);
    }

    @GetMapping("/id/{wineryId}")
    public ResponseEntity<WineryDTO> getWineryById(@PathVariable Integer wineryId) throws ResourceNotFoundException {
        WineryDTO wineryDTO = wineryService.getWineryById(wineryId);
        return ResponseEntity.ok().body(wineryDTO);
    }

    @PostMapping("/create")
    public ResponseEntity<WineryDTO> createWinery(@RequestBody WineryDTO wineryDTO) throws BadRequestException {
        WineryDTO createdWinery = wineryService.createWinery(wineryDTO);
        return new ResponseEntity<>(createdWinery, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<WineryDTO> updateWinery(@RequestBody WineryDTO wineryDTO) throws ResourceNotFoundException {
        WineryDTO updatedWinery = wineryService.updateWinery(wineryDTO);
        return ResponseEntity.ok().body(updatedWinery);
    }

    @DeleteMapping("/delete/{wineryId}")
    public ResponseEntity<Void> deleteWinery(@PathVariable Integer wineryId) throws ResourceNotFoundException {
        wineryService.deleteWinery(wineryId);
        return ResponseEntity.noContent().build();
    }
}
