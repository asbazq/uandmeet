package com.project.uandmeet.oauth;

import com.project.uandmeet.model.Member;
import com.project.uandmeet.model.MemberRoleEnum;
import com.project.uandmeet.repository.MemberRepository;
import com.project.uandmeet.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

//현재 서비스 클래스는 Securityconfig에서 불러와지고 있음
@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    //구글로 받은 userRequest 데이터에 대한 후처리되는 함수
    // 함수 종료시 @AuthenticationPrincipal 어노테이션이 만들어진다.
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException { //userRequest에 정보가 전부 담겨있음
        log.info("CustomOAuth2UserService.loadUser() called");

        OAuth2User oAuth2User;
        try {
            oAuth2User = super.loadUser(userRequest);
        } catch (OAuth2AuthenticationException  e) {
            log.error("Error loading user: ", e);
            throw e;
        }
        
        // 소셜로그인 시, code를 리턴(OAuth2-Client라이브러리) -> AccessToken 요청
        // 여기까지가 userRequest정보 -> loadUser함수 호출 -> 외부에서 회원프로필 받아줌.
        log.info("userRequest : "+ oAuth2User.getAttributes()); // 소셜 로그인한 유저의 이름,이메일,사진 등이 담겨있음

        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        String registrationId = clientRegistration.getRegistrationId();
        // registrationId로 어떤 OAuth로 로그인 했는지 알 수 있음
        log.info("registrationId : " + registrationId);

        String accessToken = userRequest.getAccessToken().getTokenValue();
        log.info("OAuth2 Access Token: " + accessToken);


        if ("kakao".equals(registrationId)) {
            Map<String, Object> attributes = oAuth2User.getAttributes();
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
            String email = (String) kakaoAccount.get("email");
            String loginto = registrationId;
            log.info("User email : {}", email);
            MemberRoleEnum role = MemberRoleEnum.USER;
            String profile = (String) properties.get("profile_image");
            String password = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(password);

            // nickname 설정
            String[] emailadress = email.split("@");
            String id = emailadress[0];
            String uuid = UUID.randomUUID().toString().substring(0,3);
            String uniqueId = id + uuid;
            Member member = memberRepository.findByUsername(email).orElseGet( 
                () -> {
                    Member newMember = Member.builder()
                                            .username(email)
                                            .password(encodedPassword)
                                            .loginto(loginto)
                                            .role(role)
                                            .nickname(uniqueId)
                                            .profile(profile)
                                            .build();
                    
                    return  memberRepository.save(newMember);
                });
                log.info("Member details: {}", member);
                return new UserDetailsImpl(member, oAuth2User.getAttributes());  // 리턴될때 authentication 에 저장됨

        }

        if ("google".equals(registrationId)) {
            String email = oAuth2User.getAttribute("email");
            String loginto = registrationId;
            MemberRoleEnum role = MemberRoleEnum.USER;
            String profile = oAuth2User.getAttribute("picture");
            String password = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(password);

            // nickname 설정
            String[] emailadress = email.split("@");
            String id = emailadress[0];
            String uuid = UUID.randomUUID().toString().substring(0,3);
            String uniqueId = id + uuid;

            Member member = memberRepository.findByUsername(email).orElseGet(
                () -> {
                    Member newMember = Member.builder()
                            .username(email)
                            .password(encodedPassword)
                            .loginto(loginto)
                            .role(role)
                            .nickname(uniqueId)
                            .profile(profile)
                            .build();
                    return memberRepository.save(newMember);
                });
        return new UserDetailsImpl(member, oAuth2User.getAttributes());  // 리턴될때 authentication 에 저장됨
        }
        throw new OAuth2AuthenticationException("Unknown registration id: " + registrationId);
    }

}

