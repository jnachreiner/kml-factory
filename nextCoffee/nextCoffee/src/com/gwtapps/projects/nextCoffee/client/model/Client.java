package com.gwtapps.projects.nextCoffee.client.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Client implements IsSerializable
{
	private String name;
	public Client(){}
	public Client( String name ){
		this.name = name;
	}	
	
	public String getName(){
		return name;
	}
}