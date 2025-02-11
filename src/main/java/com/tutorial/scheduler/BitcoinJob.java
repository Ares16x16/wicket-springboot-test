package com.tutorial.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.tutorial.service.bitcoinService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BitcoinJob implements Job {

    @Autowired
    private bitcoinService bitcoinService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            // Generate random fake data
            Random random = new Random();
            Map<String, Object> root = new HashMap<>();
            Map<String, Object> bpi = new HashMap<>();
            
            Map<String, Object> usd = new HashMap<>();
            usd.put("code", "USD");
            usd.put("rate", String.format("%,d.00", 30000 + random.nextInt(20000)));
            usd.put("description", "United States Dollar");
            
            Map<String, Object> gbp = new HashMap<>();
            gbp.put("code", "GBP");
            gbp.put("rate", String.format("%,d.00", 20000 + random.nextInt(15000)));
            gbp.put("description", "British Pound Sterling");
            
            Map<String, Object> eur = new HashMap<>();
            eur.put("code", "EUR");
            eur.put("rate", String.format("%,d.00", 25000 + random.nextInt(17000)));
            eur.put("description", "Euro");
            
            bpi.put("USD", usd);
            bpi.put("GBP", gbp);
            bpi.put("EUR", eur);
            
            root.put("bpi", bpi);
            
            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(root);
            
            bitcoinService.updateData(jsonResponse);
        } catch (Exception ex) {
            throw new JobExecutionException("Error generating fake data", ex);
        }
    }
}
