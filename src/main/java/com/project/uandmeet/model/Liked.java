package com.project.uandmeet.model;

import com.project.uandmeet.dto.boardDtoGroup.LikeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Liked {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

//    public Liked(LikeDto likeDto, Member memberTemp, Board board) {
//        this.member = memberTemp;
//        this.board = board;
//        this.isLike = likeDto.getIsLike();
//    }

    public Liked(Board board, Member member) {
        this.member = member;
        this.board = board;
    }
}