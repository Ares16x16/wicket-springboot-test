package com.tutorial.web.pages;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.Model;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.tutorial.service.AuthenticationService;
import com.tutorial.service.SchedulerService;
import com.tutorial.session.CustomSession;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import java.time.Duration;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class LoginPage extends WebPage {
    private final TextField<String> username;
    private final PasswordTextField password;
    private final CheckBox rememberMe;
    
    @SpringBean
    private AuthenticationService authenticationService;

    @SpringBean
    private SchedulerService schedulerService;

    public LoginPage() {
        add(new FeedbackPanel("feedback"));
        username = new TextField<>("username", Model.of(""));
        password = new PasswordTextField("password", Model.of(""));
        rememberMe = new CheckBox("rememberMe", Model.of(Boolean.FALSE));
        
        Form<Void> form = new Form<Void>("loginForm") {
            @Override
            protected void onSubmit() {
                String usernameValue = username.getModelObject();
                String passwordValue = password.getModelObject();
                Boolean rememberMeValue = rememberMe.getModelObject();
                
                if (authenticationService.authenticate(usernameValue, passwordValue)) {
                    try {
                        CustomSession session = CustomSession.get();
                        Set<String> roles = new HashSet<>();
                        if ("admin".equals(usernameValue)) {
                            roles.add("ADMIN");
                            session.signIn(usernameValue, roles);
                            setResponsePage(AdminPage.class);
                        } else {
                            roles.add("USER");
                            session.signIn(usernameValue, roles);
                            setResponsePage(HomePage.class);
                        }
                        if (rememberMeValue) {
                            // Do sth
                        }
                    } catch (Exception e) {
                        error("Session initialization failed: " + e.getMessage());
                    }
                } else {
                    error("Login failed");
                }
            }
        };

        form.add(username);
        form.add(password);
        form.add(rememberMe);
        add(form);
        
        Label schedulerStatus = new Label("schedulerStatus", () -> 
            schedulerService.getLastRunTime() != null ? schedulerService.getLastRunTime() : "Scheduler not started."
        );
        schedulerStatus.setOutputMarkupId(true);
        add(schedulerStatus);

        add(new AbstractAjaxTimerBehavior(Duration.ofSeconds(30)) {
            @Override
            protected void onTimer(AjaxRequestTarget target) {
                target.add(schedulerStatus);
            }
        });
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forUrl("css/login-styles.css"));
    }
}
