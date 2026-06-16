package com.codereview.repository;

import com.codereview.model.ReviewIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewIssueRepository extends JpaRepository<ReviewIssue, Long> {
    List<ReviewIssue> findByCodeReviewId(Long reviewId);
}
