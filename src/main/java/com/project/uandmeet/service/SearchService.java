package com.project.uandmeet.service;

import com.project.uandmeet.dto.SearchResponseDto;
import com.project.uandmeet.model.Board;
import com.project.uandmeet.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final BoardRepository boardRepository;

    public List<SearchResponseDto>  queryDslSearch(String boardType,int page, int size, String sort, String keyword, String city, String gu) {

        page = Math.max(page - 1, 0);

        // 제목만 검색
        if(sort.equals("title")){

            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

            Page<Board> result = boardRepository.searchByBoardTypeAndTitleContaining(boardType, keyword, pageable);

             // map을 사용하면 각 요소를 처리하고 그 결과를 새로운 리스트로 변환
             List<SearchResponseDto> boardList = result.stream().map(board -> {

                // 정보 공유일때
                if(board.getBoardType().equals("information")){
                    Long id = board.getId();
                    Board board1 = boardRepository.findById(id).orElseThrow(() -> new NullPointerException("보드가 없습니다"));
                    return new SearchResponseDto(board1, "information");
                }
                // 매칭 게시판일때
                else{
                    List<SearchResponseDto> tempBoardList = new ArrayList<>();
                    matching_Service(city, gu, tempBoardList, board);
                    return tempBoardList.isEmpty() ? null : tempBoardList.get(0);
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
            return boardList;
        }

        // 내용만 검색
        if(sort.equals("content")){

            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

            Page<Board> result = boardRepository.searchByBoardTypeAndContentContaining(boardType, keyword, pageable);

            // map을 사용하면 각 요소를 처리하고 그 결과를 새로운 리스트로 변환
            List<SearchResponseDto> boardList = result.stream().map(board -> {

                // 정보 공유일때
                if(board.getBoardType().equals("information")){
                    Long id = board.getId();
                    Board board1 = boardRepository.findById(id).orElseThrow(() -> new NullPointerException("보드가 없습니다"));
                    return new SearchResponseDto(board1, "information");
                }
                // 매칭 게시판일때
                else{
                    List<SearchResponseDto> tempBoardList = new ArrayList<>();
                    matching_Service(city, gu, tempBoardList, board);
                    return tempBoardList.isEmpty() ? null : tempBoardList.get(0);
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
            return boardList;
        }

        //제목+내용 검색
        if (sort.equals("title_Content")) {

            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

            Page<Board> result = boardRepository.searchByBoardTypeAndTitleContainingOrContentContaining(boardType, keyword, pageable);

            // map을 사용하면 각 요소를 처리하고 그 결과를 새로운 리스트로 변환
            List<SearchResponseDto> boardList = result.stream().map(board -> {

                // 정보 공유일때
                if(board.getBoardType().equals("information")){
                    Long id = board.getId();
                    Board board1 = boardRepository.findById(id).orElseThrow(() -> new NullPointerException("보드가 없습니다"));
                    return new SearchResponseDto(board1, "information");
                }
                // 매칭 게시판일때
                else{
                    List<SearchResponseDto> tempBoardList = new ArrayList<>();
                    matching_Service(city, gu, tempBoardList, board);
                    return tempBoardList.isEmpty() ? null : tempBoardList.get(0);
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
            return boardList;
        }

        return null;
    }

    private void matching_Service(String city, String gu, List<SearchResponseDto> boardList, Board board) {

        // 전국일때
        if(gu.equals("all") && city.equals("all")){

            Long id = board.getId();
            Board board1 = boardRepository.findById(id).orElseThrow(() -> new NullPointerException("보드가 없습니다"));

            SearchResponseDto responseDto = new SearchResponseDto(board1);
            boardList.add(responseDto);
        }
        // city 전체 조회일때 city이름 가져와서 responseDto에 추가
        if(gu.equals("all") && board.getCity().getCtpKorNmAbbreviation().equals(city)){
            Long id = board.getId();
            Board board1 = boardRepository.findById(id).orElseThrow(() -> new NullPointerException("보드가 없습니다"));

            SearchResponseDto responseDto = new SearchResponseDto(board1);
            boardList.add(responseDto);
        }
        // 특정 city,gu 조회
        if(board.getCity().getCtpKorNmAbbreviation().equals(city) && board.getGu().getSigKorNm().equals(gu)){
            Long id = board.getId();
            Board board1 = boardRepository.findById(id).orElseThrow(() -> new NullPointerException("보드가 없습니다"));

            SearchResponseDto responseDto = new SearchResponseDto(board1);
            boardList.add(responseDto);
        }
    }

}