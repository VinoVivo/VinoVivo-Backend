package com.mscommerce.service.implementation;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.type.TypeDTO;
import com.mscommerce.models.Type;
import com.mscommerce.repositories.jpa.TypeRepository;
import com.mscommerce.service.ITypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TypeServiceImpl implements ITypeService {

    private final TypeRepository typeRepository;

    /**
     * Fetches all types.
     * @return List of all types.
     */
    @Override
    public List<TypeDTO> getAllTypes() throws ResourceNotFoundException {
        // Fetch all types from the repository
        List<Type> types = typeRepository.findAll();

        // Convert the list of types to a list of DTOs and return
        return types.stream()
                .map(this::convertTypeToTypeDTO)
                .collect(Collectors.toList());
    }

    /**
     * Fetches a type by ID.
     * @param typeId ID of the type.
     * @return The fetched type.
     */
    @Override
    public TypeDTO getTypeById(Integer typeId) throws ResourceNotFoundException {
        // Fetch the type from the repository by ID
        Optional<Type> typeOptional = typeRepository.findById(typeId);

        if (typeOptional.isEmpty()) {
            // If the type is not found, throw a ResourceNotFoundException
            throw new ResourceNotFoundException("Type not found with ID: " + typeId);
        }

        // Convert the type to a DTO and return
        Type type = typeOptional.get();
        return convertTypeToTypeDTO(type);
    }

    /**
     * Creates a new type.
     * @param typeDTO Contains the new type details.
     * @return The created type.
     */
    @Override
    public TypeDTO createType(TypeDTO typeDTO) throws BadRequestException {
        // Validate the input DTO
        validateTypeDTO(typeDTO);

        // Convert the DTO to a Type entity
        Type type = convertTypeDTOToType(typeDTO);

        // Save the type in the repository
        Type savedType = typeRepository.save(type);

        // Set the ID of the DTO and return
        typeDTO.setId(savedType.getId());
        return typeDTO;
    }

    /**
     * Updates an existing type.
     * @param typeDTO Contains the updated type details.
     * @return The updated type.
     */
    @Override
    public TypeDTO updateType(TypeDTO typeDTO) throws BadRequestException, ResourceNotFoundException {
        // Validate the input DTO
        validateTypeDTO(typeDTO);

        // Check if the type exists
        Type existingType = typeRepository.findById(typeDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Type not found with ID: " + typeDTO.getId()));

        // Update the name of the existing type
        existingType.setName(typeDTO.getName());

        // Save the updated type
        Type savedType = typeRepository.save(existingType);

        // Convert the updated type to DTO and return
        return convertTypeToTypeDTO(savedType);
    }

    /**
     * Deletes a specific type.
     * @param typeId ID of the type to be deleted.
     */
    @Override
    public void deleteType(Integer typeId) throws ResourceNotFoundException {
        // Check if the type exists
        Type existingType = typeRepository.findById(typeId)
                .orElseThrow(() -> new ResourceNotFoundException("Type not found with ID: " + typeId));

        // Delete the type
        typeRepository.delete(existingType);
    }

    /**
     * Converts a DTO to a Type entity.
     * @param typeDTO DTO to be converted.
     * @return Converted Type entity.
     */
    private TypeDTO convertTypeToTypeDTO(Type type) {
            TypeDTO typeDTO = new TypeDTO();
            typeDTO.setId(type.getId());
            typeDTO.setName(type.getName());
            return typeDTO;
    }

    /**
     * Converts a Type to a DTO.
     * @param type Type to be converted.
     * @return Converted DTO.
     */
    private Type convertTypeDTOToType(TypeDTO typeDTO) {
            Type type = new Type();
            type.setId(typeDTO.getId());
            type.setName(typeDTO.getName());
            return type;
    }

    // Validation methods
    /**
     * Validates a TypeDTO.
     * @param typeDTO DTO to be validated.
     */
    private void validateTypeDTO(TypeDTO typeDTO) throws BadRequestException {
        if (typeDTO.getName() == null || typeDTO.getName().trim().isEmpty()) {
            throw new BadRequestException("Type name must not be null or empty");
        }
    }
}
