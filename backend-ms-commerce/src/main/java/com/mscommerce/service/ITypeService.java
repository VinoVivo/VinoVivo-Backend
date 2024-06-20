package com.mscommerce.service;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.type.TypeDTO;

import java.util.List;

public interface ITypeService {

    List<TypeDTO> getAllTypes() throws ResourceNotFoundException;

    TypeDTO getTypeById(Integer typeId) throws ResourceNotFoundException;

    TypeDTO createType(TypeDTO typeDTO) throws BadRequestException;

    TypeDTO updateType(TypeDTO typeDTO) throws ResourceNotFoundException, BadRequestException;

    void deleteType(Integer typeId) throws ResourceNotFoundException;

}
