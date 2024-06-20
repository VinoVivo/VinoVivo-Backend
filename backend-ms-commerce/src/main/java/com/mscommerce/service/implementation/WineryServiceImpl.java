package com.mscommerce.service.implementation;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.winery.WineryDTO;
import com.mscommerce.models.Winery;
import com.mscommerce.repositories.jpa.WineryRepository;
import com.mscommerce.service.IWineryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WineryServiceImpl implements IWineryService {

    private final WineryRepository wineryRepository;

    /**
     * Fetches all wineries.
     * @return List of all wineries.
     */
    @Override
    public List<WineryDTO> getAllWineries() {
        // Fetch all wineries from the repository
        List<Winery> wineries = wineryRepository.findAll();

        // Convert the list of wineries to a list of DTOs and return
        return wineries.stream()
                .map(this::convertWineryToWineryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Fetches a winery by ID.
     * @param wineryId ID of the winery.
     * @return The fetched winery.
     */
    @Override
    public WineryDTO getWineryById(Integer wineryId) throws ResourceNotFoundException {
        // Fetch the winery from the repository by ID
        Optional<Winery> wineryOptional = wineryRepository.findById(wineryId);

        if (wineryOptional.isEmpty()) {
            // If the winery is not found, throw a ResourceNotFoundException
            throw new ResourceNotFoundException("Winery not found with ID: " + wineryId);
        }

        // Convert the winery to a DTO and return
        Winery winery = wineryOptional.get();
        return convertWineryToWineryDTO(winery);
    }

    /**
     * Creates a new winery.
     * @param wineryDTO Contains the new winery details.
     * @return The created winery.
     */
    @Override
    public WineryDTO createWinery(WineryDTO wineryDTO) throws BadRequestException {
        // Validate the input DTO
        validateWineryDTO(wineryDTO);

        // Convert the DTO to a Winery entity
        Winery winery = convertWineryDTOToWinery(wineryDTO);

        // Save the winery in the repository
        Winery savedWinery = wineryRepository.save(winery);

        // Set the ID of the DTO and return
        wineryDTO.setId(savedWinery.getId());
        return wineryDTO;
    }

    /**
     * Updates an existing winery.
     * @param wineryDTO Contains the updated winery details.
     * @return The updated winery.
     */
    @Override
    public WineryDTO updateWinery(WineryDTO wineryDTO) throws BadRequestException, ResourceNotFoundException {
        // Validate the input DTO
        validateWineryDTO(wineryDTO);

        // Check if the winery exists
        Winery existingWinery = wineryRepository.findById(wineryDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Winery not found with ID: " + wineryDTO.getId()));

        // Update the name of the existing winery
        existingWinery.setName(wineryDTO.getName());

        // Save the updated winery
        Winery savedWinery = wineryRepository.save(existingWinery);

        // Convert the updated winery to DTO and return
        return convertWineryToWineryDTO(savedWinery);
    }

    /**
     * Deletes a specific winery.
     * @param wineryId ID of the winery to be deleted.
     */
    @Override
    public void deleteWinery(Integer wineryId) throws ResourceNotFoundException {
        // Check if the winery exists
        Winery existingWinery = wineryRepository.findById(wineryId)
                .orElseThrow(() -> new ResourceNotFoundException("Winery not found with ID: " + wineryId));

        // Delete the winery
        wineryRepository.delete(existingWinery);
    }

    /**
     * Converts a DTO to a Winery entity.
     * @param wineryDTO DTO to be converted.
     * @return Converted Winery entity.
     */
    private WineryDTO convertWineryToWineryDTO(Winery winery) {
            WineryDTO wineryDTO = new WineryDTO();
            wineryDTO.setId(winery.getId());
            wineryDTO.setName(winery.getName());
            return wineryDTO;
    }

    /**
     * Converts a Winery to a DTO.
     * @param winery Winery to be converted.
     * @return Converted DTO.
     */
    private Winery convertWineryDTOToWinery(WineryDTO wineryDTO) {
            Winery winery = new Winery();
            winery.setId(wineryDTO.getId());
            winery.setName(wineryDTO.getName());
            return winery;
    }

    // Validation methods
    /**
     * Validates a WineryDTO.
     * @param wineryDTO DTO to be validated.
     */
    private void validateWineryDTO(WineryDTO wineryDTO) throws BadRequestException {
        if (wineryDTO.getName() == null || wineryDTO.getName().trim().isEmpty()) {
            throw new BadRequestException("Winery name must not be null or empty");
        }
    }
}

