package com.tutorial.service;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class SchedulerService {

    private ScheduledExecutorService scheduler;
    private volatile String lastRunTime;

    public String getLastRunTime() {
        return lastRunTime;
    }

    @PostConstruct
    public void startScheduler() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            lastRunTime = "Scheduled Task Last Run At: " + LocalDateTime.now().toString();
            //System.out.println("Scheduled Task: Application is running.");
        }, 0, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void stopScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}
