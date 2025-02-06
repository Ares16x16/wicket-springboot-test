package com.tutorial.web.pages;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.dao.DataIntegrityViolationException;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.tutorial.security.AuthenticatedWebPage;
import com.tutorial.session.CustomSession;
import com.tutorial.service.AuthenticationService;
import com.tutorial.web.resources.CaptchaImageResource;

import java.sql.SQLIntegrityConstraintViolationException;
import org.apache.wicket.validation.validator.StringValidator;
import org.apache.wicket.model.ResourceModel;

public class AdminPage extends AuthenticatedWebPage {
    @SpringBean
    private AuthenticationService authenticationService;
    
    @SpringBean
    private DefaultKaptcha captchaProducer;

    private TextField<String> newUsername;
    private PasswordTextField newPassword;
    private TextField<String> captchaInput;
    private CaptchaImageResource captchaResource;
    private NonCachingImage captchaImage;

    public AdminPage() {
        captchaResource = new CaptchaImageResource(captchaProducer);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        if (!CustomSession.get().hasRole("ADMIN")) {
            setResponsePage(LoginPage.class);
        }
        
        // FeedbackPanel
        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
        
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
                String generatedCaptcha = captchaResource.getGeneratedText();
                
                // Normalize captcha inputs
                if (inputCaptcha != null && generatedCaptcha != null) {
                    inputCaptcha = inputCaptcha.trim().toLowerCase();
                    generatedCaptcha = generatedCaptcha.trim().toLowerCase();
                    
                    if (generatedCaptcha.equals(inputCaptcha)) {
                        String user = newUsername.getModelObject();
                        String pass = newPassword.getModelObject();
                        try {
                            authenticationService.createAccountInDB(user, pass);
                            info("Account created successfully!");
                            newUsername.setModelObject("");
                            newPassword.setModelObject("");
                            captchaInput.setModelObject("");
                            generateNewCaptcha();
                        } catch (Exception e) {
                            // Check if the root cause is a duplicate entry
                            Throwable rootCause = getRootCause(e);
                            if (rootCause instanceof SQLIntegrityConstraintViolationException && 
                                rootCause.getMessage().contains("Duplicate entry")) {
                                error("Username already exists. Please choose a different username.");
                            } else {
                                error("An error occurred while creating the account.");
                            }
                            generateNewCaptcha();
                        }
                    } else {
                        error("Invalid captcha. Please try again.");
                        generateNewCaptcha();
                    }
                } else {
                    error("Please enter the captcha code.");
                    generateNewCaptcha();
                }
            }
        };
        add(createAccountForm);

        newUsername = new TextField<>("newUsername", Model.of(""));
        createAccountForm.add(newUsername);

        newPassword = new PasswordTextField("newPassword", Model.of(""));
        newPassword.setLabel(Model.of("Password"));  // replace variable name in error msg
        newPassword.setRequired(true);
        createAccountForm.add(newPassword);

        captchaInput = new TextField<>("captchaInput", Model.of(""));
        createAccountForm.add(captchaInput);

        captchaImage = new NonCachingImage("captchaImage", captchaResource);
        captchaImage.setOutputMarkupId(true);
        createAccountForm.add(captchaImage);

        // Ajax refresh button
        AjaxLink<Void> refreshButton = new AjaxLink<Void>("refreshCaptcha") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                generateNewCaptcha();
                captchaImage.setImageResource(captchaResource);
                target.add(captchaImage);
            }
        };
        createAccountForm.add(refreshButton);
    }

    private void generateNewCaptcha() {
        captchaResource = new CaptchaImageResource(captchaProducer);
        if (captchaImage != null) {
            captchaImage.setImageResource(captchaResource);
        }
    }

    // Add this helper method to get the root cause of exception
    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forUrl("css/admin-styles.css"));
    }
}
