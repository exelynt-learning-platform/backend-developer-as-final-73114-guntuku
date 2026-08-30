package com.booking.resourcebooking.service;

import com.booking.resourcebooking.dto.ResourceRequest;
import com.booking.resourcebooking.dto.ResourceResponse;
import com.booking.resourcebooking.exception.BadRequestException;
import com.booking.resourcebooking.exception.ResourceNotFoundException;
import com.booking.resourcebooking.model.Resource;
import com.booking.resourcebooking.model.ResourceType;
import com.booking.resourcebooking.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;

    @Override
    @Transactional
    public ResourceResponse createResource(ResourceRequest request) {
        ResourceType resourceType = parseResourceType(request.getType());

        Resource resource = Resource.builder()
                .name(request.getName())
                .type(resourceType)
                .description(request.getDescription())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .pricePerHour(request.getPricePerHour())
                .available(request.getAvailable() != null ? request.getAvailable() : true)
                .build();

        Resource saved = resourceRepository.save(resource);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));
        return mapToResponse(resource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getAllResources(Boolean availableOnly) {
        List<Resource> resources;
        if (Boolean.TRUE.equals(availableOnly)) {
            resources = resourceRepository.findByAvailable(true);
        } else {
            resources = resourceRepository.findAll();
        }
        return resources.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ResourceResponse updateResource(Long id, ResourceRequest request) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));

        ResourceType resourceType = parseResourceType(request.getType());

        resource.setName(request.getName());
        resource.setType(resourceType);
        resource.setDescription(request.getDescription());
        resource.setLocation(request.getLocation());
        resource.setCapacity(request.getCapacity());
        resource.setPricePerHour(request.getPricePerHour());
        if (request.getAvailable() != null) {
            resource.setAvailable(request.getAvailable());
        }

        Resource updated = resourceRepository.save(resource);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteResource(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));
        resourceRepository.delete(resource);
    }

    private ResourceType parseResourceType(String typeStr) {
        if (typeStr == null || typeStr.trim().isEmpty()) {
            throw new BadRequestException("Resource type is required");
        }
        try {
            return ResourceType.valueOf(typeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid resource type: " + typeStr);
        }
    }

    private ResourceResponse mapToResponse(Resource resource) {
        return ResourceResponse.builder()
                .id(resource.getId())
                .name(resource.getName())
                .type(resource.getType() != null ? resource.getType().name() : null)
                .description(resource.getDescription())
                .location(resource.getLocation())
                .capacity(resource.getCapacity())
                .pricePerHour(resource.getPricePerHour())
                .available(resource.getAvailable())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();
    }
}
