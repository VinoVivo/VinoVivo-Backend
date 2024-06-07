package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.variety.VarietyDTO;
import com.mscommerce.service.implementation.VarietyServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/variety")
@RequiredArgsConstructor
public class VarietyController {

    private final VarietyServiceImpl varietyServiceImpl;

    @GetMapping("/all")
    public ResponseEntity<List<VarietyDTO>> getAllVarieties() throws ResourceNotFoundException {
        List<VarietyDTO> varietyDTOs = varietyServiceImpl.getAllVarieties();
        return ResponseEntity.ok().body(varietyDTOs);
    }

    @GetMapping("/id/{varietyId}")
    public ResponseEntity<VarietyDTO> getVarietyById(@PathVariable Integer varietyId) throws ResourceNotFoundException {
        VarietyDTO varietyDTO = varietyServiceImpl.getVarietyById(varietyId);
        return ResponseEntity.ok().body(varietyDTO);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VarietyDTO> createVariety(@RequestBody VarietyDTO varietyDTO) throws BadRequestException {
        VarietyDTO createdVariety = varietyServiceImpl.createVariety(varietyDTO);
        return new ResponseEntity<>(createdVariety, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VarietyDTO> updateVariety(@RequestBody VarietyDTO varietyDTO) throws BadRequestException, ResourceNotFoundException {
        VarietyDTO updatedVariety = varietyServiceImpl.updateVariety(varietyDTO);
        return ResponseEntity.ok().body(updatedVariety);
    }

    @DeleteMapping("/delete/{varietyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVariety(@PathVariable Integer varietyId) throws ResourceNotFoundException {
        varietyServiceImpl.deleteVariety(varietyId);
        return ResponseEntity.noContent().build();
    }
}
