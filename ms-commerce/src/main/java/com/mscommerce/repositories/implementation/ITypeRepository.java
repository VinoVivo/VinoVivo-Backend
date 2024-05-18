package com.mscommerce.repositories.implementation;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.TypeDTO;
import com.mscommerce.models.Type;

import java.util.List;

public interface ITypeRepository {

    List<TypeDTO> getAllTypes() throws ResourceNotFoundException;

    TypeDTO getTypeById(Integer typeId) throws ResourceNotFoundException;

    TypeDTO createType(TypeDTO typeDTO) throws BadRequestException;

    TypeDTO updateType(TypeDTO typeDTO) throws ResourceNotFoundException;

    void deleteType(Integer typeId) throws ResourceNotFoundException;

    TypeDTO convertTypeToTypeDTO(Type type);

    Type convertTypeDTOToType(TypeDTO typeDTO);
}
