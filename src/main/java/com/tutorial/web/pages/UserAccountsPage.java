package com.tutorial.web.pages;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.tutorial.service.AuthenticationService;
import com.tutorial.model.LoginUser;

import java.util.List;
import com.tutorial.security.AuthenticatedWebPage;

public class UserAccountsPage extends AuthenticatedWebPage {
    
    @SpringBean
    private AuthenticationService authenticationService;
    
    public UserAccountsPage() {
        add(new Label("userAccountsTitle", "User Accounts"));
        
        List<LoginUser> users = authenticationService.getAllUsers();
        
        add(new ListView<LoginUser>("userRow", users) {
            @Override
            protected void populateItem(ListItem<LoginUser> item) {
                LoginUser user = item.getModelObject();
                item.add(new Label("username", Model.of(user.getUsername())));
                item.add(new Link<Void>("deleteUser") {
                    @Override
                    public void onClick() {
                        authenticationService.deleteUser(user.getUsername());
                        setResponsePage(UserAccountsPage.class, new PageParameters());
                    }
                });
            }
        });
        
        add(new Link<Void>("backToAdmin") {
            @Override
            public void onClick() {
                setResponsePage(AdminPage.class);
            }
        });
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forUrl("css/admin-styles.css"));
    }
}
