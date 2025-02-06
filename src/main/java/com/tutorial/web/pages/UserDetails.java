package com.tutorial.web.pages;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.*;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

import com.tutorial.entity.User;
import com.tutorial.service.UserService;
import com.tutorial.session.CustomSession;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.apache.wicket.util.resource.IResourceStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class UserDetails extends WebPage {
    private static final long serialVersionUID = 1L;

    @SpringBean
    private transient UserService userService;

    public UserDetails() {

        if (!CustomSession.get().isSignedIn()) {
            setResponsePage(LoginPage.class);
            return;
        }
        
        // feedback panel
        add(new FeedbackPanel("feedback"));

        List<User> users = userService.getAllUsers();
        ListView<User> listView = new ListView<User>("userList", users) {
            @Override
            protected void populateItem(ListItem<User> item) {
                User user = item.getModelObject();
                item.add(new Label("userName", user.getName()));
                item.add(new Label("userEmail", user.getEmail()));
                item.add(new Label("userID", user.getId().toString()));
            }
        };
        add(listView);

        add(new Link<Object>("backHome") {
            @Override
            public void onClick() {
                setResponsePage(HomePage.class);
            }
        });

        // form to delete user by ID
        TextField<String> idField = new TextField<>("userID", Model.of(""));
        Form<Void> deleteForm = new Form<Void>("deleteForm") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit() {
                String idStr = idField.getModelObject();
                try {
                    Long id = Long.parseLong(idStr);
                    userService.deleteUser(id);
                    info("User deleted successfully!");
                    setResponsePage(UserDetails.class);
                } catch (NumberFormatException e) {
                    error("Invalid user ID format.");
                } catch (IllegalArgumentException e) {
                    error("User ID does not exist.");
                }
            }
        };
        deleteForm.add(idField.setRequired(true));
        add(deleteForm);

        // form to update user name by ID
        TextField<String> updateIdField = new TextField<>("updateUserID", Model.of(""));
        TextField<String> updateNameField = new TextField<>("updateUserName", Model.of(""));
        Form<Void> updateForm = new Form<Void>("updateForm") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit() {
                String idStr = updateIdField.getModelObject();
                String newName = updateNameField.getModelObject();
                try {
                    Long id = Long.parseLong(idStr);
                    userService.updateUserName(id, newName);
                    info("User name updated successfully!");
                    setResponsePage(UserDetails.class);
                } catch (NumberFormatException e) {
                    error("Invalid user ID format.");
                } catch (IllegalArgumentException e) {
                    error("User ID does not exist.");
                }
            }
        };
        updateForm.add(updateIdField.setRequired(true));
        updateForm.add(updateNameField.setRequired(true));
        add(updateForm);

        add(new Link<Void>("generatePdf") {
            @Override
            public void onClick() {
                try {
                    List<User> users = userService.getAllUsers();

                    String reportPath = "reports/person_details_report.jrxml";
                    InputStream reportStream = Thread.currentThread()
                            .getContextClassLoader()
                            .getResourceAsStream(reportPath);

                    if (reportStream == null) {
                        throw new RuntimeException("Could not find report template: " + reportPath);
                    }

                    JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
                    JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(users);

                    Map<String, Object> parameters = new HashMap<>();
                    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    JasperExportManager.exportReportToPdfStream(jasperPrint, baos);
                    byte[] pdfBytes = baos.toByteArray();

                    IResourceStream resourceStream = new AbstractResourceStreamWriter() {
                        @Override
                        public void write(java.io.OutputStream output) {
                            try {
                                output.write(pdfBytes);
                            } catch (java.io.IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        @Override
                        public String getContentType() {
                            return "application/pdf";
                        }
                    };

                    getRequestCycle().scheduleRequestHandlerAfterCurrent(
                            new org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler(
                                    resourceStream, "user_details.pdf"));

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
        response.render(CssHeaderItem.forUrl("css/userdetails-styles.css"));
    }
}