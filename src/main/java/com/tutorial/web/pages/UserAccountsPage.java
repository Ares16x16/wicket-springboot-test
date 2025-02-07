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
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.apache.wicket.util.resource.IResourceStream;

import com.tutorial.service.AuthenticationService;
import com.tutorial.model.LoginUser;

import java.util.List;
import com.tutorial.security.AuthenticatedWebPage;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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

        add(new Link<Void>("generatePdf") {
            @Override
            public void onClick() {
                try {
                    List<LoginUser> users = authenticationService.getAllUsers();

                    String reportPath = "reports/users_report.jrxml";
                    InputStream reportStream = Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream(reportPath);
                    
                    if (reportStream == null) {
                        throw new RuntimeException("Could not find report template: " + reportPath);
                    }

                    JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
                    JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(users);

                    // Parameters for the report
                    Map<String, Object> parameters = new HashMap<>();

                    // Fill the report
                    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

                    // Export to PDF
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    JasperExportManager.exportReportToPdfStream(jasperPrint, baos);
                    byte[] pdfBytes = baos.toByteArray();

                    IResourceStream resourceStream = new AbstractResourceStreamWriter() {
                        @Override
                        public void write(java.io.OutputStream output) {
                            try {
                                output.write(pdfBytes, 0, pdfBytes.length);
                            } catch (java.io.IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        @Override
                        public String getContentType() {
                            return "application/pdf";
                        }
                    };

                    getRequestCycle().scheduleRequestHandlerAfterCurrent(new org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler(resourceStream, "users_report.pdf"));


                } catch (Exception e) {
                    error("Failed to generate PDF: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forUrl("css/admin-styles.css"));
        //response.render(CssHeaderItem.forUrl("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css"));
    }
}
