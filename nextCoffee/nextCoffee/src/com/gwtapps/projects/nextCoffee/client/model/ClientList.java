package com.gwtapps.projects.nextCoffee.client.model;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class ClientList {
	
	private Client me;
	private List contacts = new ArrayList();
	
	public ClientList( String name ){
		this.me = new Client( name );
	}	
	
	public Client getMe(){
		return me;
	}	
	
	public void addContact( Client contact){
		contacts.add( contact );
	}	
	
	public void removeContact( Client contact ){
		for( Iterator it = contacts.iterator(); it.hasNext(); ){
			Client aContact = (Client)it.next();
			if( aContact.getName().compareTo( contact.getName() ) == 0 )
				it.remove();
		}
	}
	
	public Client getContact( String name ){
		for( Iterator it = contacts.iterator(); it.hasNext(); ){
			Client aClient = (Client)it.next();
			if( aClient.getName().compareTo( name ) == 0 )
				return aClient;
		}
		return null;
	}
	
	public int getContactCount(){
		return contacts.size();
	}	
	
	public Client getContact( int index ){
		return (Client) contacts.get(index);
	}

}

