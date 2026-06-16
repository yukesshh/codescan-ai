package com.codereview.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "code_reviews")
public class CodeReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "original_code", columnDefinition = "LONGTEXT", nullable = false)
    private String originalCode;

    @Column(name = "ai_feedback", columnDefinition = "LONGTEXT")
    private String aiFeedback;

    @Column(name = "overall_rating")
    private Double overallRating;

    @Column(name = "complexity_score")
    private Integer complexityScore;

    @Column(name = "complexity_level")
    private String complexityLevel;

    @Column(name = "bugs_found")
    private Integer bugsFound;

    @Column(name = "suggestions_count")
    private Integer suggestionsCount;

    @Column(name = "lines_of_code")
    private Integer linesOfCode;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "codeReview", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewIssue> issues;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public enum ReviewStatus { PENDING, PROCESSING, COMPLETED, FAILED }

    // Getters
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getTitle() { return title; }
    public String getLanguage() { return language; }
    public String getOriginalCode() { return originalCode; }
    public String getAiFeedback() { return aiFeedback; }
    public Double getOverallRating() { return overallRating; }
    public Integer getComplexityScore() { return complexityScore; }
    public String getComplexityLevel() { return complexityLevel; }
    public Integer getBugsFound() { return bugsFound; }
    public Integer getSuggestionsCount() { return suggestionsCount; }
    public Integer getLinesOfCode() { return linesOfCode; }
    public ReviewStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<ReviewIssue> getIssues() { return issues; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setTitle(String title) { this.title = title; }
    public void setLanguage(String language) { this.language = language; }
    public void setOriginalCode(String originalCode) { this.originalCode = originalCode; }
    public void setAiFeedback(String aiFeedback) { this.aiFeedback = aiFeedback; }
    public void setOverallRating(Double overallRating) { this.overallRating = overallRating; }
    public void setComplexityScore(Integer complexityScore) { this.complexityScore = complexityScore; }
    public void setComplexityLevel(String complexityLevel) { this.complexityLevel = complexityLevel; }
    public void setBugsFound(Integer bugsFound) { this.bugsFound = bugsFound; }
    public void setSuggestionsCount(Integer suggestionsCount) { this.suggestionsCount = suggestionsCount; }
    public void setLinesOfCode(Integer linesOfCode) { this.linesOfCode = linesOfCode; }
    public void setStatus(ReviewStatus status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setIssues(List<ReviewIssue> issues) { this.issues = issues; }

    // Builder
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final CodeReview r = new CodeReview();
        public Builder user(User v) { r.user = v; return this; }
        public Builder title(String v) { r.title = v; return this; }
        public Builder language(String v) { r.language = v; return this; }
        public Builder originalCode(String v) { r.originalCode = v; return this; }
        public Builder linesOfCode(Integer v) { r.linesOfCode = v; return this; }
        public Builder status(ReviewStatus v) { r.status = v; return this; }
        public CodeReview build() { return r; }
    }
}