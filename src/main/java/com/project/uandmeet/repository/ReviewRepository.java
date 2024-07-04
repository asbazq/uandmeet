package com.project.uandmeet.repository;

import com.project.uandmeet.model.Review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findAllById(Long memberId, Pageable pageable);
    Long countByTo(Long to);
    Long countByToAndNum(Long to, int i);
    List<Review> findByTo(Long to);
    boolean existsByFromAndBoardId(Long form, Long boardId);
}