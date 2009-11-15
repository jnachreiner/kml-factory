 package com.gwtapps.projects.nextCoffee.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;
import com.gwtapps.projects.nextCoffee.client.NextCoffeeServiceClientImpl;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class NextCoffee implements EntryPoint {


	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		MainView mainView = new MainView();
		NextCoffeeServiceClientImpl nextCoffeeService = 
			new NextCoffeeServiceClientImpl( GWT.getModuleBaseURL() + "nextcoffee" );
		RootPanel.get("mainView").add(mainView);
		mainView.addClientList(nextCoffeeService);
		//RootPanel.get("mainView").add( nextCoffeeService.getView() );
	}
}
