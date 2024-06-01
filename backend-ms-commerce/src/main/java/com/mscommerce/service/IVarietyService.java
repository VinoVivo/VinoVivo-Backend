package com.mscommerce.service;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.VarietyDTO;
import com.mscommerce.models.Variety;

import java.util.List;

public interface IVarietyService {

    List<VarietyDTO> getAllVarieties() throws ResourceNotFoundException;

    VarietyDTO getVarietyById(Integer varietyId) throws ResourceNotFoundException;

    VarietyDTO createVariety(VarietyDTO varietyDTO) throws BadRequestException;

    VarietyDTO updateVariety(VarietyDTO varietyDTO) throws ResourceNotFoundException;

    void deleteVariety(Integer varietyId) throws ResourceNotFoundException;

    VarietyDTO convertVarietyToVarietyDTO(Variety variety);

    Variety convertVarietyDTOToVariety(VarietyDTO varietyDTO);
}
