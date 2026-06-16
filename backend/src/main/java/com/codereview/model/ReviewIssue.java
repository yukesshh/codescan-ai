package com.codereview.model;

import jakarta.persistence.*;

@Entity
@Table(name = "review_issues")
public class ReviewIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private CodeReview codeReview;

    @Column(name = "issue_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private IssueType issueType;

    @Column(name = "severity")
    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "suggestion", columnDefinition = "TEXT")
    private String suggestion;

    public enum IssueType { BUG, CODE_SMELL, SECURITY, PERFORMANCE, STYLE, COMPLEXITY, BEST_PRACTICE }
    public enum Severity { CRITICAL, HIGH, MEDIUM, LOW, INFO }

    // Getters
    public Long getId() { return id; }
    public CodeReview getCodeReview() { return codeReview; }
    public IssueType getIssueType() { return issueType; }
    public Severity getSeverity() { return severity; }
    public Integer getLineNumber() { return lineNumber; }
    public String getDescription() { return description; }
    public String getSuggestion() { return suggestion; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setCodeReview(CodeReview codeReview) { this.codeReview = codeReview; }
    public void setIssueType(IssueType issueType) { this.issueType = issueType; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }
    public void setDescription(String description) { this.description = description; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    // Builder
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final ReviewIssue i = new ReviewIssue();
        public Builder codeReview(CodeReview v) { i.codeReview = v; return this; }
        public Builder issueType(IssueType v) { i.issueType = v; return this; }
        public Builder severity(Severity v) { i.severity = v; return this; }
        public Builder lineNumber(Integer v) { i.lineNumber = v; return this; }
        public Builder description(String v) { i.description = v; return this; }
        public Builder suggestion(String v) { i.suggestion = v; return this; }
        public ReviewIssue build() { return i; }
    }
}