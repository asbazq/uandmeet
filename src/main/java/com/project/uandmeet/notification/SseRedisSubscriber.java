package com.project.uandmeet.notification;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.uandmeet.Exception.CustomException;
import com.project.uandmeet.Exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SseRedisSubscriber {
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    //redis에서 publish(topic = sse)로 전송된 데이터 받음
    public void sendMessage(String message) {
        try {

            // ChatMessage 객채로 맵핑
            NotificationDto.Publish sseMessage = objectMapper.readValue(message, NotificationDto.Publish.class);

            notificationService.send(sseMessage);
            log.info("SSE 메세지 받기도 성공 ");

        } catch (Exception e) {
            throw new CustomException(ErrorCode. FAILED_MESSAGE);
        }
    }
}
