package com.tutorial.web.pages;

import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import java.time.Duration;

import com.tutorial.service.bitcoinService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BitcoinPage extends WebPage {

    @SpringBean
    private bitcoinService bitcoinService;

    public BitcoinPage(final PageParameters parameters) {
        // Parse JSON data from bitcoinService into a list of currency maps
        List<Map<String, Object>> currencyList = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> root = mapper.readValue(bitcoinService.getLatestData(), Map.class);
            Map<String, Object> bpi = (Map<String, Object>) root.get("bpi");
            for (Map.Entry<String, Object> entry : bpi.entrySet()) {
                Map<String, Object> currency = (Map<String, Object>) entry.getValue();
                currencyList.add(currency);
            }
            // Optionally add header labels if needed using root.get("chartName") or root.get("time")
        } catch(Exception e) {
            // Log error and leave currencyList empty or add a message
        }

        // Wrap table in a container for auto-update
        WebMarkupContainer tableContainer = new WebMarkupContainer("tableContainer");
        tableContainer.setOutputMarkupId(true);
        // Auto-update every 5 seconds (5000ms)
        tableContainer.add(new AjaxSelfUpdatingTimerBehavior(Duration.ofSeconds(5)));
        add(tableContainer);

        // ListView inside the table container (child of tableContainer)
        tableContainer.add(new ListView<Map<String, Object>>("currencyList", currencyList) {
            @Override
            protected void populateItem(ListItem<Map<String, Object>> item) {
                Map<String, Object> currency = item.getModelObject();
                item.add(new Label("code", currency.get("code").toString()));
                item.add(new Label("rate", currency.get("rate").toString()));
                item.add(new Label("description", currency.get("description").toString()));
            }
        });
        
        // Refresh button
        add(new Link<Void>("refreshLink") {
            @Override
            public void onClick() {
                setResponsePage(BitcoinPage.class);
            }
        });
        
        // Home page button
        add(new Link<Void>("homePage") {
            @Override
            public void onClick() {
                setResponsePage(HomePage.class);
            }
        });
        
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forUrl("css/bitcoin-styles.css"));
    }
}
