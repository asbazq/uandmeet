package com.project.uandmeet.api;

import com.project.uandmeet.model.Category;
import com.project.uandmeet.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CategoryInitializer implements CommandLineRunner { // 애플리케이션 시작 시 특정 작업을 수행

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // 애플리케이션 시작 시 호출
    @Override
    public void run(String... args) throws Exception {
        List<String> categories = Arrays.asList("gym", "running", "riding", "badminton", "tennis", "golf", "etc");

        for (String categoryName : categories) {
            if (!categoryRepository.existsByCategory(categoryName)) {
                categoryRepository.save(new Category(categoryName));
            }
        }
    }
}
