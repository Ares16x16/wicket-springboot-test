package com.tutorial.modelchain;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.PackageResourceReference;

@AuthorizeInstantiation("USER")
public class HomePage extends AuthenticatedWebPage {
    public HomePage() {
        // Check if user is authenticated
        if (!CustomSession.get().isSignedIn()) {
            setResponsePage(LoginPage.class);
            return;
        }
        
        add(new Link<Void>("toPersons") {
            @Override
            public void onClick() {
                setResponsePage(PersonListDetails.class);
            }
        });

        add(new Link<Void>("toUsers") {
            @Override
            public void onClick() {
                setResponsePage(UserDetails.class);
            }
        });
        add(new Link<Void>("toAnother") {
            @Override
            public void onClick() {
                setResponsePage(com.tutorial.linktopage.AnotherPage.class);
            }
        });

        add(new Link<Void>("logout") {
            @Override
            public void onClick() {
                CustomSession.get().invalidate();
                setResponsePage(LoginPage.class);
            }
        });
    }
    
    @Override
        public void renderHead(IHeaderResponse response) {
            super.renderHead(response);
            response.render(CssHeaderItem.forReference(
                new PackageResourceReference(LoginPage.class, "homepage.css")));
        }
}