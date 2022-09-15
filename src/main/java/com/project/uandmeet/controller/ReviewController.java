package com.project.uandmeet.controller;

import com.project.uandmeet.dto.*;
import com.project.uandmeet.security.UserDetailsImpl;
import com.project.uandmeet.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    // review 작성 페이지
    @GetMapping("/api/review/{id}")
    public ResponseEntity<ReviewResponseDto> Review(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                    @PathVariable("id") Long id) {

        return ResponseEntity.ok(reviewService.review(userDetails, id));
    }

    //review 작성
    @PostMapping("/api/review")
    public ResponseEntity<ReviewDto> createReview(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @RequestBody ReviewRequestDto requestDto) throws ParseException {
        return ResponseEntity.ok(reviewService.createReview(userDetails, requestDto));
    }
}
