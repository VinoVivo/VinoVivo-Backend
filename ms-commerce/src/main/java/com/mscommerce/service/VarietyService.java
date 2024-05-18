package com.mscommerce.service;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.VarietyDTO;
import com.mscommerce.models.Variety;
import com.mscommerce.repositories.VarietyRepository;
import com.mscommerce.repositories.implementation.IVarietyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VarietyService implements IVarietyRepository {

    private final VarietyRepository varietyRepository;

    // Method to retrieve all varieties
    @Override
    public List<VarietyDTO> getAllVarieties() throws ResourceNotFoundException {
        try {
            // Retrieve all varieties from the repository
            List<Variety> varieties = varietyRepository.findAll();
            // Convert the list of varieties to a list of DTOs and return
            return varieties.stream()
                    .map(this::convertVarietyToVarietyDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            // If an exception occurs, throw a ResourceNotFoundException
            throw new ResourceNotFoundException("Failed to fetch varieties");
        }
    }

    // Method to retrieve a variety by its ID
    @Override
    public VarietyDTO getVarietyById(Integer varietyId) throws ResourceNotFoundException {
        try {
            // Retrieve the variety from the repository by ID
            Optional<Variety> varietyOptional = varietyRepository.findById(varietyId);
            if (varietyOptional.isEmpty()) {
                // If the variety is not found, throw a ResourceNotFoundException
                throw new ResourceNotFoundException("Variety not found with ID: " + varietyId);
            }
            // Convert the variety to a DTO and return
            Variety variety = varietyOptional.get();
            return convertVarietyToVarietyDTO(variety);
        } catch (ResourceNotFoundException ex) {
            // If a ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while getting Variety by ID", ex);
        }
    }

    // Method to create a new variety
    @Override
    public VarietyDTO createVariety(VarietyDTO varietyDTO) throws BadRequestException {
        try {
            // Convert the DTO to a Variety entity
            Variety variety = convertVarietyDTOToVariety(varietyDTO);
            // Save the variety in the repository
            Variety savedVariety = varietyRepository.save(variety);
            // Set the ID of the DTO and return
            varietyDTO.setId(savedVariety.getId());
            return varietyDTO;
        } catch (Exception ex) {
            // If an exception occurs, throw a BadRequestException
            throw new BadRequestException("The received request does not have the correct format.");
        }
    }

    // Method to update an existing variety
    @Override
    public VarietyDTO updateVariety(VarietyDTO varietyDTO) throws ResourceNotFoundException {
        try {
            // Check if the variety exists
            Variety existingVariety = varietyRepository.findById(varietyDTO.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variety not found with ID: " + varietyDTO.getId()));
            // Update the name of the existing variety
            existingVariety.setName(varietyDTO.getName());
            // Save the updated variety
            Variety savedVariety = varietyRepository.save(existingVariety);
            // Convert the updated variety to DTO and return
            return convertVarietyToVarietyDTO(savedVariety);
        } catch (ResourceNotFoundException ex) {
            // If a ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while updating Variety", ex);
        }
    }

    // Method to delete a variety
    @Override
    public void deleteVariety(Integer varietyId) throws ResourceNotFoundException {
        try {
            // Check if the variety exists
            Variety existingVariety = varietyRepository.findById(varietyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Variety not found with ID: " + varietyId));
            // Delete the variety
            varietyRepository.delete(existingVariety);
        } catch (ResourceNotFoundException ex) {
            // If a ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while deleting Variety", ex);
        }
    }

    // Method to convert a Variety entity to a VarietyDTO
    @Override
    public VarietyDTO convertVarietyToVarietyDTO(Variety variety) {
        try {
            VarietyDTO varietyDTO = new VarietyDTO();
            varietyDTO.setId(variety.getId());
            varietyDTO.setName(variety.getName());
            return varietyDTO;
        } catch (Exception ex) {
            // If any exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while converting Variety to VarietyDTO", ex);
        }
    }

    // Method to convert a VarietyDTO to a Variety entity
    @Override
    public Variety convertVarietyDTOToVariety(VarietyDTO varietyDTO) {
        try {
            Variety variety = new Variety();
            variety.setId(varietyDTO.getId());
            variety.setName(varietyDTO.getName());
            return variety;
        } catch (Exception ex) {
            // If any exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while converting VarietyDTO to Variety", ex);
        }
    }
}
