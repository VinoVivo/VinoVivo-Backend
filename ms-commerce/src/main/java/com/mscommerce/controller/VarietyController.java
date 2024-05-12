package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.VarietyDTO;
import com.mscommerce.service.VarietyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/variety")
@RequiredArgsConstructor
public class VarietyController {

    private final VarietyService varietyService;

    @GetMapping("/all")
    public ResponseEntity<List<VarietyDTO>> getAllVarieties() throws ResourceNotFoundException {
        List<VarietyDTO> varietyDTOs = varietyService.getAllVarieties();
        return ResponseEntity.ok().body(varietyDTOs);
    }

    @GetMapping("/id/{varietyId}")
    public ResponseEntity<VarietyDTO> getVarietyById(@PathVariable Integer varietyId) throws ResourceNotFoundException {
        VarietyDTO varietyDTO = varietyService.getVarietyById(varietyId);
        return ResponseEntity.ok().body(varietyDTO);
    }

    @PostMapping("/create")
    public ResponseEntity<VarietyDTO> createVariety(@RequestBody VarietyDTO varietyDTO) throws BadRequestException {
        VarietyDTO createdVariety = varietyService.createVariety(varietyDTO);
        return new ResponseEntity<>(createdVariety, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<VarietyDTO> updateVariety(@RequestBody VarietyDTO varietyDTO) throws ResourceNotFoundException {
        VarietyDTO updatedVariety = varietyService.updateVariety(varietyDTO);
        return ResponseEntity.ok().body(updatedVariety);
    }

    @DeleteMapping("/delete/{varietyId}")
    public ResponseEntity<Void> deleteVariety(@PathVariable Integer varietyId) throws ResourceNotFoundException {
        varietyService.deleteVariety(varietyId);
        return ResponseEntity.noContent().build();
    }
}
