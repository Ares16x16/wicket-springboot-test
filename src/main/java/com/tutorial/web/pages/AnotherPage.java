package com.tutorial.web.pages;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;


public class AnotherPage extends WebPage{
	public AnotherPage(final PageParameters parameters){
		add(new Link<Void>("homePage"){

			@Override
			public void onClick() {
				setResponsePage(com.tutorial.web.pages.HomePage.class);
			}
			
		});
	}
}
