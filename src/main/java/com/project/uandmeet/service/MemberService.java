package com.project.uandmeet.service;

import com.project.uandmeet.dto.*;
import com.project.uandmeet.model.Concern;
import com.project.uandmeet.model.JoinCnt;
import com.project.uandmeet.model.Member;
import com.project.uandmeet.redis.RedisUtil;
import com.project.uandmeet.repository.MemberRepository;
import com.project.uandmeet.security.UserDetailsImpl;
import com.project.uandmeet.security.jwt.JwtProperties;
import com.project.uandmeet.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@Transactional
@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RedisUtil redisUtil;
    private final JwtTokenProvider jwtTokenProvider;
    private String authNumber; // 난수 번호

    // 난수 생성
    public void makeRandomNumber() {

        String checkNum = UUID.randomUUID().toString().substring(0, 6);
        System.out.println("임시 비밀번호 : " + checkNum);
        authNumber = checkNum;
    }

    // 회원 가입 1. emali check
    public String checkemail(String username) throws IOException {
        String[] emailadress = username.split("@");
        String id = emailadress[0];
        String host = emailadress[1];
//        String pattern = "^[a-zA-Z0-9]*$";
        String pattern = "^[a-zA-Z0-9_!#$%&'\\*+/=?{|}~^.-]+@[a-zA-Z0-9.-]+.[a-zA-Z0-9.-]*$";
        String idpattern = "^[a-zA-Z0-9_!#$%&'\\*+/=?{|}~^.-]*$";
        String hostpattern = "^[a-zA-Z0-9.-]*$";
        // email 조건
        // ID 영문 대소문자, 숫자, _!#$%&'\*+/=?{|}~^.- 특문허용
        // Host 시작전 @, 영문 대소문자, 숫자, .-특문허용

        // 회원가입 username 조건
        if (username.length() < 10) {
            throw new IllegalArgumentException("이메일을 10자 이상 입력하세요");
        } else if (!Pattern.matches(idpattern, id)) {
            throw new IllegalArgumentException("id에 알파벳 대소문자와 숫자, 특수기호( _!#$%&'\\*+/=?{|}~^.-)로만 입력하세요");
        } else if (!Pattern.matches(hostpattern, host)) {
            throw new IllegalArgumentException("host에 알파벳 대소문자와 숫자, 특수기호(.-)로만 입력하세요");
        } else if (!Pattern.matches(pattern, username)) {
            throw new IllegalArgumentException("이메일 규격에 맞게 입력하세요");
        } else if (username.contains("script")) {
            throw new IllegalArgumentException("xss공격 멈춰주세요.");
        }

        // email 중복 확인
        checkDuplicateEmail(username);
        return "email check";
    }


    // 비밀번호, 비밀번호 재입력 확인
    public String checkPassword(String password, String passwordCheck) {
        if (password.length() < 3) {
            throw new IllegalArgumentException("비밀번호를 3자 이상 입력하세요");
        } else if (password.length() > 21) {
            throw new IllegalArgumentException("비밀번호를 20자 이하로 입력하세요");
        } else if (password.contains("script")) {
            throw new IllegalArgumentException("xss공격 멈춰주세요.");
        } else if (passwordCheck.contains("script")) {
            throw new IllegalArgumentException("xss공격 멈춰주세요.");
        }
        if (!(passwordCheck.equals(password))) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return "password check 완료";
    }

    public String signup(MemberRequestDto requestDto) throws IOException {
        String username = requestDto.getUsername();
        String[] emailadress = username.split("@");
        String id = emailadress[0];

        checkemail(requestDto.getUsername());
        checkPassword(requestDto.getPassword(), requestDto.getPasswordCheck());
        Member member = requestDto.register();
        member.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        // login 구별
        member.setLoginto("normal");
        member.setNickname(id);

        // 프로필 이미지 추가
//        if (requestDto.getUserProfileImage() != null) {
//            String profileUrl = s3Uploader.upload(requestDto.getUserProfileImage(), "profile");
//            users.setUserProfileImage(profileUrl);
//        }

        memberRepository.save(member);
        return "회원가입 완료";
    }


    public void checkDuplicateEmail(String username) {
        Optional<Member> member = memberRepository.findByUsername(username);
        if (member.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 계정입니다.");
        }
    }



    // 회원 탈퇴
    public String withdraw(UserDetailsImpl userDetails, String password) {
        if (userDetails.getPassword().equals(passwordEncoder.encode(password))) {
            String username = userDetails.getUsername();
            memberRepository.deleteByUsername(username);
        }
        return "회원탈퇴 완료";
    }

    public Map<String, String> refresh(HttpServletRequest request, HttpServletResponse response) {

        // refreshToken
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith(JwtProperties.TOKEN_PREFIX)) {
            throw new RuntimeException("JWT Token이 존재하지 않습니다.");
        }

        // Refresh Token 유효성 검사
        jwtTokenProvider.validateToken(authorizationHeader);
        String username = jwtTokenProvider.getUserPk(authorizationHeader);
        // Access Token 재발급
        String accessToken = jwtTokenProvider.createToken(username);

        Map<String, String> accessTokenResponseMap = new HashMap<>();

        // 현재시간과 Refresh Token 만료날짜를 통해 남은 만료기간 계산 (만료기간 전 재발급이 필요없다면 삭제)
        // Refresh Token 만료시간 계산해 특정 시간 미만일 시 refresh token 도 재발급
        Date now = new Date();
        Date refreshExpireTime = jwtTokenProvider.ExpireTime(authorizationHeader);
        if (refreshExpireTime.before(new Date(now.getTime() + 1000 * 60 * 60 * 24L))) { // refresh token 만료시간이 특정시간보다 작으면 재발급
            String newRefreshToken = jwtTokenProvider.createRefreshToken();
            accessTokenResponseMap.put(JwtProperties.HEADER_REFRESH, JwtProperties.TOKEN_PREFIX + newRefreshToken);
            redisUtil.setDataExpire(jwtTokenProvider.getUserPk(accessToken), newRefreshToken, JwtProperties.REFRESH_EXPIRATION_TIME);
        }

        accessTokenResponseMap.put(JwtProperties.HEADER_ACCESS, JwtProperties.TOKEN_PREFIX + accessToken);
        Map<String, String> tokens = accessTokenResponseMap;
        response.setHeader(JwtProperties.HEADER_ACCESS, tokens.get(JwtProperties.HEADER_ACCESS));
        if (tokens.get(JwtProperties.HEADER_REFRESH) != null) {
            response.setHeader(JwtProperties.HEADER_REFRESH, tokens.get(JwtProperties.HEADER_REFRESH));
        }
        return tokens;
    }

    public String findpassword(String username) {
        // 비밀번호 난수 생성
        makeRandomNumber();

        Member member = memberRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("해당 아이디가 없습니다.")
        );
        member.setPassword(passwordEncoder.encode(authNumber));

        //인증메일 보내기
        String setFrom = "wjdgns5488@naver.com"; // email-config에 설정한 자신의 이메일 주소를 입력
        String toMail = username;
        String title = "비밀번호 찾기 이메일 입니다."; // 이메일 제목
        String content =
                "홈페이지를 방문해주셔서 감사합니다." +    //html 형식으로 작성 !
                        "<br><br>" +
                        "임시 비밀번호는 " + authNumber + "입니다." +
                        "<br>" +
                        "로그인 후 비밀번호를 변경해 주세요"; //이메일 내용 삽입
        emailService.mailSend(setFrom, toMail, title, content);
        return authNumber;
    }

    // 활동 내역 조회
    public MypageDto action(UserDetailsImpl userDetails) {
        String username = userDetails.getUsername();
        Member member = memberRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("볼수 없는 정보입니다")
        );
        String nickname = member.getNickname();
        List<Concern> concern = member.getConcern();
        List<JoinCnt> joinCnt = member.getJoinCnt();
        MypageDto mypageDto = new MypageDto(nickname, concern, joinCnt);
        return mypageDto;
    }

    // 활동내역 -> 관심사 수정
    public MypageDto concernedit(UserDetailsImpl userDetails, List<Concern> concern) {
        String username = userDetails.getUsername();
        Member member = memberRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("수정 권한이 없습니다.")
        );
        String nickname = member.getNickname();
        List<JoinCnt> joinCnt = member.getJoinCnt();
        MypageDto mypageDto = new MypageDto(nickname, concern, joinCnt);
        return mypageDto;
    }

    // 활동 페이지 -> 닉네임 수정
    public MypageDto nicknameedit(UserDetailsImpl userDetails, String nickname) {
        String username = userDetails.getUsername();
        Member member = memberRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("수정 권한이 없습니다.")
        );
        List<Concern> concern = member.getConcern();
        List<JoinCnt> joinCnt = member.getJoinCnt();
        MypageDto mypageDto = new MypageDto(nickname, concern, joinCnt);
        return mypageDto;
    }

    // memberInfo 조회
    public MyPageInfoDto myinfo(UserDetailsImpl userDetails) {
        String username = userDetails.getUsername();
        Member member = memberRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("볼 수 없는 정보입니다")
        );
        String gender = member.getGender();
        String birth = member.getBirth();
        MyPageInfoDto myPageInfoDto = new MyPageInfoDto(username, gender, birth);
        return myPageInfoDto;
    }

    // info -> gender 수정
    public MyPageInfoDto genderedit(UserDetailsImpl userDetails, InfoeditRequestDto requestDto) {
        String username = userDetails.getUsername();
        Member member = memberRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("볼 수 없는 정보입니다")
        );
        String gender = requestDto.getGender();
        String birth = member.getBirth();
        MyPageInfoDto myPageInfoDto = new MyPageInfoDto(username, gender, birth);
        return myPageInfoDto;
    }

    // info -> birth 수정
    public MyPageInfoDto birthedit(UserDetailsImpl userDetails, InfoeditRequestDto requestDto) {
        String username = userDetails.getUsername();
        Member member = memberRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("볼 수 없는 정보입니다")
        );
        String gender = member.getGender();
        String birth = requestDto.getBirth();
        MyPageInfoDto myPageInfoDto = new MyPageInfoDto(username, gender, birth);
        return myPageInfoDto;
    }

    // profile 조회
    public ProfileDto profile(UserDetailsImpl userDetails) {
        String username = userDetails.getUsername();
        Member member = memberRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("볼수 없는 정보입니다")
        );
        String nickname = member.getNickname();
        double star = member.getStar();
        String profileimgurl = member.getProfileImgUrl();
        ProfileDto profileDto = new ProfileDto(nickname, star, profileimgurl);
        return profileDto;
    }

    // profile 수정
    public ProfileDto profileedit(UserDetailsImpl userDetails, ProfileEditRequestDto requestDto) {
        String username = userDetails.getUsername();
        Member member = memberRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("볼수 없는 정보입니다")
        );
        String nickname = member.getNickname();
        double star = member.getStar();
        String profileimgurl = requestDto.getProfileimgurl();
        ProfileDto profileDto = new ProfileDto(nickname, star, profileimgurl);
        return profileDto;
    }

    // password 변경
    public String changepass(UserDetailsImpl userDetails, String passwordcheck, String newpassword) {
        String username = userDetails.getUsername();
        Member member = memberRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("해당 권한이 없습니다.")
        );
        if (!passwordEncoder.encode(passwordcheck).equals(member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        } else {
            member.setPassword(passwordEncoder.encode(newpassword));
        }
        return "비밀번호 변경 완료";
    }
}

