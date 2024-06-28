package com.project.uandmeet.chat.service;

import com.project.uandmeet.Exception.CustomException;
import com.project.uandmeet.Exception.ErrorCode;
import com.project.uandmeet.model.Member;
import com.project.uandmeet.security.UserDetailsImpl;
import com.project.uandmeet.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@RequiredArgsConstructor
public class CommonUtil {

    public static Member getMember(){
        try {
            UserDetailsImpl userDetails= (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userDetails == null) {
                throw new CustomException(ErrorCode.INVALID_AUTH_TOKEN);
            }
            return userDetails.getMember();
        } catch (ClassCastException e) {
            throw new CustomException(ErrorCode.INVALID_AUTH_TOKEN);
        }
    }
    public static Member getUserByToken(String token, JwtTokenProvider jwtTokenProvider){
        if(token == null || "null".equals(token)) return null;
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) jwtTokenProvider.getAuthentication(token).getPrincipal();
            return userDetails.getMember();
        }catch (Exception e){
            throw new CustomException(ErrorCode.INVALID_AUTH_TOKEN);
        }
    }
}
