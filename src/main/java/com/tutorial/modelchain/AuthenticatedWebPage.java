package com.tutorial.modelchain;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class AuthenticatedWebPage extends WebPage {
    public AuthenticatedWebPage() {
        super();
    }

    public AuthenticatedWebPage(PageParameters parameters) {
        super(parameters);
    }
}
