package com.mscommerce.repositories.implementation;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.WineryDTO;
import com.mscommerce.models.Winery;

import java.util.List;

public interface IWineryRepository {

    List<WineryDTO> getAllWineries() throws ResourceNotFoundException;

    WineryDTO getWineryById(Integer wineryId) throws ResourceNotFoundException;

    WineryDTO createWinery(WineryDTO wineryDTO) throws BadRequestException;

    WineryDTO updateWinery(WineryDTO wineryDTO) throws ResourceNotFoundException;

    void deleteWinery(Integer wineryId) throws ResourceNotFoundException;

    WineryDTO convertWineryToWineryDTO(Winery winery);

    Winery convertWineryDTOToWinery(WineryDTO wineryDTO);
}
