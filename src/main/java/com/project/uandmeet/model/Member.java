package com.project.uandmeet.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //유저 아이디
    @Column(nullable = false, unique = true)
    private String username;

    //실제이름
    @Column(nullable = false)
    private String realname;

    @Column(nullable = false)
    private String socialId;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String birth;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String profile;

    @Column
    private String concern;

    @Column
    private Double star;

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "member",cascade = CascadeType.ALL)
    private List<Board> boardList = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "member",cascade = CascadeType.ALL)
    private List<Like> likeList = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "member",cascade = CascadeType.ALL)
    private List<Entry> entryList = new ArrayList<>();

    //
}
