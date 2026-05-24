package com.trackit.repository;

import com.trackit.model.Application;
import com.trackit.model.ApplicationStatus;
import com.trackit.model.ApplicationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // Filter by status
    List<Application> findByStatusOrderByDateAppliedDesc(ApplicationStatus status);

    // Filter by type
    List<Application> findByTypeOrderByDateAppliedDesc(ApplicationType type);

    // Filter by status AND type
    List<Application> findByStatusAndTypeOrderByDateAppliedDesc(ApplicationStatus status, ApplicationType type);

    // Search by company name (case-insensitive)
    List<Application> findByCompanyContainingIgnoreCaseOrderByDateAppliedDesc(String company);

    // Search by role (case-insensitive)
    List<Application> findByRoleContainingIgnoreCaseOrderByDateAppliedDesc(String role);

    // Get all sorted by date desc
    List<Application> findAllByOrderByDateAppliedDesc();

    // Applications in a date range
    List<Application> findByDateAppliedBetweenOrderByDateAppliedDesc(LocalDate from, LocalDate to);

    // Count by status
    long countByStatus(ApplicationStatus status);

    // Count by type
    long countByType(ApplicationType type);

    // Stats summary query
    @Query("SELECT a.status, COUNT(a) FROM Application a GROUP BY a.status")
    List<Object[]> countGroupedByStatus();

    // Search across role and company
    @Query("SELECT a FROM Application a WHERE " +
           "LOWER(a.role) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.company) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY a.dateApplied DESC")
    List<Application> searchByRoleOrCompany(@Param("query") String query);
}
