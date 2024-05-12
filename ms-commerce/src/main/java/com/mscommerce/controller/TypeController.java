package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.TypeDTO;
import com.mscommerce.service.TypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/type")
@RequiredArgsConstructor
public class TypeController {

    private final TypeService typeService;


    @GetMapping("/all")
    public ResponseEntity<List<TypeDTO>> getAllTypes() throws ResourceNotFoundException {
        List<TypeDTO> typeDTOs = typeService.getAllTypes();
        return ResponseEntity.ok().body(typeDTOs);
    }

    @GetMapping("/id/{typeId}")
    public ResponseEntity<TypeDTO> getTypeById(@PathVariable Integer typeId) throws ResourceNotFoundException {
        TypeDTO typeDTO = typeService.getTypeById(typeId);
        return ResponseEntity.ok().body(typeDTO);
    }

    @PostMapping("/create")
    public ResponseEntity<TypeDTO> createType(@RequestBody TypeDTO typeDTO) throws BadRequestException {
        TypeDTO createdType = typeService.createType(typeDTO);
        return new ResponseEntity<>(createdType, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<TypeDTO> updateType(@RequestBody TypeDTO typeDTO) throws ResourceNotFoundException {
        TypeDTO updatedType = typeService.updateType(typeDTO);
        return ResponseEntity.ok().body(updatedType);
    }

    @DeleteMapping("/delete/{typeId}")
    public ResponseEntity<Void> deleteType(@PathVariable Integer typeId) throws ResourceNotFoundException {
        typeService.deleteType(typeId);
        return ResponseEntity.noContent().build();
    }
}
