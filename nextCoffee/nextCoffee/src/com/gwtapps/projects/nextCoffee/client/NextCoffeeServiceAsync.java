package com.gwtapps.projects.nextCoffee.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtapps.projects.nextCoffee.client.model.Client;
import com.gwtapps.projects.nextCoffee.client.model.DrinkCup;

public interface NextCoffeeServiceAsync {

	void getEvents(AsyncCallback<ArrayList> callback);

	void signIn(String name, AsyncCallback<Void> callback);

	void signOut(AsyncCallback<Void> callback);

	void orderDrink(Client to, DrinkCup drink, AsyncCallback<Void> callback);

}
