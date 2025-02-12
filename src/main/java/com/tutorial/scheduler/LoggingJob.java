package com.tutorial.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class LoggingJob implements Job {
    public static volatile String lastExecutionTime = "Never";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        lastExecutionTime = "LoggingJob executed at: " + java.time.LocalDateTime.now();
        System.out.println(lastExecutionTime);
    }
}
