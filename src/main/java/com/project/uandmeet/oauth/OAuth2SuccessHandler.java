package com.project.uandmeet.oauth;

import com.project.uandmeet.redis.RedisUtil;
import com.project.uandmeet.security.UserDetailsImpl;
import com.project.uandmeet.security.jwt.JwtProperties;
import com.project.uandmeet.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    private final RedisUtil redisUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        log.info("Authentication Success");

        // OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal(); // 구글 이메일

        String registrationId = userDetails.getMember().getLoginto();

        String nickname = userDetails.getMember().getNickname();

        String username = userDetails.getUsername();

        String profile = userDetails.getMember().getProfile();

        String accessToken = jwtTokenProvider.createToken(userDetails.getUsername(), userDetails.getMember().getId());

        String refreshToken = jwtTokenProvider.createRefreshToken(userDetails.getUsername());

        // redis 에 token 저장
        redisUtil.setDataExpire(userDetails.getUsername()+JwtProperties.HEADER_ACCESS, JwtProperties.TOKEN_PREFIX + accessToken, JwtProperties.ACCESS_EXPIRATION_TIME);
        redisUtil.setDataExpire(userDetails.getUsername()+JwtProperties.HEADER_REFRESH, JwtProperties.TOKEN_PREFIX + refreshToken, JwtProperties.REFRESH_EXPIRATION_TIME);


        String url = makeRedirectUrl(accessToken, refreshToken, username , nickname, profile, registrationId);

        log.info("조합된 URL: "+url);

        getRedirectStrategy().sendRedirect(request, response, url);
    }

    private String makeRedirectUrl(String access_Token, String refresh_Token, String username,String nickname, String profile, String registrationId) throws UnsupportedEncodingException {
        String encodedNickname = URLEncoder.encode(nickname, "UTF-8");
        String encodedRegistrationId = URLEncoder.encode(registrationId, "UTF-8");
        String encodedUsername = URLEncoder.encode(username, "UTF-8");
        return UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/authoriztion/" + registrationId)
                .queryParam("access_Token", access_Token)
               .queryParam("refresh_Token", refresh_Token)
                .queryParam("username", encodedUsername)
                .queryParam("nickname", encodedNickname)
                .queryParam("profile", profile)
                .queryParam("registrationId", encodedRegistrationId)
                .build().toUriString();

    }
}
