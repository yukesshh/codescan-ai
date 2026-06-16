package com.codereview.controller;

import com.codereview.model.CodeReview;
import com.codereview.model.ReviewIssue;
import com.codereview.service.CodeReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class CodeReviewController {

    @Autowired
    private CodeReviewService codeReviewService;

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody Map<String, String> body, Authentication auth) {
        try {
            String title = body.get("title");
            String language = body.get("language");
            String code = body.get("code");

            if (title == null || language == null || code == null || code.isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "title, language, and code are required"));

            CodeReview review = codeReviewService.createReview(auth.getName(), title, language, code);
            return ResponseEntity.ok(toMap(review));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserReviews(Authentication auth) {
        List<CodeReview> reviews = codeReviewService.getUserReviews(auth.getName());
        List<Map<String, Object>> result = reviews.stream().map(this::toSummaryMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReview(@PathVariable Long id, Authentication auth) {
        try {
            CodeReview review = codeReviewService.getReviewById(id, auth.getName());
            return ResponseEntity.ok(toMap(review));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id, Authentication auth) {
        try {
            codeReviewService.deleteReview(id, auth.getName());
            return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication auth) {
        Map<String, Object> stats = codeReviewService.getDashboardStats(auth.getName());
        return ResponseEntity.ok(stats);
    }

    // ---- Mappers ----
    private Map<String, Object> toMap(CodeReview r) {
    List<Map<String, Object>> issues = r.getIssues() == null ? List.of() :
        r.getIssues().stream().map(this::issueToMap).collect(Collectors.toList());

    Map<String, Object> map = new java.util.HashMap<>();
    map.put("id", r.getId());
    map.put("title", r.getTitle());
    map.put("language", r.getLanguage());
    map.put("originalCode", r.getOriginalCode());
    map.put("aiFeedback", r.getAiFeedback() != null ? r.getAiFeedback() : "");
    map.put("overallRating", r.getOverallRating() != null ? r.getOverallRating() : 0);
    map.put("complexityScore", r.getComplexityScore() != null ? r.getComplexityScore() : 0);
    map.put("complexityLevel", r.getComplexityLevel() != null ? r.getComplexityLevel() : "N/A");
    map.put("bugsFound", r.getBugsFound() != null ? r.getBugsFound() : 0);
    map.put("suggestionsCount", r.getSuggestionsCount() != null ? r.getSuggestionsCount() : 0);
    map.put("linesOfCode", r.getLinesOfCode() != null ? r.getLinesOfCode() : 0);
    map.put("status", r.getStatus().name());
    map.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : "");
    map.put("issues", issues);
    return map;
}

    private Map<String, Object> toSummaryMap(CodeReview r) {
        return Map.of(
            "id", r.getId(),
            "title", r.getTitle(),
            "language", r.getLanguage(),
            "overallRating", r.getOverallRating() != null ? r.getOverallRating() : 0,
            "complexityLevel", r.getComplexityLevel() != null ? r.getComplexityLevel() : "N/A",
            "bugsFound", r.getBugsFound() != null ? r.getBugsFound() : 0,
            "linesOfCode", r.getLinesOfCode() != null ? r.getLinesOfCode() : 0,
            "status", r.getStatus().name(),
            "createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : ""
        );
    }

    private Map<String, Object> issueToMap(ReviewIssue i) {
        return Map.of(
            "id", i.getId(),
            "issueType", i.getIssueType().name(),
            "severity", i.getSeverity().name(),
            "lineNumber", i.getLineNumber() != null ? i.getLineNumber() : 0,
            "description", i.getDescription() != null ? i.getDescription() : "",
            "suggestion", i.getSuggestion() != null ? i.getSuggestion() : ""
        );
    }
}
