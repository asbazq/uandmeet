package com.project.uandmeet.service;

import com.project.uandmeet.dto.*;
import com.project.uandmeet.dto.boardDtoGroup.*;
import com.project.uandmeet.dto.boardDtoGroup.LikeDto;
import com.project.uandmeet.dto.commentsDtoGroup.CommentsInquiryDto;
import com.project.uandmeet.dto.commentsDtoGroup.CommentsRequestDto;
import com.project.uandmeet.Exception.CustomException;
import com.project.uandmeet.Exception.ErrorCode;
import com.project.uandmeet.dto.MemberDtoGroup.MemberSimpleDto;
import com.project.uandmeet.model.*;
import com.project.uandmeet.redis.RedisUtil;
import com.project.uandmeet.repository.*;
import com.project.uandmeet.security.UserDetailsImpl;
import com.project.uandmeet.service.local.LocalUploader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.json.simple.parser.ParseException;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor //생성자 미리 생성.
public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final LikedRepository likedRepository;
    private final EntryRepository entryRepository;
    private final CommentRepository commentRepository;
    // private final S3Uploader s3Uploader;
    private final LocalUploader localUploader;
    private final String POST_IMAGE_DIR = "images";
    private final RedisUtil redisUtil;


    //게시판 생성
    @Transactional
    public Long boardNew(BoardRequestDto.createAndCheck boardRequestDto, UserDetailsImpl userDetails, MultipartFile data) throws IOException, CustomException, NullPointerException, ParseException {
        Member member = memberRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));

        Category category = categoryRepository.findAllByCategory(boardRequestDto.getCategory())
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));

        Siarea siarea = null;
        Guarea guarea = null;

        if (boardRequestDto.getBoardType().equals("matching")) {
            siarea = redisUtil.getSiarea(boardRequestDto.getCity());
            guarea = redisUtil.getGuarea(boardRequestDto.getCity(), boardRequestDto.getGu());
            // siarea = siareaRepository.findByCtpKorNmAbbreviation(boardRequestDto.getCity())
            //         .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));

            // guarea = guareaRepository.findAllBySiareaAndSigKorNm(siarea, boardRequestDto.getGu())
            //         .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));
        }

        Board board;
        if (data != null && !data.isEmpty()) {
            // ImageDto uploadImage = s3Uploader.upload(boardRequestDto.getData(), POST_IMAGE_DIR);
            ImageDto uploadImage = localUploader.upload(data, POST_IMAGE_DIR); 
            board = new Board(member, category, siarea, guarea, boardRequestDto, uploadImage.getImageUrl());
        } else {
            board = new Board(member, category, siarea, guarea, boardRequestDto);
        }

        boardRepository.save(board);
        log.info("board Id : {}", board.getId());
        return board.getId();
    }

    //매칭 게시물 전체 조회 (카테고리별 전체 조회)
    @Transactional
    public BoardResponseFinalDto boardMatchingAllInquiry(String type, String cate, Integer page, Integer amount, String city, String gu) throws NullPointerException, ParseException {
        page = Math.max(page - 1, 0);

        Sort sort = Sort.by("createdAt").descending();
        PageRequest pageRequest = PageRequest.of(page, amount, sort);
        Page<Board> boardPage;
        Category category = null;
        Siarea siarea = null;
        Guarea guarea = null;

        if (!cate.equalsIgnoreCase("all")) {
            category = categoryRepository.findAllByCategory(cate)
                    .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));
        }

        if (!city.equalsIgnoreCase("all")) {
            siarea = redisUtil.getSiarea(city);
            // siarea = siareaRepository.findByCtpKorNmAbbreviation(city)
            //         .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));
        }

        if (!gu.equalsIgnoreCase("all")) {
            guarea = redisUtil.getGuarea(city, gu);
            // guarea = guareaRepository.findAllBySiareaAndSigKorNm(siarea, gu)
            //         .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));
        }

        if (cate.equalsIgnoreCase("all")) {
            if (city.equalsIgnoreCase("all")) {
                boardPage = boardRepository.findAllByBoardType(type, pageRequest);
            } else {
                if (gu.equalsIgnoreCase("all")) {
                    boardPage = boardRepository.findAllByBoardTypeAndCity(type, pageRequest, siarea);
                } else {
                    boardPage = boardRepository.findAllByBoardTypeAndCityAndGu(type, pageRequest, siarea, guarea);
                }
            }
        } else {
            if (city.equalsIgnoreCase("all")) {
                boardPage = boardRepository.findAllByBoardTypeAndCategory(type, category, pageRequest);
            } else {
                if (gu.equalsIgnoreCase("all")) {
                    boardPage = boardRepository.findAllByBoardTypeAndCategoryAndCity(type, category, siarea, pageRequest);
                } else {
                    boardPage = boardRepository.findAllByBoardTypeAndCategoryAndCityAndGu(type, category, pageRequest, siarea, guarea);
                }
            }
        }

        List<BoardResponseDto> boardResponseDtos = new ArrayList<>();
        for (Board board : boardPage) {
            MemberSimpleDto memberSimpleDto = new MemberSimpleDto(board.getMember().getNickname(),
                    board.getMember().getUsername(), board.getMember().getProfile());
            BoardResponseDto boardResponseDto = new BoardResponseDto(memberSimpleDto, board);
            boardResponseDtos.add(boardResponseDto);
        }
        return new BoardResponseFinalDto(boardResponseDtos, boardPage.getTotalElements());
    }

    //매칭 게시물 상세 조회
    @Transactional
    public BoardResponseDto boardChoiceInquiry(Long id) {

        //개시판 정보 추출
        Board boards = boardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));

        // 찾으 정보를 Dto로 변환 한다.
        BoardResponseDto boardResponseDto = null;

        if (boards != null) {
            //작성자 간이 닉네임 생성.
            MemberSimpleDto memberSimpleDto = new MemberSimpleDto(boards.getMember().getNickname(),
                    boards.getMember().getUsername(), boards.getMember().getProfile());

            boardResponseDto = new BoardResponseDto(memberSimpleDto, boards);
            return boardResponseDto;
        } else return null;
    }

    //게시물 삭제.
    @Transactional
    public CustomException  boardDel(Long id, UserDetailsImpl userDetails) {
        Member member = memberRepository.findById(userDetails.getMember().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));

        if (board.getMember().getUsername().equals(member.getUsername())) {
            try {
                boardRepository.deleteById(id);
                return new CustomException(ErrorCode.COMPLETED_OK);
            } catch (Exception e) {
                return new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new CustomException(ErrorCode.INVALID_AUTHORITY);
        }
    }

    //매칭 게시물 수정
    @Transactional
    public CustomException boardUpdate(Long id, BoardRequestDto.updateMatching boardRequestMatchingUpdateDto, UserDetailsImpl userDetails) {
        Member member = memberRepository.findById(userDetails.getMember().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));

        if (board.getMember().getUsername().equals(member.getUsername())) {
            Board boardUpdate = new Board(board, boardRequestMatchingUpdateDto);
            try {
                boardRepository.save(boardUpdate);
                return new CustomException(ErrorCode.COMPLETED_OK);
            } catch (Exception e) {
                return new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new CustomException(ErrorCode.INVALID_AUTHORITY);
        }
    }


    //게시물 좋아요 유무
    @Transactional
    public LikeDto.response likeClick(LikeDto.request likeDto, UserDetailsImpl userDetails) {
        Member member = memberRepository.findById(userDetails.getMember().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));

        Board board = boardRepository.findById(likeDto.getBoardId())
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));

        if (!board.getMember().getId().equals(member.getId())) {
            if (likeDto.getIsLike()) {
                if (!likedRepository.findByBoardAndMember(board, member).isPresent()) {
                    Liked like = new Liked(likeDto, member, board);
                    likedRepository.save(like);
                    board.setLikeCount(board.getLikeCount() + 1);
                    boardRepository.save(board);
                    LikeDto.response response = new LikeDto.response(board.getLikeCount());
                    return response;
                } else {
                    throw new CustomException(ErrorCode.DUPLICATE_APPLY);
                }
            } else {
                if (likedRepository.findByBoardAndMember(board, member).isPresent()) {

                    Liked like = likedRepository.findByBoardAndMember(board, member)
                            .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));
                    likedRepository.deleteById(like.getId());
                    board.setLikeCount(board.getLikeCount() - 1);
                    boardRepository.save(board);
                    LikeDto.response response = new LikeDto.response(board.getLikeCount());
                    return response;
                } else {
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            throw new CustomException(ErrorCode.INVALID_AUTHORITY);
        }
    }


    @Transactional
    public EntryDto.response matchingJoin(EntryDto.request entryDto, UserDetailsImpl userDetails) {
        Board board = boardRepository.findById(entryDto.getBoardId())
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));

        Member member = memberRepository.findById(userDetails.getMember().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));

        if (!board.getMember().getId().equals(member.getId())) {
            if (entryDto.getIsMatching()) {
                if (!entryRepository.findByMemberAndBoard(member, board).isPresent()) {
                    Entry entry = new Entry(board, member, entryDto);
                    entryRepository.save(entry);
                    board.setCurrentEntry(board.getCurrentEntry() + 1);
                    boardRepository.save(board);
                    EntryDto.response response = new EntryDto.response(board.getCurrentEntry());
                    return response;
                } else {
                    throw new CustomException(ErrorCode.DUPLICATE_APPLY);
                }
            } else {
                if (entryRepository.findByMemberAndBoard(member, board).isPresent()) {
                    Entry entry = entryRepository.findByMemberAndBoard(member, board)
                            .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));

                    entryRepository.delete(entry);
                    board.setCurrentEntry(board.getCurrentEntry() - 1);
                    boardRepository.save(board);
                    EntryDto.response response = new EntryDto.response(board.getCurrentEntry());
                    return response;
                } else {
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            throw new CustomException(ErrorCode.INVALID_AUTHORITY);
        }
    }


    //댓글 작성.
    @Transactional
    public CommentsInquiryDto commentsNew(Long id, CommentsRequestDto commentsRequestDto, UserDetailsImpl userDetails) {

        Member member = memberRepository.findById(userDetails.getMember().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));


        MemberSimpleDto memberSimpleDto = new MemberSimpleDto(member.getNickname(),
                member.getUsername(), member.getProfile());


        Comment comment = new Comment(commentsRequestDto, member, board);
        commentRepository.save(comment);
        //댓글수 넣기
        board.setCommentCount(board.getCommentCount() + 1);
        boardRepository.save(board);
        CommentsInquiryDto commentsInquiryDto = new CommentsInquiryDto(memberSimpleDto, comment);
        return commentsInquiryDto;
    }

    //댓글 조회
    public List<CommentsInquiryDto> commentInquiry(Long id) {

        //Board의 전체 댓글들을 조회한다.
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));

        List<Comment> commentList = commentRepository.findByBoardOrderByCreatedAtDesc(board);

        // 찾으 정보를 Dto로 변환 한다.
        List<CommentsInquiryDto> commentsInquiryDtos = new ArrayList<>();

        if (commentList != null) {
            for (Comment commentTemp : commentList) {
                //작성자 간이 닉네임 생성.
                MemberSimpleDto memberSimpleDto = new MemberSimpleDto(commentTemp.getMember().getNickname(),
                        commentTemp.getMember().getUsername(), commentTemp.getMember().getProfile());

                CommentsInquiryDto commentsInquiryDto = new CommentsInquiryDto(memberSimpleDto, commentTemp);
                commentsInquiryDtos.add(commentsInquiryDto);
            }
        }
        return commentsInquiryDtos;
    }

    //댓글 삭제
    @Transactional
    public CustomException commentDel(Long boardId, Long commentId, UserDetailsImpl userDetails) {
        //로그인 유저 정보.
        Member memberTemp = memberRepository.findById(userDetails.getMember().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));
        Comment comment = commentRepository.findByIdAndBoard(commentId, board)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));

        //본인이 아니면 예외처리
        if (comment.getMember().getUsername().equals(memberTemp.getUsername())) {
            commentRepository.deleteById(comment.getId());
            //댓글수 넣기
            board.setCommentCount(board.getCommentCount() - 1);
            boardRepository.save(board);
            return new CustomException(ErrorCode.COMPLETED_OK);
        } else {
            return new CustomException(ErrorCode.INVALID_AUTHORITY);
        }
    }

    public CustomException categoryNew(String category) {

        String[] TEMP = new String[]{"gym", "running", "ridding", "badminton", "tennis", "golf", "hiking", "ballet", "climing", "pilates", "swiming", "boxing", "bowling",
                "crossfit", "gymnastics", "skateboard", "skate", "pocketball", "ski", "futsal", "pingpong", "basketball", "baseball", "soccer", "volleyball", "etc"};
            for (String s : TEMP) {
                Category category1 = new Category(s);
                categoryRepository.save(category1);
            }
            return new CustomException(ErrorCode.COMPLETED_OK);
    }

    //공유 게시물 전체 조회 (카테고리별 전체 조회)
    @Transactional
    public BoardResponseFinalDto boardInfoAllInquiry(String type, String cate, Integer page, Integer amount) {
        page = Math.max(page - 1, 0);
        Sort sortInfo = Sort.by("createdAt").descending();
        PageRequest pageRequest = PageRequest.of(page, amount, sortInfo);
        Page<Board> boardPage;
        Category category = null;

        if (!cate.equalsIgnoreCase("all")) {
            category = categoryRepository.findAllByCategory(cate)
                    .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));
        }

        if (cate.equalsIgnoreCase("all")) {
            boardPage = boardRepository.findAllByBoardType(type, pageRequest);
        } else {
            boardPage = boardRepository.findAllByBoardTypeAndCategory(type, category, pageRequest);
        }
        // 찾으 정보를 Dto로 변환 한다.
        List<BoardResponseDto> boardResponseDtos = new ArrayList<>();
        for (Board boardTemp : boardPage) {
            MemberSimpleDto memberSimpleDto = new MemberSimpleDto(boardTemp.getMember().getNickname(),
                    boardTemp.getMember().getUsername(), boardTemp.getMember().getProfile());
            BoardResponseDto boardResponseDto = new BoardResponseDto(boardTemp, memberSimpleDto);
            boardResponseDtos.add(boardResponseDto);
        }
        return new BoardResponseFinalDto(boardResponseDtos, boardPage.getTotalElements());
    }


    //공유 게시물 상세 조회
    @Transactional
    public BoardResponseDto boardChoiceInfoInquiry(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));
        MemberSimpleDto memberSimpleDto = new MemberSimpleDto(board.getMember().getNickname(),
                board.getMember().getUsername(), board.getMember().getProfile());

        return new BoardResponseDto(board, memberSimpleDto);
    }


    //공유 게시물 수정
    @Transactional
    public CustomException boardInfoUpdate(Long id, BoardRequestDto.updateInfo boardRequestInfoUpdateDto,
                                           UserDetailsImpl userDetails) {
        Member memberTemp = memberRepository.findById(userDetails.getMember().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));

        if (board.getMember().getUsername().equals(memberTemp.getUsername())) {
            Board boardUpdate = new Board(board, boardRequestInfoUpdateDto);
            boardRepository.save(boardUpdate);
            return new CustomException(ErrorCode.COMPLETED_OK);
        } else {
            return new CustomException(ErrorCode.INVALID_AUTHORITY);
        }
    }


    // 매칭 참여 좋아요 유무 확인
    @Transactional
    public StateCheckDto stateCheck(Long id, UserDetailsImpl userDetails) {

        Member member = memberRepository.findById(userDetails.getMember().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));

        boolean matchingState = entryRepository.findByMemberAndBoard(member, board).isPresent();
        boolean likeState = likedRepository.findByBoardAndMember(board, member).isPresent();

        StateCheckDto stateCheckDto = new StateCheckDto(board.getBoardType(), matchingState, likeState);
        return stateCheckDto;
    }
}