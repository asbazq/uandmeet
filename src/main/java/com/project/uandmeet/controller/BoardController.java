package com.project.uandmeet.controller;

import com.project.uandmeet.dto.*;
import com.project.uandmeet.dto.boardDtoGroup.*;
import com.project.uandmeet.dto.boardDtoGroup.LikeDto;
import com.project.uandmeet.dto.commentsDtoGroup.CommentsInquiryDto;
import com.project.uandmeet.dto.commentsDtoGroup.CommentsRequestDto;
import com.project.uandmeet.Exception.CustomException;
import com.project.uandmeet.Exception.ErrorCode;
import com.project.uandmeet.security.UserDetailsImpl;
import com.project.uandmeet.service.BoardService;
import com.project.uandmeet.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final SearchService searchService;

    @PostMapping("/api/categoryset/{category}")
    private CustomException categoryNew(@PathVariable("category") String category) {
        return boardService.categoryNew(category);
    }

    //게시물 작성
    @PostMapping("/api/board/create")
    private ResponseEntity<Long> boardNew(@ModelAttribute BoardRequestDto.createAndCheck boardRequestDto,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @RequestParam(value = "data", required = false) MultipartFile data) throws IOException {
        System.out.println(userDetails.getMember().getUsername());
        return ResponseEntity.ok(boardService.boardNew(boardRequestDto, userDetails, data));
    }

    //매칭 게시물 전체 조회 (카테고리별 전체 조회)
    @GetMapping("/api/boards/matching")
    private ResponseEntity<BoardResponseFinalDto> boardMatchingAllInquiry(
            @RequestParam String cate,          //카테고리
            @RequestParam Integer page,        //페이지번호
            @RequestParam Integer amount,
            @RequestParam String city,        //시
            @RequestParam String gu) {        //군
        BoardResponseFinalDto boardMatchingAllInquiry = boardService.boardMatchingAllInquiry("matching",cate,page,amount,city,gu);

        if (boardMatchingAllInquiry == null) {
            throw new CustomException(ErrorCode.CAN_NOT_CREATE_ROOM);
        } else
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(boardMatchingAllInquiry);
    }

    //매칭 게시물 상세 조회
    @GetMapping("/api/boards/matching/{id}")
    private ResponseEntity<BoardResponseDto> boardChoiceInquiry(@PathVariable("id") Long id) {
        BoardResponseDto boardChoiceInquiry = null;
        boardChoiceInquiry = boardService.boardChoiceInquiry(id);
        if (boardChoiceInquiry == null) {
            throw new CustomException(ErrorCode.CAN_NOT_CREATE_ROOM);
        } else
            return ResponseEntity.ok(boardChoiceInquiry);
    }

    //매칭 개시물 수정
    @PutMapping("/api/board/matching/{id}")
    private CustomException boardUpdate(@PathVariable("id") Long id,
                                        @RequestBody BoardRequestDto.updateMatching boardRequestDto,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return boardService.boardUpdate(id, boardRequestDto, userDetails);
    }

    //공유 게시물 전체 조회 (카테고리별 전체 조회)
    @GetMapping("/api/boards/information")
    private ResponseEntity<BoardResponseFinalDto> boardInfoAllInquiry(
            @RequestParam String cate,
            @RequestParam Integer page,
            @RequestParam Integer amount) {
        BoardResponseFinalDto boardInfoAllInquiry = boardService.boardInfoAllInquiry("information", cate,page,amount);

        if (boardInfoAllInquiry == null) {
            throw new CustomException(ErrorCode.CAN_NOT_CREATE_ROOM);
        } else
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(boardInfoAllInquiry);
    }

    //공유 게시물 상세 조회
    @GetMapping("/api/boards/information/{id}")
    private ResponseEntity<BoardResponseDto> boardChoiceInfoInquiry(@PathVariable("id") Long id) {
        BoardResponseDto boardChoiceInfoInquiry = null;
        boardChoiceInfoInquiry = boardService.boardChoiceInfoInquiry(id);
        if (boardChoiceInfoInquiry == null) {
            throw new CustomException(ErrorCode.CAN_NOT_CREATE_ROOM);
        } else
            return ResponseEntity.ok(boardChoiceInfoInquiry);
    }

    //공유 개시물 수정
    @PutMapping("/api/board/information/{id}")
    private CustomException boardInfoUpdate(@PathVariable("id") Long id,
                                            @RequestBody BoardRequestDto.updateInfo boardRequestDto,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return boardService.boardInfoUpdate(id, boardRequestDto, userDetails);
    }

    //게시물 삭제.
    @DeleteMapping("/api/board/{id}")
    private CustomException boardDel(@PathVariable("id") Long id,
                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {

        return boardService.boardDel(id, userDetails);
    }

    //좋아요 유무
    @PostMapping("/board/likes")
    private ResponseEntity<LikeDto.response> likeClick(@RequestBody LikeDto.request likeDto,
                                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(boardService.likeClick(likeDto, userDetails));
    }

    @PostMapping("/board/statecheck/{id}")
    private ResponseEntity<StateCheckDto> stateCheck(@PathVariable("id") Long id,
                                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(boardService.stateCheck(id,userDetails));
    }



    //매칭참여
    @PostMapping("/board/matchingentry")
    private ResponseEntity<EntryDto.response> matchingJoin(@RequestBody EntryDto.request entryDto,
                                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(boardService.matchingJoin(entryDto, userDetails));
    }

    //댓글작성
    @PostMapping("/api/board/{id}/comments")
    private ResponseEntity<CommentsInquiryDto> commentsNew(@PathVariable("id") Long id,
                                                           @RequestBody CommentsRequestDto commentsRequestDto,
                                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(boardService.commentsNew(id,commentsRequestDto,userDetails));
    }

    //댓글 전체 조회
    @GetMapping("/api/board/{id}/comments")
    private ResponseEntity<List<CommentsInquiryDto>> commentInquiry(@PathVariable("id") Long id) {
        return ResponseEntity.ok(boardService.commentInquiry(id));
    }

    //댓글 삭제
    @DeleteMapping("/api/board/{boardId}/comments/{commentId}")
    private CustomException commentDel(@PathVariable("boardId") Long boardId,
                                       @PathVariable("commentId") Long commentId,
                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return boardService.commentDel(boardId,commentId, userDetails);
    }

    @GetMapping("/board/search")
    public ResponseEntity<List<SearchResponseDto>> search(@RequestParam(value = "page") int page,
                                          @RequestParam(value = "boardType") String boardType,
                                          @RequestParam(value = "amount") int size,
                                          @RequestParam(value = "sort") String sort,
                                          @RequestParam(value = "keyword") String keyword,
                                          @RequestParam(value = "city") String city,
                                          @RequestParam(value = "gu") String gu) {
        return ResponseEntity.ok(searchService.queryDslSearch(boardType,page, size, sort, keyword, city, gu));
    }
}