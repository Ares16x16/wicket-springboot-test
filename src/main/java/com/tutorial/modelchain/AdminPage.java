package com.tutorial.modelchain;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;

public class AdminPage extends AuthenticatedWebPage {
    @Override
    protected void onInitialize() {
        super.onInitialize();
        if (!CustomSession.get().hasRole("ADMIN")) {
            setResponsePage(LoginPage.class);
        }
        
        add(new Label("adminTitle", "Admin Page"));
        add(new Link<Void>("logout") {
            @Override
            public void onClick() {
                CustomSession.get().signOut();
                setResponsePage(LoginPage.class);
            }
        });
    }
}
