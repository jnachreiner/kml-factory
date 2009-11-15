package com.gwtapps.projects.nextCoffee.client.view;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.gwtapps.projects.nextCoffee.client.model.ClientList;

public class NextCoffeeView extends Composite implements CloseHandler<Window.ClosingEvent> {
	
	private DeckPanel mainPanel = new DeckPanel();
	private ClientListView contactListView;
	private SignInView signIn;
	private ClientList contactList;
	private NextCoffeeViewListener listener;
	
	public NextCoffeeView( NextCoffeeViewListener listener ){
		initWidget( mainPanel );
		this.listener = listener;
		
		signIn = new SignInView( this );
		
		mainPanel.add( signIn );
		mainPanel.showWidget(0);
		
		//History.addValueChangeHandler(this);
	}
	
	public NextCoffeeViewListener getListener(){
		return listener;
	}
	
	public ClientListView getContactListView(){
		return contactListView;
	}
	
	public ClientList getContactList(){
		return contactList;
	}
	
	public void setContactList( ClientList contactList ){
		this.contactList = contactList;
		if( contactListView == null ){
			contactListView = new ClientListView( this );
			mainPanel.add( contactListView );
		}
		mainPanel.showWidget(1);
	}	

	public void onClose(CloseEvent<Window.ClosingEvent> event) {
		listener.onSignOut();
	}	
}
