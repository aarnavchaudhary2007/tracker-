package com.trackit.service;

import com.trackit.dto.ApplicationRequest;
import com.trackit.dto.ApplicationResponse;
import com.trackit.dto.StatsResponse;
import com.trackit.exception.ResourceNotFoundException;
import com.trackit.model.Application;
import com.trackit.model.ApplicationStatus;
import com.trackit.model.ApplicationType;
import com.trackit.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final ApplicationRepository repository;

    @Autowired
    public ApplicationService(ApplicationRepository repository) {
        this.repository = repository;
    }

    // ---- Get all (with optional filters) ----

    public List<ApplicationResponse> getAll(String status, String type, String search) {
        List<Application> results;

        if (search != null && !search.isBlank()) {
            results = repository.searchByRoleOrCompany(search.trim());
        } else if (status != null && type != null) {
            ApplicationStatus appStatus = parseStatus(status);
            ApplicationType appType = parseType(type);
            results = repository.findByStatusAndTypeOrderByDateAppliedDesc(appStatus, appType);
        } else if (status != null) {
            ApplicationStatus appStatus = parseStatus(status);
            results = repository.findByStatusOrderByDateAppliedDesc(appStatus);
        } else if (type != null) {
            ApplicationType appType = parseType(type);
            results = repository.findByTypeOrderByDateAppliedDesc(appType);
        } else {
            results = repository.findAllByOrderByDateAppliedDesc();
        }

        return results.stream()
                .map(ApplicationResponse::from)
                .collect(Collectors.toList());
    }

    // ---- Get by ID ----

    public ApplicationResponse getById(Long id) {
        Application app = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application", id));
        return ApplicationResponse.from(app);
    }

    // ---- Create ----

    public ApplicationResponse create(ApplicationRequest request) {
        Application app = new Application();
        mapRequestToEntity(request, app);
        Application saved = repository.save(app);
        return ApplicationResponse.from(saved);
    }

    // ---- Update (full replace) ----

    public ApplicationResponse update(Long id, ApplicationRequest request) {
        Application app = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application", id));
        mapRequestToEntity(request, app);
        Application saved = repository.save(app);
        return ApplicationResponse.from(saved);
    }

    // ---- Patch status only ----

    public ApplicationResponse updateStatus(Long id, String status) {
        Application app = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application", id));
        app.setStatus(parseStatus(status));
        Application saved = repository.save(app);
        return ApplicationResponse.from(saved);
    }

    // ---- Delete ----

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Application", id);
        }
        repository.deleteById(id);
    }

    // ---- Stats ----

    public StatsResponse getStats() {
        long total = repository.count();
        long applied = repository.countByStatus(ApplicationStatus.APPLIED);
        long interview = repository.countByStatus(ApplicationStatus.INTERVIEW);
        long offer = repository.countByStatus(ApplicationStatus.OFFER);
        long rejected = repository.countByStatus(ApplicationStatus.REJECTED);
        long internships = repository.countByType(ApplicationType.INTERNSHIP);
        long jobs = repository.countByType(ApplicationType.JOB);

        return new StatsResponse(total, applied, interview, offer, rejected, internships, jobs);
    }

    // ---- Private helpers ----

    private void mapRequestToEntity(ApplicationRequest request, Application app) {
        app.setRole(request.getRole());
        app.setCompany(request.getCompany());
        app.setType(request.getType());
        app.setStatus(request.getStatus());
        app.setDateApplied(request.getDateApplied());
        app.setNotes(request.getNotes());
        app.setLocation(request.getLocation());
        app.setSalaryRange(request.getSalaryRange());
    }

    private ApplicationStatus parseStatus(String status) {
        try {
            return ApplicationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid status: '" + status + "'. Valid values: APPLIED, INTERVIEW, OFFER, REJECTED");
        }
    }

    private ApplicationType parseType(String type) {
        try {
            return ApplicationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid type: '" + type + "'. Valid values: INTERNSHIP, JOB");
        }
    }
}
