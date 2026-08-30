package com.booking.resourcebooking.service;

import com.booking.resourcebooking.dto.ResourceRequest;
import com.booking.resourcebooking.dto.ResourceResponse;

import java.util.List;

public interface ResourceService {
    ResourceResponse createResource(ResourceRequest resourceRequest);
    ResourceResponse getResourceById(Long id);
    List<ResourceResponse> getAllResources(Boolean availableOnly);
    ResourceResponse updateResource(Long id, ResourceRequest resourceRequest);
    void deleteResource(Long id);
}
