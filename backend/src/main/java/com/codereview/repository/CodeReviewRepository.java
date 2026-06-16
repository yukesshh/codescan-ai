package com.codereview.repository;

import com.codereview.model.CodeReview;
import com.codereview.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CodeReviewRepository extends JpaRepository<CodeReview, Long> {
    List<CodeReview> findByUserOrderByCreatedAtDesc(User user);
    List<CodeReview> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT AVG(cr.overallRating) FROM CodeReview cr WHERE cr.user.id = :userId AND cr.overallRating IS NOT NULL")
    Double findAverageRatingByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(cr) FROM CodeReview cr WHERE cr.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT cr.language, COUNT(cr) FROM CodeReview cr WHERE cr.user.id = :userId GROUP BY cr.language")
    List<Object[]> findLanguageStatsByUserId(@Param("userId") Long userId);
}
