package com.tutorial.web.pages;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.dao.DataIntegrityViolationException;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.tutorial.security.AuthenticatedWebPage;
import com.tutorial.session.CustomSession;
import com.tutorial.service.AuthenticationService;
import com.tutorial.web.resources.CaptchaImageResource;

public class AdminPage extends AuthenticatedWebPage {
    @SpringBean
    private AuthenticationService authenticationService;
    
    @SpringBean
    private DefaultKaptcha captchaProducer;

    private TextField<String> newUsername;
    private PasswordTextField newPassword;
    private TextField<String> captchaInput;
    private boolean duplicateUsername = false;
    private CaptchaImageResource captchaResource;

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
        
        // New link to access Scheduler Monitor page
        add(new Link<Void>("scheduleMonitor") {
            @Override
            public void onClick() {
                setResponsePage(SchedulerMonitorPage.class);
            }
        });
        
        Form<Void> createAccountForm = new Form<Void>("createAccountForm") {
            @Override
            protected void onSubmit() {
                String inputCaptcha = captchaInput.getModelObject();
                if (captchaResource != null && 
                    captchaResource.getGeneratedText() != null && 
                    captchaResource.getGeneratedText().equals(inputCaptcha)) {
                    String user = newUsername.getModelObject();
                    String pass = newPassword.getModelObject();
                    try {
                        authenticationService.createAccountInDB(user, pass);
                        info("Account created successfully!");
                        newUsername.setModelObject("");
                        newPassword.setModelObject("");
                        captchaInput.setModelObject("");
                        generateNewCaptcha();
                    } catch (DataIntegrityViolationException e) {
                        duplicateUsername = true;
                    } catch (Exception e) {
                        error("An error occurred while creating the account.");
                    }
                } else {
                    error("Invalid captcha. Please try again.");
                    generateNewCaptcha();
                }
            }
        };
        add(createAccountForm);

        newUsername = new TextField<>("newUsername", Model.of(""));
        createAccountForm.add(newUsername);

        newPassword = new PasswordTextField("newPassword", Model.of(""));
        createAccountForm.add(newPassword);

        captchaInput = new TextField<>("captchaInput", Model.of(""));
        createAccountForm.add(captchaInput);

        captchaResource = new CaptchaImageResource(captchaProducer);
        NonCachingImage captchaImage = new NonCachingImage("captchaImage", captchaResource);
        captchaImage.setOutputMarkupId(true);
        createAccountForm.add(captchaImage);
    }

    private void generateNewCaptcha() {
        captchaResource = new CaptchaImageResource(captchaProducer);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forUrl("css/admin-styles.css"));
        if (duplicateUsername) {
            response.render(JavaScriptHeaderItem.forScript("alert('Username already exists.');", "duplicate-username-alert"));
            duplicateUsername = false;
        }
    }
}
