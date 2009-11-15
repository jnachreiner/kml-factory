package com.gwtapps.projects.nextCoffee.client;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.gwtapps.projects.nextCoffee.client.model.Client;
import com.gwtapps.projects.nextCoffee.client.model.ClientList;
import com.gwtapps.projects.nextCoffee.client.model.DrinkCup;
import com.gwtapps.projects.nextCoffee.client.view.NextCoffeeView;
import com.gwtapps.projects.nextCoffee.client.view.NextCoffeeViewListener;

public class NextCoffeeServiceClientImpl implements NextCoffeeViewListener{
	
	private class SignInCallback implements AsyncCallback{
		public void onFailure(Throwable throwable){ GWT.log("error sign in",throwable); }
		public void onSuccess(Object obj){
			view.setContactList( contactList );
			nextCoffeeService.getEvents( new GetEventsCallback() );
		}
	}
	
	private class EmptyCallback implements AsyncCallback{
		public void onFailure(Throwable throwable){ GWT.log("error",throwable); }
		public void onSuccess(Object obj){}
	}	
	
	private class GetEventsCallback implements AsyncCallback{
		public void onFailure(Throwable throwable){ GWT.log("error get events",throwable); }
		public void onSuccess(Object obj){
			ArrayList events = (ArrayList)obj;
			for( int i=0; i< events.size(); ++i ){
				Object event = events.get(i);
				handleEvent( event );
			}
			nextCoffeeService.getEvents( this );
		}
	}

	private NextCoffeeServiceAsync nextCoffeeService;
	private ClientList contactList;
	private NextCoffeeView view = new NextCoffeeView( this );

	public NextCoffeeServiceClientImpl( String URL ){
		nextCoffeeService = (NextCoffeeServiceAsync) GWT.create( NextCoffeeService.class );
		ServiceDefTarget endpoint = (ServiceDefTarget) nextCoffeeService;
		endpoint.setServiceEntryPoint( URL );		
	}

	public NextCoffeeView getView(){
		return view;
	}

	public void onSignIn( String name ){
		contactList = new ClientList( name );
		nextCoffeeService.signIn( name, new SignInCallback() );
	}

	public void onSignOut(){
		nextCoffeeService.signOut( new EmptyCallback() );
	}	

	public void onSendMessage( Client toContact, DrinkCup drink ){
		nextCoffeeService.orderDrink( toContact, drink, new EmptyCallback() );
	}

	protected void handleEvent( Object event ){
		if( event instanceof OrderDrinkEvent ){	
			OrderDrinkEvent orderDrinkEvent = (OrderDrinkEvent)event;
			//view.clinetListView( orderDrinkEvent.sender ).addMessage( orderDrinkEvent.drink );
		}
		else if( event instanceof SignOnEvent ){
			SignOnEvent signOnEvent = (SignOnEvent)event;
			contactList.addContact(signOnEvent.client);
			view.getContactListView().addContact(signOnEvent.client);
		}
		else if( event instanceof SignOffEvent ){
			SignOffEvent signOffEvent = (SignOffEvent)event;
			contactList.removeContact(signOffEvent.client);
			view.getContactListView().removeContact(signOffEvent.client);
		}
	}
 

}
