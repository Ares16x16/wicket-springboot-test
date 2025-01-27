package com.tutorial.web.pages;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.dao.DataIntegrityViolationException;

import com.tutorial.security.AuthenticatedWebPage;
import com.tutorial.session.CustomSession;
import com.tutorial.service.AuthenticationService;

public class AdminPage extends AuthenticatedWebPage {
    @SpringBean
    private AuthenticationService authenticationService;

    private TextField<String> newUsername;
    private PasswordTextField newPassword;

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

        add(new Link<Void>("viewUsers") {
            @Override
            public void onClick() {
                setResponsePage(UserAccountsPage.class);
            }
        });

        Form<Void> createAccountForm = new Form<Void>("createAccountForm") {
            @Override
            protected void onSubmit() {
                String user = newUsername.getModelObject();
                String pass = newPassword.getModelObject();
                try {
                    authenticationService.createAccountInDB(user, pass);
                    info("Account created successfully!");
                    // Clear the form fields
                    newUsername.setModelObject("");
                    newPassword.setModelObject("");
                } catch (DataIntegrityViolationException e) {
                    error("Username already exists. Please choose another.");
                } catch (Exception e) {
                    error("An error occurred while creating the account.");
                }
            }
        };
        add(createAccountForm);

        newUsername = new TextField<>("newUsername", Model.of(""));
        createAccountForm.add(newUsername);

        newPassword = new PasswordTextField("newPassword", Model.of(""));
        createAccountForm.add(newPassword);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forUrl("css/admin-styles.css"));
    }
}
