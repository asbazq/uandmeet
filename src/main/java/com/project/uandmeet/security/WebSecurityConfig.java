package com.project.uandmeet.security;

import com.project.uandmeet.oauth.CustomOAuth2UserService;
import com.project.uandmeet.oauth.OAuth2SuccessHandler;
import com.project.uandmeet.redis.RedisUtil;
import com.project.uandmeet.security.jwt.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


@RequiredArgsConstructor
@Configuration
@EnableWebSecurity // 스프링 Security 지원을 가능하게 함
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

//    @Autowired
//    @Lazy
//    private JwtExceptionFilter jwtExceptionFilter;
    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;

    private final CorsFilter corsFilter;

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtil redisUtil;

    private final CustomOAuth2UserService customOAuth2UserService;

    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(WebSecurity web) {
        web
                .ignoring()
                .antMatchers("/h2-console/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        http.cors().configurationSource(corsConfigurationSource());
        // 토큰 인증이므로 세션 사용x
        http.csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(corsFilter)
                .formLogin().disable() // 기본적인 formLogin방식을 쓰지않음 -> JWT를 쓰려면 필수 위 세션허용,cors등록,formLogin방식을 꺼야함
                .httpBasic().disable(); // httpbasic방식(기본인증방식) : authorization에 id,pw를 담아서 보내는 방식(여기서 id,pw가 노출될수 있음)
        http.headers().frameOptions().sameOrigin();

        http
                .authorizeRequests()
                // 회원 관리 처리 API 전부를 login 없이 허용
                .antMatchers("/**").permitAll()
//                .antMatchers("/user/duplicate/username").permitAll()
//                .antMatchers("/api/**").permitAll()
//                .antMatchers("/user/signup").permitAll()
//                .antMatchers("/user/login").permitAll()
//                .antMatchers("/user/refresh").permitAll()
//                .antMatchers("/user/confirmEmail").permitAll()
//                .antMatchers("/user/signin/**").permitAll()
//                .antMatchers("/health/**").permitAll()
//                .antMatchers("/health").permitAll()
//                .antMatchers("/user/confirmEmail").permitAll()
//                .antMatchers("/wss/chat/**").permitAll()
//                .antMatchers("/api/**").permitAll()
                .anyRequest().permitAll()
                // 그 외 어떤 요청이든 '인증'
                .and()
//                .addFilter(new JwtAuthenticationFilter(authenticationManager(),redisUtil, jwtTokenProvider)) // AuthenticatonManager 파라미터 필요
                .addFilter(new JwtAuthorizationFilter(authenticationManager(), jwtTokenProvider))// AuthenticatonManager 파라미터 필요
//                .addFilterBefore(new JwtTokenExceptionFilter(), JwtAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(authenticationManager,redisUtil, jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);



        http.oauth2Login()
                .successHandler(oAuth2SuccessHandler)
                .userInfoEndpoint()
                .userService(customOAuth2UserService);


    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
       configuration.addAllowedOriginPattern("http://localhost:3000");
        // configuration.addAllowedOriginPattern("http://3.35.9.167:8080");
        //이곳에 관련 url 추가 해야합니다 도메인,리액트(?) 등
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.addExposedHeader("Authorization"); // 없으면 프론트측 Header에 나타나지않고 network에만 나타나게됨
        configuration.addExposedHeader(JwtProperties.HEADER_ACCESS); // 없으면 프론트측 Header에 나타나지않고 network에만 나타나게됨
        configuration.addExposedHeader(JwtProperties.HEADER_REFRESH);
        configuration.addExposedHeader("username");
        configuration.addExposedHeader("nickname");
        configuration.addExposedHeader("profile");
        configuration.addExposedHeader("loginto");
        configuration.setAllowCredentials(true);
//        configuration.validateAllowCredentials();
        configuration.setMaxAge(3600L);
        configuration.applyPermitDefaultValues(); // 기본 허용 설정
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
