package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.type.TypeDTO;
import com.mscommerce.service.implementation.TypeServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/type")
@RequiredArgsConstructor
public class TypeController {

    private final TypeServiceImpl typeServiceImpl;

    @GetMapping("/all")
    public ResponseEntity<List<TypeDTO>> getAllTypes() throws ResourceNotFoundException {
        List<TypeDTO> typeDTOs = typeServiceImpl.getAllTypes();
        return ResponseEntity.ok().body(typeDTOs);
    }

    @GetMapping("/id/{typeId}")
    public ResponseEntity<TypeDTO> getTypeById(@PathVariable Integer typeId) throws ResourceNotFoundException {
        TypeDTO typeDTO = typeServiceImpl.getTypeById(typeId);
        return ResponseEntity.ok().body(typeDTO);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TypeDTO> createType(@RequestBody TypeDTO typeDTO) throws BadRequestException {
        TypeDTO createdType = typeServiceImpl.createType(typeDTO);
        return new ResponseEntity<>(createdType, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TypeDTO> updateType(@RequestBody TypeDTO typeDTO) throws BadRequestException, ResourceNotFoundException {
        TypeDTO updatedType = typeServiceImpl.updateType(typeDTO);
        return ResponseEntity.ok().body(updatedType);
    }

    @DeleteMapping("/delete/{typeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteType(@PathVariable Integer typeId) throws ResourceNotFoundException {
        typeServiceImpl.deleteType(typeId);
        return ResponseEntity.noContent().build();
    }
}
