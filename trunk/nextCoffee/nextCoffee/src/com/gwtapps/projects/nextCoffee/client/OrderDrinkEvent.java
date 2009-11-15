package com.gwtapps.projects.nextCoffee.client;

import com.gwtapps.projects.nextCoffee.client.model.Client;
import com.gwtapps.projects.nextCoffee.client.model.DrinkCup;

public class OrderDrinkEvent extends Event 
{
	public Client sender;
	public DrinkCup drink;
}
