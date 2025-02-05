package com.tutorial.service;

import org.springframework.stereotype.Service;

@Service
public class bitcoinService {
    private volatile String latestData = "No data yet";

    public String getLatestData() {
        return latestData;
    }

    public void updateData(String data) {
        this.latestData = data;
    }
}
