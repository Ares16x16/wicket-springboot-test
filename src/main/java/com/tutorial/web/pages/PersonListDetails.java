package com.tutorial.web.pages;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.tutorial.modelchain.CustomSession;
import com.tutorial.modelchain.Person;
import com.tutorial.service.MessageService;
import com.tutorial.service.UserService;

import io.micrometer.common.util.StringUtils;

public class PersonListDetails extends WebPage {
    @SpringBean
    private MessageService messageService;

    @SpringBean
    private UserService userService;

    private Form<Person> form;
    private DropDownChoice<Person> personsList;
    private TextField<String> nameField;
    private TextField<String> surnameField;
    private TextField<String> addressField;
    private TextField<String> emailField;

    public PersonListDetails() {

        if (!CustomSession.get().isSignedIn()) {
            setResponsePage(LoginPage.class);
            return;
        }

        // Initialize model with a new Person object
        Model<Person> listModel = new Model<Person>(new Person());

        // Renderer for the drop-down list
        ChoiceRenderer<Person> personRender = new ChoiceRenderer<Person>() {
            @Override
            public Object getDisplayValue(Person person) {
                return person.getName() + " " + person.getSurname();
            }
        };

        // Initialize drop-down choice
        personsList = new DropDownChoice<Person>("persons", listModel, personsPojo(), personRender);
        personsList.setNullValid(true);
        personsList.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                Person selectedPerson = personsList.getModelObject();
                if (selectedPerson != null) {
                    nameField.setModelObject(selectedPerson.getName());
                    surnameField.setModelObject(selectedPerson.getSurname());
                    addressField.setModelObject(selectedPerson.getAddress());
                    emailField.setModelObject(selectedPerson.getEmail());
                } else {
                    nameField.setModelObject("");
                    surnameField.setModelObject("");
                    addressField.setModelObject("");
                    emailField.setModelObject("");
                }
                target.add(nameField);
                target.add(surnameField);
                target.add(addressField);
                target.add(emailField);
            }
        });
        add(personsList);

        // Initialize form with CompoundPropertyModel
        form = new Form<Person>("form", new CompoundPropertyModel<Person>(listModel)) {
            @Override
            protected void onSubmit() {
                super.onSubmit();
                Person p = getModelObject();
                if (p != null) {
                    if (StringUtils.isNotBlank(p.getName()) && StringUtils.isNotBlank(p.getEmail())) {
                        // Save to database via UserService
                        userService.createUser(p.getName(), p.getEmail());
                        info("User saved successfully!");
                        // Redirect to UserDetails page
                        setResponsePage(UserDetails.class);
                    } else {
                        error("Name and email are required");
                    }
                }
            }
        };

        nameField = new TextField<String>("name");
        nameField.setRequired(true);
        nameField.setOutputMarkupId(true);
        form.add(nameField);

        surnameField = new TextField<String>("surname");
        surnameField.setOutputMarkupId(true);
        form.add(surnameField);

        addressField = new TextField<String>("address");
        addressField.setOutputMarkupId(true);
        form.add(addressField);

        emailField = new TextField<String>("email");
        emailField.setRequired(true);
        emailField.setOutputMarkupId(true);
        form.add(emailField);

        // Add Return to Home button
        Link<Void> homeLink = new Link<Void>("homeButton") {
            @Override
            public void onClick() {
                setResponsePage(HomePage.class);
            }
        };
        add(homeLink);

        add(form);

        // Display message from MessageService
        add(new Label("springMessage", messageService.getMessage()));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forUrl("css/personlistdetails-styles.css"));
    }

    private static List<Person> personsPojo() {
        List<Person> persons = new ArrayList<Person>();
        persons.add(new Person("a", "a", "a street", "a.a@a.com"));
        persons.add(new Person("b", "b", "b street", "b.b@b.com"));
        persons.add(new Person("c", "c", "c street", "c.c@c.com"));
        return persons;
    }
}