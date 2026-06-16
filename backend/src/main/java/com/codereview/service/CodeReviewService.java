package com.codereview.service;

import com.codereview.model.*;
import com.codereview.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CodeReviewService {

    @Autowired private CodeReviewRepository reviewRepository;
    @Autowired private ReviewIssueRepository issueRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OpenAIService openAIService;
    @Autowired private ObjectMapper objectMapper;

    @Transactional
    public CodeReview createReview(String username, String title, String language, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Save initial review record
        CodeReview review = CodeReview.builder()
                .user(user)
                .title(title)
                .language(language)
                .originalCode(code)
                .linesOfCode(code.split("\n").length)
                .status(CodeReview.ReviewStatus.PROCESSING)
                .build();

        review = reviewRepository.save(review);

        try {
            // Call AI service
            String aiResponse = openAIService.analyzeCode(code, language);
            Map<String, Object> analysisResult = objectMapper.readValue(aiResponse, Map.class);

            // Parse and update review
            review.setAiFeedback(aiResponse);
            review.setOverallRating(parseDouble(analysisResult.get("overallRating")));
            review.setComplexityScore(parseInteger(analysisResult.get("complexityScore")));
            review.setComplexityLevel((String) analysisResult.get("complexityLevel"));
            review.setStatus(CodeReview.ReviewStatus.COMPLETED);

            // Process issues
            List<Map<String, Object>> issuesList = (List<Map<String, Object>>) analysisResult.get("issues");
            int bugsFound = 0;

            if (issuesList != null) {
                review.setSuggestionsCount(issuesList.size());
                List<ReviewIssue> savedIssues = new ArrayList<>();

                for (Map<String, Object> issueData : issuesList) {
                    ReviewIssue issue = ReviewIssue.builder()
                            .codeReview(review)
                            .issueType(parseIssueType((String) issueData.get("issueType")))
                            .severity(parseSeverity((String) issueData.get("severity")))
                            .lineNumber((Integer) issueData.get("lineNumber"))
                            .description((String) issueData.get("description"))
                            .suggestion((String) issueData.get("suggestion"))
                            .build();

                    savedIssues.add(issue);
                    if (issue.getIssueType() == ReviewIssue.IssueType.BUG) bugsFound++;
                }

                issueRepository.saveAll(savedIssues);
                review.setIssues(savedIssues);
            }

            review.setBugsFound(bugsFound);
            review = reviewRepository.save(review);

            // Update user's total reviews
            user.setTotalReviews(user.getTotalReviews() + 1);
            userRepository.save(user);

        } catch (Exception e) {
    e.printStackTrace();
    System.err.println("REVIEW FAILED: " + e.getMessage());
    review.setStatus(CodeReview.ReviewStatus.FAILED);
    review.setAiFeedback("Analysis failed: " + e.getMessage());
    reviewRepository.save(review);
}

        return review;
    }

    public List<CodeReview> getUserReviews(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reviewRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public CodeReview getReviewById(Long id, String username) {
        CodeReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        if (!review.getUser().getUsername().equals(username))
            throw new RuntimeException("Access denied");
        return review;
    }

    public Map<String, Object> getDashboardStats(String username) {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

    Long total = reviewRepository.countByUserId(user.getId());
    Double avgRating = reviewRepository.findAverageRatingByUserId(user.getId());
    List<Object[]> langStats = reviewRepository.findLanguageStatsByUserId(user.getId());

    Map<String, Long> languageBreakdown = new LinkedHashMap<>();
    for (Object[] row : langStats) {
        languageBreakdown.put((String) row[0], (Long) row[1]);
    }

    // Build simple maps for recent reviews to avoid lazy-load issues
    List<Map<String, Object>> recentReviews = reviewRepository
            .findByUserIdOrderByCreatedAtDesc(user.getId())
            .stream()
            .limit(5)
            .map(r -> {
                Map<String, Object> m = new java.util.HashMap<>();
                m.put("id", r.getId());
                m.put("title", r.getTitle());
                m.put("language", r.getLanguage());
                m.put("overallRating", r.getOverallRating() != null ? r.getOverallRating() : 0);
                m.put("complexityLevel", r.getComplexityLevel() != null ? r.getComplexityLevel() : "N/A");
                m.put("bugsFound", r.getBugsFound() != null ? r.getBugsFound() : 0);
                m.put("status", r.getStatus().name());
                m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : "");
                return m;
            })
            .collect(java.util.stream.Collectors.toList());

    Map<String, Object> stats = new HashMap<>();
    stats.put("totalReviews", total);
    stats.put("averageRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0);
    stats.put("languageBreakdown", languageBreakdown);
    stats.put("recentReviews", recentReviews);
    stats.put("username", user.getUsername());
    stats.put("fullName", user.getFullName());
    stats.put("memberSince", user.getCreatedAt());

    return stats;
}

    public void deleteReview(Long id, String username) {
        CodeReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        if (!review.getUser().getUsername().equals(username))
            throw new RuntimeException("Access denied");
        reviewRepository.delete(review);
    }

    // --- Helpers ---
    private Double parseDouble(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return null; }
    }

    private Integer parseInteger(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return null; }
    }

    private ReviewIssue.IssueType parseIssueType(String val) {
        try { return ReviewIssue.IssueType.valueOf(val); } catch (Exception e) { return ReviewIssue.IssueType.CODE_SMELL; }
    }

    private ReviewIssue.Severity parseSeverity(String val) {
        try { return ReviewIssue.Severity.valueOf(val); } catch (Exception e) { return ReviewIssue.Severity.INFO; }
    }
}
