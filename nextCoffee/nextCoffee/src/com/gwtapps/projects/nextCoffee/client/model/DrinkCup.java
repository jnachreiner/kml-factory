package com.gwtapps.projects.nextCoffee.client.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DrinkCup implements IsSerializable{
	
	private String name;
	
	public DrinkCup(){}
	public DrinkCup( String name ){
		this.name = name;
	}
	
	public String toString(){
		return name;
	}
}

