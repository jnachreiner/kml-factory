package com.gwtapps.projects.nextCoffee.client.view;

import com.gwtapps.projects.nextCoffee.client.model.Client;
import com.gwtapps.projects.nextCoffee.client.model.DrinkCup;

public interface NextCoffeeViewListener 
{
	void onSignIn( String name );
	void onSignOut();
	void onSendMessage( Client toClient, DrinkCup drink );
}
