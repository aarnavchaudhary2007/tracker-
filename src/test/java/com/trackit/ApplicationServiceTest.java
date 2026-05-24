package com.trackit;

import com.trackit.dto.ApplicationRequest;
import com.trackit.dto.ApplicationResponse;
import com.trackit.dto.StatsResponse;
import com.trackit.exception.ResourceNotFoundException;
import com.trackit.model.Application;
import com.trackit.model.ApplicationStatus;
import com.trackit.model.ApplicationType;
import com.trackit.repository.ApplicationRepository;
import com.trackit.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository repository;

    @InjectMocks
    private ApplicationService service;

    private Application sampleApp;
    private ApplicationRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleApp = new Application(
                "Backend Intern", "Razorpay",
                ApplicationType.INTERNSHIP, ApplicationStatus.APPLIED,
                LocalDate.of(2025, 5, 10)
        );
        sampleApp.setId(1L);

        sampleRequest = new ApplicationRequest();
        sampleRequest.setRole("Backend Intern");
        sampleRequest.setCompany("Razorpay");
        sampleRequest.setType(ApplicationType.INTERNSHIP);
        sampleRequest.setStatus(ApplicationStatus.APPLIED);
        sampleRequest.setDateApplied(LocalDate.of(2025, 5, 10));
    }

    @Test
    void getAll_returnsAllApplications() {
        when(repository.findAllByOrderByDateAppliedDesc()).thenReturn(List.of(sampleApp));
        List<ApplicationResponse> result = service.getAll(null, null, null);
        assertEquals(1, result.size());
        assertEquals("Razorpay", result.get(0).getCompany());
    }

    @Test
    void getById_returnsCorrectApplication() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleApp));
        ApplicationResponse result = service.getById(1L);
        assertEquals("Backend Intern", result.getRole());
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void create_savesAndReturnsApplication() {
        when(repository.save(any(Application.class))).thenReturn(sampleApp);
        ApplicationResponse result = service.create(sampleRequest);
        assertNotNull(result);
        assertEquals("Razorpay", result.getCompany());
        verify(repository, times(1)).save(any(Application.class));
    }

    @Test
    void update_updatesExistingApplication() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleApp));
        when(repository.save(any(Application.class))).thenReturn(sampleApp);
        ApplicationResponse result = service.update(1L, sampleRequest);
        assertNotNull(result);
        verify(repository, times(1)).save(any(Application.class));
    }

    @Test
    void update_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update(99L, sampleRequest));
    }

    @Test
    void updateStatus_changesStatusCorrectly() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleApp));
        when(repository.save(any(Application.class))).thenReturn(sampleApp);
        service.updateStatus(1L, "INTERVIEW");
        verify(repository, times(1)).save(any(Application.class));
    }

    @Test
    void updateStatus_throwsOnInvalidStatus() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleApp));
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(1L, "INVALID"));
    }

    @Test
    void delete_deletesSuccessfully() {
        when(repository.existsById(1L)).thenReturn(true);
        service.delete(1L);
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void delete_throwsWhenNotFound() {
        when(repository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service.delete(99L));
    }

    @Test
    void getStats_returnsCorrectCounts() {
        when(repository.count()).thenReturn(5L);
        when(repository.countByStatus(ApplicationStatus.APPLIED)).thenReturn(2L);
        when(repository.countByStatus(ApplicationStatus.INTERVIEW)).thenReturn(1L);
        when(repository.countByStatus(ApplicationStatus.OFFER)).thenReturn(1L);
        when(repository.countByStatus(ApplicationStatus.REJECTED)).thenReturn(1L);
        when(repository.countByType(ApplicationType.INTERNSHIP)).thenReturn(3L);
        when(repository.countByType(ApplicationType.JOB)).thenReturn(2L);

        StatsResponse stats = service.getStats();
        assertEquals(5L, stats.getTotalApplications());
        assertEquals(2L, stats.getApplied());
        assertEquals(1L, stats.getOffer());
        assertEquals(3L, stats.getTotalInternships());
    }
}
