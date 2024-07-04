package com.project.uandmeet.repository;

import com.project.uandmeet.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public  interface BoardRepository extends JpaRepository<Board, Long> {

    Board findBoardById(Long boardId);
    Page<Board> findAllByBoardTypeAndCategory(String boardType, Category Category, Pageable pageable);
    Page<Board> findAllByBoardTypeAndCategoryAndCity(String boardType, Category category, Siarea city, Pageable pageable);
    Page<Board> findAllByBoardTypeAndCategoryAndCityAndGu(String boardType, Category Category, Pageable pageable,Siarea City, Guarea Gu);

    Page<Board> findAllByBoardType(String boardType, Pageable pageable);
    Page<Board> findAllByBoardTypeAndCity(String boardType, Pageable pageable,Siarea City);
    Page<Board> findAllByBoardTypeAndCityAndGu(String boardType, Pageable pageable,Siarea City, Guarea Gu);

    List<Board> findByMemberAndBoardTypeOrderByCreatedAtDesc(Member member, String boardType);
    Page<Board> findByMemberAndBoardType(Member member,  String boardType, Pageable pageable);
    Long countByMemberAndAndBoardType(Member member, String boardType);
    List<Board> findByBoardTypeOrderByLikeCountDesc(String boardType);
    List<Board> findByBoardTypeAndCategoryOrderByLikeCountDesc(String boardtype, Category category);
    Optional<Board> findByBoardTypeAndId(String boardType, Long boardId);

    @Query("SELECT p FROM Board p WHERE p.boardType = :boardType AND p.title LIKE CONCAT('%', :query, '%')")
    Page<Board> searchByBoardTypeAndTitleContaining(@Param("boardType") String boardType, @Param("query") String query, Pageable pageable);

    @Query("SELECT p FROM Board p WHERE p.boardType = :boardType AND p.content LIKE CONCAT('%', :query, '%')")
    Page<Board> searchByBoardTypeAndContentContaining(@Param("boardType") String boardType, @Param("query") String query, Pageable pageable);

    // @Query("SELECT p FROM Board p WHERE p.boardType = :boardType AND (p.title LIKE CONCAT('%', :query, '%') OR p.content LIKE CONCAT('%', :query, '%'))")
    @Query("SELECT p FROM Board p WHERE p.boardType = :boardType AND (p.title LIKE '%' || :query || '%' OR p.content LIKE '%' || :query || '%')")
    Page<Board> searchByBoardTypeAndTitleContainingOrContentContaining(@Param("boardType") String boardType, @Param("query") String query, Pageable pageable);
    
}