package com.tutorial.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;

import com.tutorial.scheduler.BitcoinJob;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetailFactoryBean hkoWeatherJobDetail() {
        JobDetailFactoryBean factory = new JobDetailFactoryBean();
        factory.setJobClass(BitcoinJob.class);
        factory.setDescription("Invoke HKO Weather Job");
        factory.setDurability(true);
        return factory;
    }

    @Bean
    public CronTriggerFactoryBean hkoWeatherJobTrigger(JobDetailFactoryBean hkoWeatherJobDetail) {
        CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
        trigger.setJobDetail(hkoWeatherJobDetail.getObject());
        trigger.setCronExpression("0/10 * * * * ?"); // execute every 10 seconds
        trigger.setDescription("Trigger for Bitcoin Job");
        return trigger;
    }
}
