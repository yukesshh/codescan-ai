package com.codereview.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CodeReview> codeReviews;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    // Getters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Integer getTotalReviews() { return totalReviews; }
    public List<CodeReview> getCodeReviews() { return codeReviews; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }
    public void setCodeReviews(List<CodeReview> codeReviews) { this.codeReviews = codeReviews; }

    // Builder
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final User u = new User();
        public Builder username(String v) { u.username = v; return this; }
        public Builder email(String v) { u.email = v; return this; }
        public Builder password(String v) { u.password = v; return this; }
        public Builder fullName(String v) { u.fullName = v; return this; }
        public User build() { return u; }
    }
}