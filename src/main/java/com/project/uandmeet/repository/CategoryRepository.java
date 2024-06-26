package com.project.uandmeet.repository;

import com.project.uandmeet.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
//    Optional<Category> findByCategory(String category);

    Optional<Category> findAllByCategory(String category);
    boolean existsByCategory(String category);

}