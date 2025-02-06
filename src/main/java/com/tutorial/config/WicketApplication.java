package com.tutorial.config;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.tutorial.security.AuthenticatedWebPage;
import com.tutorial.session.CustomRoleCheckingStrategy;
import com.tutorial.session.CustomSession;
import com.tutorial.web.pages.AdminPage;
import com.tutorial.web.pages.HomePage;
import com.tutorial.web.pages.LoginPage;
import com.tutorial.web.pages.PersonListDetails;
import com.tutorial.web.pages.UserDetails;
import com.tutorial.web.pages.BitcoinPage;

import org.apache.wicket.authorization.strategies.CompoundAuthorizationStrategy;
import org.apache.wicket.authorization.strategies.page.SimplePageAuthorizationStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.RoleAuthorizationStrategy;
import org.apache.wicket.Session;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;

public class WicketApplication extends WebApplication {

    @Override
    public Class<LoginPage> getHomePage() {
        return LoginPage.class;
    }

    @Override
    public Session newSession(Request request, Response response) {
        return new CustomSession(request);
    }

    @Override
    public void init() {
        super.init();
        
        // Get Spring's ApplicationContext
        ApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        getComponentInstantiationListeners().add(new SpringComponentInjector(this, applicationContext));

        // Configure security
        CompoundAuthorizationStrategy authorizationStrategy = new CompoundAuthorizationStrategy();
        
        // Add page-based auth strategy
        SimplePageAuthorizationStrategy pageAuthStrategy = new SimplePageAuthorizationStrategy(
                AuthenticatedWebPage.class, LoginPage.class) {
            @Override
            protected boolean isAuthorized() {
                return CustomSession.get().isSignedIn();
            }
        };
        authorizationStrategy.add(pageAuthStrategy);
        
        // Add role-based auth strategy
        authorizationStrategy.add(new RoleAuthorizationStrategy(new CustomRoleCheckingStrategy()));
        
        // Set the authorization strategy
        getSecuritySettings().setAuthorizationStrategy(authorizationStrategy);
        
        // Configure error pages
        getApplicationSettings().setAccessDeniedPage(LoginPage.class);
        getApplicationSettings().setPageExpiredErrorPage(LoginPage.class);

        // Mount pages
        mountPage("/home", HomePage.class);
        mountPage("/persons", PersonListDetails.class);
        mountPage("/users", UserDetails.class);
        mountPage("/another", BitcoinPage.class);

        mountPage("/admin", AdminPage.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}