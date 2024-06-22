package com.meze.repository;

import com.meze.domains.Review;
import com.meze.domains.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review,Long>{


    Page<Review> findAll(Pageable pageable);

    Page<Review> findAllReviewByUser(User user, Pageable pageable );

    Optional<Review> findByIdAndUser(Long id, User user);

    Page<Review> findReviewByProductId(Long productId, Pageable pageable);

    Page<Review> findReviewByUserId(Long userId, Pageable pageable );


}
