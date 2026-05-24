package com.trackit.dto;

public class StatsResponse {

    private long totalApplications;
    private long applied;
    private long interview;
    private long offer;
    private long rejected;
    private long totalInternships;
    private long totalJobs;

    public StatsResponse() {}

    public StatsResponse(long totalApplications, long applied, long interview,
                         long offer, long rejected, long totalInternships, long totalJobs) {
        this.totalApplications = totalApplications;
        this.applied = applied;
        this.interview = interview;
        this.offer = offer;
        this.rejected = rejected;
        this.totalInternships = totalInternships;
        this.totalJobs = totalJobs;
    }

    // ---- Getters & Setters ----

    public long getTotalApplications() { return totalApplications; }
    public void setTotalApplications(long totalApplications) { this.totalApplications = totalApplications; }

    public long getApplied() { return applied; }
    public void setApplied(long applied) { this.applied = applied; }

    public long getInterview() { return interview; }
    public void setInterview(long interview) { this.interview = interview; }

    public long getOffer() { return offer; }
    public void setOffer(long offer) { this.offer = offer; }

    public long getRejected() { return rejected; }
    public void setRejected(long rejected) { this.rejected = rejected; }

    public long getTotalInternships() { return totalInternships; }
    public void setTotalInternships(long totalInternships) { this.totalInternships = totalInternships; }

    public long getTotalJobs() { return totalJobs; }
    public void setTotalJobs(long totalJobs) { this.totalJobs = totalJobs; }
}
