package com.gwtapps.projects.nextCoffee.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.gwtapps.projects.nextCoffee.client.model.Client;
import com.gwtapps.projects.nextCoffee.client.model.DrinkCup;

public interface NextCoffeeService extends RemoteService
{
	void signIn( String name );
	void signOut();
	/**
	 * @gwt.typeArgs <com.gwtapps.messenger.client.Event>
	 */
	ArrayList getEvents();
	void orderDrink( Client to, DrinkCup drink );
}
