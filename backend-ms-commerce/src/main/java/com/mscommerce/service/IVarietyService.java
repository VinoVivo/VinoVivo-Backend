package com.mscommerce.service;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.variety.VarietyDTO;

import java.util.List;

public interface IVarietyService {

    List<VarietyDTO> getAllVarieties() throws ResourceNotFoundException;

    VarietyDTO getVarietyById(Integer varietyId) throws ResourceNotFoundException;

    VarietyDTO createVariety(VarietyDTO varietyDTO) throws BadRequestException;

    VarietyDTO updateVariety(VarietyDTO varietyDTO) throws ResourceNotFoundException, BadRequestException;

    void deleteVariety(Integer varietyId) throws ResourceNotFoundException;

}
