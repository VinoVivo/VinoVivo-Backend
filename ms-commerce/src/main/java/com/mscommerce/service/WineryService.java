package com.mscommerce.service;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.WineryDTO;
import com.mscommerce.models.Winery;
import com.mscommerce.repositories.WineryRepository;
import com.mscommerce.repositories.implementation.IWineryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WineryService implements IWineryRepository {

    private final WineryRepository wineryRepository;


    // Method to retrieve all wineries
    @Override
    public List<WineryDTO> getAllWineries() throws ResourceNotFoundException {
        try {
            // Retrieve all wineries from the repository
            List<Winery> wineries = wineryRepository.findAll();
            // Convert the list of wineries to a list of DTOs and return
            return wineries.stream()
                    .map(this::convertWineryToWineryDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            // If an exception occurs, throw a ResourceNotFoundException
            throw new ResourceNotFoundException("Failed to fetch wineries");
        }
    }

    // Method to retrieve a winery by its ID
    @Override
    public WineryDTO getWineryById(Integer wineryId) throws ResourceNotFoundException {
        try {
            // Retrieve the winery from the repository by ID
            Optional<Winery> wineryOptional = wineryRepository.findById(wineryId);
            if (wineryOptional.isEmpty()) {
                // If the winery is not found, throw a ResourceNotFoundException
                throw new ResourceNotFoundException("Winery not found with ID: " + wineryId);
            }
            // Convert the winery to a DTO and return
            Winery winery = wineryOptional.get();
            return convertWineryToWineryDTO(winery);
        } catch (ResourceNotFoundException ex) {
            // If a ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while getting Winery by ID", ex);
        }
    }

    // Method to create a new winery
    @Override
    public WineryDTO createWinery(WineryDTO wineryDTO) throws BadRequestException {
        try {
            // Convert the DTO to a Winery entity
            Winery winery = convertWineryDTOToWinery(wineryDTO);
            // Save the winery in the repository
            Winery savedWinery = wineryRepository.save(winery);
            // Set the ID of the DTO and return
            wineryDTO.setId(savedWinery.getId());
            return wineryDTO;
        } catch (Exception ex) {
            // If an exception occurs, throw a BadRequestException
            throw new BadRequestException("The received request does not have the correct format.");
        }
    }

    // Method to update an existing winery
    @Override
    public WineryDTO updateWinery(WineryDTO wineryDTO) throws ResourceNotFoundException {
        try {
            // Check if the winery exists
            Winery existingWinery = wineryRepository.findById(wineryDTO.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Winery not found with ID: " + wineryDTO.getId()));
            // Update the name of the existing winery
            existingWinery.setName(wineryDTO.getName());
            // Save the updated winery
            Winery savedWinery = wineryRepository.save(existingWinery);
            // Convert the updated winery to DTO and return
            return convertWineryToWineryDTO(savedWinery);
        } catch (ResourceNotFoundException ex) {
            // If a ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while updating Winery", ex);
        }
    }

    // Method to delete a winery
    @Override
    public void deleteWinery(Integer wineryId) throws ResourceNotFoundException {
        try {
            // Check if the winery exists
            Winery existingWinery = wineryRepository.findById(wineryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Winery not found with ID: " + wineryId));
            // Delete the winery
            wineryRepository.delete(existingWinery);
        } catch (ResourceNotFoundException ex) {
            // If a ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while deleting Winery", ex);
        }
    }

    // Method to convert a Winery entity to a WineryDTO
    @Override
    public WineryDTO convertWineryToWineryDTO(Winery winery) {
        try {
            WineryDTO wineryDTO = new WineryDTO();
            wineryDTO.setId(winery.getId());
            wineryDTO.setName(winery.getName());
            return wineryDTO;
        } catch (Exception ex) {
            // If any exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while converting Winery to WineryDTO", ex);
        }
    }

    // Method to convert a WineryDTO to a Winery entity
    @Override
    public Winery convertWineryDTOToWinery(WineryDTO wineryDTO) {
        try {
            Winery winery = new Winery();
            winery.setId(wineryDTO.getId());
            winery.setName(wineryDTO.getName());
            return winery;
        } catch (Exception ex) {
            // If any exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while converting WineryDTO to Winery", ex);
        }
    }
}

