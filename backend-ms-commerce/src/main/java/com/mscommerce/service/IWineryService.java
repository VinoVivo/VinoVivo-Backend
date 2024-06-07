package com.mscommerce.service;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.winery.WineryDTO;

import java.util.List;

public interface IWineryService {

    List<WineryDTO> getAllWineries() throws ResourceNotFoundException;

    WineryDTO getWineryById(Integer wineryId) throws ResourceNotFoundException;

    WineryDTO createWinery(WineryDTO wineryDTO) throws BadRequestException;

    WineryDTO updateWinery(WineryDTO wineryDTO) throws ResourceNotFoundException, BadRequestException;

    void deleteWinery(Integer wineryId) throws ResourceNotFoundException;

}
