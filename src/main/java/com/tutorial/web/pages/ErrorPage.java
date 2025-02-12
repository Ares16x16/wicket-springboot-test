package com.tutorial.web.pages;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

public class ErrorPage extends WebPage {
    
    // default constructor
    public ErrorPage() {
        this(new Exception("An unexpected error occurred"));
    }

    public ErrorPage(Exception e) {
        add(new Label("errorMessage", Model.of(e.getMessage())));
    }
}
