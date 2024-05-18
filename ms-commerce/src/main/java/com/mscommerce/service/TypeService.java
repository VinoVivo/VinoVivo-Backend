package com.mscommerce.service;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.TypeDTO;
import com.mscommerce.models.Type;
import com.mscommerce.repositories.TypeRepository;
import com.mscommerce.repositories.implementation.ITypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TypeService implements ITypeRepository {

    private final TypeRepository typeRepository;

    // Method to retrieve all types
    @Override
    public List<TypeDTO> getAllTypes() throws ResourceNotFoundException {
        try {
            // Retrieve all types from the repository
            List<Type> types = typeRepository.findAll();
            // Convert the list of types to a list of DTOs and return
            return types.stream()
                    .map(this::convertTypeToTypeDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            // If an exception occurs, throw a ResourceNotFoundException
            throw new ResourceNotFoundException("Failed to fetch types");
        }
    }

    // Method to retrieve a type by its ID
    @Override
    public TypeDTO getTypeById(Integer typeId) throws ResourceNotFoundException {
        try {
            // Retrieve the type from the repository by ID
            Optional<Type> typeOptional = typeRepository.findById(typeId);
            if (typeOptional.isEmpty()) {
                // If the type is not found, throw a ResourceNotFoundException
                throw new ResourceNotFoundException("Type not found with ID: " + typeId);
            }
            // Convert the type to a DTO and return
            Type type = typeOptional.get();
            return convertTypeToTypeDTO(type);
        } catch (ResourceNotFoundException ex) {
            // If a ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while getting Type by ID", ex);
        }
    }

    // Method to create a new type
    @Override
    public TypeDTO createType(TypeDTO typeDTO) throws BadRequestException {
        try {
            // Convert the DTO to a Type entity
            Type type = convertTypeDTOToType(typeDTO);
            // Save the type in the repository
            Type savedType = typeRepository.save(type);
            // Set the ID of the DTO and return
            typeDTO.setId(savedType.getId());
            return typeDTO;
        } catch (Exception ex) {
            // If an exception occurs, throw a BadRequestException
            throw new BadRequestException("The received request does not have the correct format.");
        }
    }

    // Method to update an existing type
    @Override
    public TypeDTO updateType(TypeDTO typeDTO) throws ResourceNotFoundException {
        try {
            // Check if the type exists
            Type existingType = typeRepository.findById(typeDTO.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Type not found with ID: " + typeDTO.getId()));
            // Update the name of the existing type
            existingType.setName(typeDTO.getName());
            // Save the updated type
            Type savedType = typeRepository.save(existingType);
            // Convert the updated type to DTO and return
            return convertTypeToTypeDTO(savedType);
        } catch (ResourceNotFoundException ex) {
            // If a ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while updating Type", ex);
        }
    }

    // Method to delete a type
    @Override
    public void deleteType(Integer typeId) throws ResourceNotFoundException {
        try {
            // Check if the type exists
            Type existingType = typeRepository.findById(typeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Type not found with ID: " + typeId));
            // Delete the type
            typeRepository.delete(existingType);
        } catch (ResourceNotFoundException ex) {
            // If a ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while deleting Type", ex);
        }
    }

    // Method to convert a Type entity to a TypeDTO
    @Override
    public TypeDTO convertTypeToTypeDTO(Type type) {
        try {
            TypeDTO typeDTO = new TypeDTO();
            typeDTO.setId(type.getId());
            typeDTO.setName(type.getName());
            return typeDTO;
        } catch (Exception ex) {
            // If any exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while converting Type to TypeDTO", ex);
        }
    }

    // Method to convert a TypeDTO to a Type entity
    @Override
    public Type convertTypeDTOToType(TypeDTO typeDTO) {
        try {
            Type type = new Type();
            type.setId(typeDTO.getId());
            type.setName(typeDTO.getName());
            return type;
        } catch (Exception ex) {
            // If any exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while converting TypeDTO to Type", ex);
        }
    }
}
