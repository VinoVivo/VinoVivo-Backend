package com.mscommerce.service.implementation;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.variety.VarietyDTO;
import com.mscommerce.models.Variety;
import com.mscommerce.repositories.jpa.VarietyRepository;
import com.mscommerce.service.IVarietyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VarietyServiceImpl implements IVarietyService {

    private final VarietyRepository varietyRepository;

    /**
     * Fetches all varieties.
     * @return List of all varieties.
     */
    @Override
    public List<VarietyDTO> getAllVarieties() {
        // Fetch all varieties from the repository
        List<Variety> varieties = varietyRepository.findAll();

        // Convert the list of varieties to a list of DTOs and return
        return varieties.stream()
                .map(this::convertVarietyToVarietyDTO)
                .collect(Collectors.toList());
    }

    /**
     * Fetches a variety by ID.
     * @param varietyId ID of the variety.
     * @return The fetched variety.
     */
    @Override
    public VarietyDTO getVarietyById(Integer varietyId) throws ResourceNotFoundException {
        // Fetch the variety from the repository by ID
        Optional<Variety> varietyOptional = varietyRepository.findById(varietyId);

        if (varietyOptional.isEmpty()) {
            // If the variety is not found, throw a ResourceNotFoundException
            throw new ResourceNotFoundException("Variety not found with ID: " + varietyId);
        }

        // Convert the variety to a DTO and return
        Variety variety = varietyOptional.get();
        return convertVarietyToVarietyDTO(variety);
    }

    /**
     * Creates a new variety.
     * @param varietyDTO Contains the new variety details.
     * @return The created variety.
     */
    @Override
    public VarietyDTO createVariety(VarietyDTO varietyDTO) throws BadRequestException {
        // Validate the input DTO
        validateVarietyDTO(varietyDTO);

        // Convert the DTO to a Variety entity
        Variety variety = convertVarietyDTOToVariety(varietyDTO);

        // Save the variety in the repository
        Variety savedVariety = varietyRepository.save(variety);

        // Set the ID of the DTO and return
        varietyDTO.setId(savedVariety.getId());
        return varietyDTO;
    }

    /**
     * Updates an existing variety.
     * @param varietyDTO Contains the updated variety details.
     * @return The updated variety.
     */
    @Override
    public VarietyDTO updateVariety(VarietyDTO varietyDTO) throws BadRequestException, ResourceNotFoundException {
        // Validate the input DTO
        validateVarietyDTO(varietyDTO);

        // Check if the variety exists
        Variety existingVariety = varietyRepository.findById(varietyDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Variety not found with ID: " + varietyDTO.getId()));

        // Update the name of the existing variety
        existingVariety.setName(varietyDTO.getName());

        // Save the updated variety
        Variety savedVariety = varietyRepository.save(existingVariety);

        // Convert the updated variety to DTO and return
        return convertVarietyToVarietyDTO(savedVariety);
    }

    /**
     * Deletes a specific variety.
     * @param varietyId ID of the variety to be deleted.
     */
    @Override
    public void deleteVariety(Integer varietyId) throws ResourceNotFoundException {
        // Check if the variety exists
        Variety existingVariety = varietyRepository.findById(varietyId)
                .orElseThrow(() -> new ResourceNotFoundException("Variety not found with ID: " + varietyId));

        // Delete the variety
        varietyRepository.delete(existingVariety);
    }

    /**
     * Converts a DTO to a Variety entity.
     * @param varietyDTO DTO to be converted.
     * @return Converted Variety entity.
     */
    private VarietyDTO convertVarietyToVarietyDTO(Variety variety) {
            VarietyDTO varietyDTO = new VarietyDTO();
            varietyDTO.setId(variety.getId());
            varietyDTO.setName(variety.getName());
            return varietyDTO;
    }

    /**
     * Converts a Variety to a DTO.
     * @param variety Variety to be converted.
     * @return Converted DTO.
     */
    private Variety convertVarietyDTOToVariety(VarietyDTO varietyDTO) {
            Variety variety = new Variety();
            variety.setId(varietyDTO.getId());
            variety.setName(varietyDTO.getName());
            return variety;
    }

    // Validation methods
    /**
     * Validates a VarietyDTO.
     * @param varietyDTO DTO to be validated.
     */
    private void validateVarietyDTO(VarietyDTO varietyDTO) throws BadRequestException {
        if (varietyDTO.getName() == null || varietyDTO.getName().trim().isEmpty()) {
            throw new BadRequestException("Variety name must not be null or empty");
        }
    }
}
