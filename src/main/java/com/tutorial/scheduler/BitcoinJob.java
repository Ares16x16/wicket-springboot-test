package com.tutorial.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.tutorial.service.bitcoinService;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class BitcoinJob implements Job {

    @Autowired
    private bitcoinService bitcoinService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            RestTemplate restTemplate = new RestTemplate();
            // Use trusted Coindesk API to get current Bitcoin Price
            String url = "https://api.coindesk.com/v1/bpi/currentprice.json";
            String jsonResponse = restTemplate.getForObject(url, String.class);
            bitcoinService.updateData(jsonResponse);
        } catch (Exception ex) {
            throw new JobExecutionException("Error fetching data from Coindesk API", ex);
        }
    }
}
