package com.gwtapps.projects.nextCoffee.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.gwtapps.projects.nextCoffee.client.NextCoffeeService;
import com.gwtapps.projects.nextCoffee.client.OrderDrinkEvent;
import com.gwtapps.projects.nextCoffee.client.SignOffEvent;
import com.gwtapps.projects.nextCoffee.client.SignOnEvent;
import com.gwtapps.projects.nextCoffee.client.model.Client;
import com.gwtapps.projects.nextCoffee.client.model.DrinkCup;

public class NextCoffeeServiceImpl extends RemoteServiceServlet implements NextCoffeeService {
	
	protected static final int MAX_MESSAGE_LENGTH = 256;
	protected static final int MAX_NAME_LENGTH = 10;
	
	private class UserInfo{
		Client client;
		ArrayList events = new ArrayList();
	}	
	
	private Map users = new HashMap();
	public void signIn( String name ){		
		//adjust name
		name = cleanString( name, MAX_NAME_LENGTH );		

		//add user to list
		String id = getThreadLocalRequest().getSession().getId();
		UserInfo user = new UserInfo();
		user.client = new Client( name );
		synchronized( this ){
			users.put( id, user );
		}
		
		//create sign on event
		SignOnEvent event = new SignOnEvent();
		event.client = user.client;
		broadcastEvent( event, user );
		
		//add sign on events for current contact list
		synchronized( this ){
			Set entrySet = users.entrySet();
			for( Iterator it = entrySet.iterator(); it.hasNext(); )
			{
				Map.Entry entry = (Map.Entry)it.next();
				UserInfo userTemp = (UserInfo)entry.getValue();
				if( userTemp != user )
				{
					SignOnEvent eventTemp = new SignOnEvent();
					eventTemp.client = userTemp.client;
					user.events.add( eventTemp );
				}
			}
		}
	}
	
	public void signOut(){
		//remove user from list
		Client client;
		String id = getThreadLocalRequest().getSession().getId();
		synchronized( this ){
			UserInfo user = (UserInfo)users.get(id);
			client = user.client;
			users.remove(id);
		}
		
		//create sign on event
		SignOffEvent event = new SignOffEvent();
		event.client = client;
		broadcastEvent( event, null );
	}
	
	public ArrayList getEvents(){
		ArrayList events = null;
		UserInfo user = getCurrentUser();
		if( user != null ){
			if( user.events.size() == 0 ){
				try{
					synchronized( user ){
						user.wait( 30*1000 );
					}
				} 
				catch (InterruptedException ignored){}
			}
			synchronized( user ){
				events = user.events;
				user.events = new ArrayList();
			}
		}
		return events;
	}
	
	public void orderDrink( Client to, DrinkCup drink ){
		//adjust message
		String cleanMessage = cleanString( drink.toString(), MAX_MESSAGE_LENGTH );
		
		//get sender and receiver
		UserInfo sender = getCurrentUser();
		UserInfo receiver = getUserByName( to.getName() );
		
		if( receiver != null ){
			//create event
			OrderDrinkEvent event = new OrderDrinkEvent();
			event.sender = sender.client;
			event.drink = new DrinkCup( cleanMessage );
			
			//send event to receiver
			synchronized( receiver ){
				receiver.events.add( event );
				receiver.notifyAll();
			}
		}
	}
	
	protected void broadcastEvent( Object event, UserInfo except ){
		synchronized( this ){
			Set entrySet = users.entrySet();
			for( Iterator it = entrySet.iterator(); it.hasNext(); ){
				Map.Entry entry = (Map.Entry)it.next();
				UserInfo user = (UserInfo)entry.getValue();
				if( user != except ){
					synchronized( user ){
						user.events.add( event );
						user.notifyAll();
					}
				}
			}
		}
	}
	
	protected UserInfo getCurrentUser(){
		String id = getThreadLocalRequest().getSession().getId();
		synchronized( this ){
			return (UserInfo)users.get(id);
		}
	}	
	
	protected UserInfo getUserByName( String name ){
		UserInfo user = null;
		synchronized( this ){
			Set entrySet = users.entrySet();
			for( Iterator it = entrySet.iterator(); it.hasNext() && user == null; ){
				Map.Entry entry = (Map.Entry)it.next();
				UserInfo userTemp = (UserInfo)entry.getValue();
				if( userTemp.client.getName().compareTo(name) == 0 )
					user = userTemp;
			}
		}
		return user;
	}
	
	protected String cleanString( String value, int maxLength ){
		value = value.trim();
		if( value.length() > maxLength )
			value = value.substring(0,maxLength);
		value = value.replaceAll("\\<.*?\\>","");
		return value;
	}
}
