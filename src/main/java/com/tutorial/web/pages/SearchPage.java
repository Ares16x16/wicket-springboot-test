package com.tutorial.web.pages;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;  // added import
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;

import com.tutorial.service.LuceneService;
import com.tutorial.session.CustomSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchPage extends WebPage {
    
    @SpringBean
    private LuceneService luceneService;
    
    private List<LuceneService.SearchResult> searchResults = new ArrayList<>();
    private WebMarkupContainer resultsTable;
    private DataView<LuceneService.SearchResult> resultsDataView;
    // Add navigator as member variable
    private AjaxPagingNavigator navigator;

    public SearchPage() {
        if (!CustomSession.get().isSignedIn()) {
            setResponsePage(LoginPage.class);
            return;
        }

        final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        // Mark searchField final and set outputMarkupId
        final TextField<String> searchField = new TextField<>("searchTerm", Model.of(""));
        searchField.setOutputMarkupId(true);
        DropDownChoice<String> searchTypeChoice = new DropDownChoice<>("searchType", Model.of("Standard"), Arrays.asList("Standard", "Fuzzy"));

        Form<Void> searchForm = new Form<Void>("searchForm") {
            @Override
            protected void onSubmit() {
                try {
                    String term = searchField.getModelObject();
                    if (term == null || term.trim().isEmpty()) {
                        // If search term is empty, show all content
                        searchResults.clear();
                        searchResults.addAll(luceneService.getAllContent());
                    } else {
                        String algo = searchTypeChoice.getModelObject();
                        List<LuceneService.SearchResult> resultsList = luceneService.searchContent(term, algo);
                        searchResults.clear();
                        if (resultsList != null) {
                            searchResults.addAll(resultsList);
                        }
                    }
                    resultsDataView.setCurrentPage(0);
                    if (searchResults.isEmpty()) {
                        info("No results found");
                    } else {
                        info("Search successful");
                    }
                } catch (Exception e) {
                    error("Search failed: " + e.getMessage());
                }
            }
        };
        searchForm.add(searchField);
        searchForm.add(searchTypeChoice);
        
        // Ajax button for search                
        searchForm.add(new AjaxButton("searchButton", searchForm) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                target.add(resultsTable);
                target.add(feedbackPanel);
                target.add(navigator);  // Add navigator to target
            }
            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(feedbackPanel);
            }
        });

        // Show All Content button
        searchForm.add(new AjaxButton("showAllButton", searchForm) {
            {
                setDefaultFormProcessing(false);  // Set in initialization block instead
            }
            
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                try {
                    searchField.setModelObject("");  // Clear search term
                    searchResults.clear();
                    searchResults.addAll(luceneService.getAllContent());
                    resultsDataView.setCurrentPage(0);
                    info("Showing all content");
                    target.add(searchField);
                    target.add(resultsTable);
                    target.add(feedbackPanel);
                    target.add(navigator);
                } catch (Exception e) {
                    error("Failed to load content: " + e.getMessage());
                    target.add(feedbackPanel);
                }
            }
        });
        add(searchForm);

        // Results table container
        resultsTable = new WebMarkupContainer("resultsTable");
        resultsTable.setOutputMarkupId(true);
        resultsTable.setVisible(true);
        
        IDataProvider<LuceneService.SearchResult> dataProvider = new ListDataProvider<>(searchResults);
        resultsDataView = new DataView<LuceneService.SearchResult>("results", dataProvider, 10) {
            @Override
            protected void populateItem(Item<LuceneService.SearchResult> item) {
                LuceneService.SearchResult result = item.getModelObject();
                item.add(new Label("id", result.getId()));
                item.add(new Label("title", result.getTitle()));
                item.add(new Label("snippet", result.getSnippet()));
                item.add(new Label("score", String.valueOf(result.getScore())));
            }
        };
        resultsTable.add(resultsDataView);
        add(resultsTable);

        // Pagination
        navigator = new AjaxPagingNavigator("navigator", resultsDataView);
        navigator.setOutputMarkupId(true);  // Enable Ajax updates
        add(navigator);

        // Back to home button
        add(new Link<Void>("backToHome") {
            @Override
            public void onClick() {
                setResponsePage(HomePage.class);
            }
        });
    }
}
