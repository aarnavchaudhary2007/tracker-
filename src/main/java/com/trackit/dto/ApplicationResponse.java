package com.trackit.dto;

import com.trackit.model.Application;
import com.trackit.model.ApplicationStatus;
import com.trackit.model.ApplicationType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ApplicationResponse {

    private Long id;
    private String role;
    private String company;
    private ApplicationType type;
    private ApplicationStatus status;
    private LocalDate dateApplied;
    private String notes;
    private String location;
    private String salaryRange;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Static factory method to build from entity
    public static ApplicationResponse from(Application app) {
        ApplicationResponse res = new ApplicationResponse();
        res.id = app.getId();
        res.role = app.getRole();
        res.company = app.getCompany();
        res.type = app.getType();
        res.status = app.getStatus();
        res.dateApplied = app.getDateApplied();
        res.notes = app.getNotes();
        res.location = app.getLocation();
        res.salaryRange = app.getSalaryRange();
        res.createdAt = app.getCreatedAt();
        res.updatedAt = app.getUpdatedAt();
        return res;
    }

    // ---- Getters ----

    public Long getId() { return id; }
    public String getRole() { return role; }
    public String getCompany() { return company; }
    public ApplicationType getType() { return type; }
    public ApplicationStatus getStatus() { return status; }
    public LocalDate getDateApplied() { return dateApplied; }
    public String getNotes() { return notes; }
    public String getLocation() { return location; }
    public String getSalaryRange() { return salaryRange; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
