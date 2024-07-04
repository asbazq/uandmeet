package com.project.uandmeet.redis;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.project.uandmeet.model.Guarea;
import com.project.uandmeet.model.Siarea;
import com.project.uandmeet.repository.GuareaRepository;
import com.project.uandmeet.repository.SiareaRepository;
import com.project.uandmeet.Exception.CustomException;
import com.project.uandmeet.Exception.ErrorCode;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;
    private final RedisTemplate<String, Object> redisLocationTemplate;
    private final SiareaRepository siareaRepository;
    private final GuareaRepository guareaRepository;

    // key를 통해 value 리턴
    public String getData(String key) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    // 저장
    public void setData(String key, String value) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, value);
    }

    // 유효 시간 동안 (key, value) 저장
    public void setDataExpire(String key, String value, long duration) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(key, value, expireDuration);
    }


    // 삭제
    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    // 시 조회
    public Siarea getSiarea(String city) {
        String cacheKey = "siarea:" + city;
        Siarea siarea = (Siarea) redisLocationTemplate.opsForValue().get(cacheKey);

        if (siarea == null) {
            siarea = siareaRepository.findByCtpKorNmAbbreviation(city)
                    .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));
                    redisLocationTemplate.opsForValue().set(cacheKey, siarea, 14, TimeUnit.DAYS);
        }

        return siarea;
    }

    // 구 조회
    public Guarea getGuarea(String city, String gu) {
        String cacheKey = "guarea:" + city + ":" + gu;
        Guarea guarea = (Guarea) redisLocationTemplate.opsForValue().get(cacheKey);

        if (guarea == null) {
            Siarea siarea = getSiarea(city);
            guarea = guareaRepository.findAllBySiareaAndSigKorNm(siarea, gu)
                    .orElseThrow(() -> new CustomException(ErrorCode.EMPTY_CONTENT));
                    redisLocationTemplate.opsForValue().set(cacheKey, guarea, 14, TimeUnit.DAYS);
        }

        return guarea;
    }
}