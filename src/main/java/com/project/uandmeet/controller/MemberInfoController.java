package com.project.uandmeet.controller;

import com.project.uandmeet.dto.*;
import com.project.uandmeet.model.Review;
import com.project.uandmeet.security.UserDetailsImpl;
import com.project.uandmeet.service.MemberInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class MemberInfoController {
    private final MemberInfoService memberInfoService;
    // 활동페이지 조회
    @GetMapping("/api/mypage/action/{nickname}")
    public ResponseEntity<MypageDto> action(@PathVariable("nickname") String nickname) {
        return ResponseEntity.ok(memberInfoService.action(nickname));
    }

    // 활동페이지 -> nickname 수정
    @PutMapping("/api/mypage/actionedit/nickname")
    public ResponseEntity<MypageDto> nicknameedit(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @RequestBody NicknameDto requestDto) {
        return ResponseEntity.ok(memberInfoService.nicknameedit(userDetails, requestDto.getNickname()));
    }

    // 활동페이지 -> concern 수정
    @PutMapping("/api/mypage/actionedit/concern")
    public ResponseEntity<MypageDto> concernedit(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                 @RequestBody ConcernDto requestDto) {
        return ResponseEntity.ok(memberInfoService.concernedit(userDetails,
                requestDto.getConcerns()));
    }

    //myInfo 페이지
    @GetMapping("/api/mypage/info")
    public ResponseEntity<MyPageInfoDto> myinfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(memberInfoService.myinfo(userDetails));
    }

    // myinfo -> gender 수정
    @PutMapping("/api/mypage/infoedit/gender")
    public ResponseEntity<MyPageInfoDto> genderedit(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                    @RequestBody InfogenderDto requestDto) {
        return ResponseEntity.ok(memberInfoService.genderedit(userDetails, requestDto));
    }

    // myinfo -> birth 수정
    @PutMapping("/api/mypage/infoedit/birth")
    public ResponseEntity<MyPageInfoDto> birthedit(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                   @RequestBody InfoeditRequestDto requestDto) {
        return ResponseEntity.ok(memberInfoService.birthedit(userDetails, requestDto));
    }

    // profile 조회
    @GetMapping("/api/mypage/profile/{nickname}")
    public ResponseEntity<ProfileDto> profile(@PathVariable("nickname") String nickname) {
        return ResponseEntity.ok(memberInfoService.profile(nickname));
    }

    // profile 수정
    @PutMapping("/api/mypage/profile")
    public ResponseEntity<ProfileDto> profileedit(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @ModelAttribute ProfileEditRequestDto requestDto) throws IOException {
        return ResponseEntity.ok(memberInfoService.profileedit(userDetails, requestDto));
    }

    // 매칭 간단평가
    @GetMapping("/api/userinfo/simplereview/{nickname}")
    public ResponseEntity<SimpleReviewResponseDto> simpleReview(@PathVariable("nickname") String nickname) {
        return ResponseEntity.ok(memberInfoService.simpleReview(nickname));
    }

    // 매칭 후기
    @GetMapping("/api/userinfo/review/{nickname}")
    public ResponseEntity<List<String>> Review(@PathVariable("nickname") String nickname,
                                                @RequestParam int page,
                                                @RequestParam int amount) {
        return ResponseEntity.ok(memberInfoService.Review(nickname, page, amount));
    }
}
