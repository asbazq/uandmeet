package com.project.uandmeet.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Entry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    private String nickname; // 참여한 사람의 닉네임 ( 이메일 잘라서 뒤에 랜덤값 붙힘 )

    public Entry(Board board, Member member) {
        this.member = member;
        this.board = board;
        this.nickname = member.getNickname();
    }
}
