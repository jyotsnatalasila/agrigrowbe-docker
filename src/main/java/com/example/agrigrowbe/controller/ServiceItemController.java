package com.example.agrigrowbe.controller;

import com.example.agrigrowbe.model.ServiceItem;
import com.example.agrigrowbe.repository.ServiceItemRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "http://localhost:4000")
public class ServiceItemController {
    private final ServiceItemRepository serviceItemRepository;

    public ServiceItemController(ServiceItemRepository serviceItemRepository){
        this.serviceItemRepository = serviceItemRepository;
    }

    @GetMapping
    public List<ServiceItem> getAllServices() {
        return serviceItemRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<ServiceItem> getServiceById(@PathVariable Long id) {
        return serviceItemRepository.findById(id);
    }
}
