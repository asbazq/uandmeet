package com.project.uandmeet.api;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

import org.json.simple.parser.ParseException;

@Slf4j
@Component
@EnableScheduling
public class ScheduledTasks {

    private final OpenApiManager openApiManager;

    public ScheduledTasks(OpenApiManager openApiManager) {
        this.openApiManager = openApiManager;
    }

    @PostConstruct
    public void onStartup() {
        log.info("apiManager start");
        fetchApiData();
    }

    // 2주마다 실행되는 스케줄러 설정 (Cron 표현식 사용)
    // (초(0초),분(0분),시간(자정), */14(14일마다), *(매월), ?(요일을 지정하지 않음))
    @Scheduled(cron = "0 0 0 */14 * ?")
    public void fetchApiData() {
        try {
            openApiManager.fetch();
            openApiManager.dataRequest();
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
