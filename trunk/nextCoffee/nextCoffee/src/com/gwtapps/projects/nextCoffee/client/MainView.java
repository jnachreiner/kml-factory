package com.gwtapps.projects.nextCoffee.client;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class MainView extends Composite {

	public static interface CoffeeImageBundle extends ImageBundle {

		@Resource("basket_put.png")
		public AbstractImagePrototype addIcon();

		@Resource("basket_remove.png")
		public AbstractImagePrototype removeIcon();

		@Resource("cup.png")
		public AbstractImagePrototype cupIcon();

	}

	public static CoffeeImageBundle coffeeImages = (CoffeeImageBundle) GWT
			.create(CoffeeImageBundle.class);
	final Image cupIcon = new Image();
	private FlexTable clientsFlexTable = new FlexTable();
	private VerticalPanel orderSummaryColumn = new VerticalPanel();
	private VerticalPanel column1 = new VerticalPanel();
	private VerticalPanel column2 = new VerticalPanel();
	private ArrayList<String> clients = new ArrayList<String>();

	public MainView() {
		// Initialize main pane
		VerticalPanel mainPanel = new VerticalPanel();
		initWidget(mainPanel);
		setWidth("100%");
		setHeight("100%");
		mainPanel.add(createPage());

		clientsFlexTable.setText(0, 0, "Name");
		clientsFlexTable.setText(0, 1, "Selection");
		clientsFlexTable.setText(0, 2, "Coffee Cups");
		clientsFlexTable.setText(0, 3, "Clear");

		// Add styles to elements in the stock list table.
		clientsFlexTable.getRowFormatter().addStyleName(0,
				"clientsFlexTableHeader");
		clientsFlexTable.addStyleName("clientsFlexTableList");
		clientsFlexTable.getCellFormatter().addStyleName(0, 2, "clientsFlexTableImageColumn");

		addRow("Kiril");
		addRow("Murf");
	}

	public void addClientList(NextCoffeeServiceClientImpl view) {
		orderSummaryColumn.add(view.getView());
	}
	
	public HorizontalPanel createPage() {
		HorizontalPanel page = new HorizontalPanel();
		page.setWidth("100%");
		page.setHeight("100%");
		page.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);

		initColumn(page, orderSummaryColumn, "20%");

		initColumn(page, column1);
		column1.add(clientsFlexTable);

		initColumn(page, column2);
		return page;
	}

	public void initColumn(HorizontalPanel page, VerticalPanel column,
			String width) {
		page.add(column);
		page.setCellWidth(column, width);
		page.setCellHeight(column, "100%");
		// column.setHeight("100%");

	}

	public void initColumn(HorizontalPanel page, VerticalPanel column) {
		page.add(column);
		page.setCellWidth(column, "40%");
		page.setCellHeight(column, "100%");
		// column.setHeight("100%");

	}

	public void addRow(String name) {
		if (clients.contains(name))
			return;

		// HorizontalPanel customerRow = new HorizontalPanel();
		Label nameLabel = new Label(name);
		final FlowPanel cupArray = new FlowPanel();

		nameLabel.setWidth("100px");

		// create a listener for removing coffee icon to the column
		ClickHandler removeButtonListener = new ClickHandler() {
			public void onClick(ClickEvent event) {
				int lastCupID = cupArray.getWidgetCount() - 1;
				if (lastCupID > -1)
					cupArray.clear();
			}
		};

		PushButton removeButton = new PushButton(coffeeImages.removeIcon()
				.createImage(), removeButtonListener);

		// Define the oracle that finds drink suggestions
		final MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
		oracle.add("Latte");
		oracle.add("Moccachino");
		oracle.add("Black Coffee + Sugar 026");
		oracle.add("Black Coffee - Sugar");

		final SuggestBox box = new SuggestBox(oracle);
		// create a listener for adding coffee icon to the column
		SelectionHandler<Suggestion> addListener = new SelectionHandler<Suggestion>() {
			public void onSelection(SelectionEvent<Suggestion> event) {
				if (cupArray.getWidgetCount() == 4) {
					Window.alert("Easy Tiger!");
					box.setText("");
					return;
				}
				cupArray.add(coffeeImages.cupIcon().createImage());
				cupArray.getElement().setTitle(box.getText());
				box.setText("");
			}
		};
		box.addSelectionHandler(addListener);

		int row = clientsFlexTable.getRowCount();
		clientsFlexTable.setText(row, 0, name);
		clientsFlexTable.setWidget(row, 1, box);
		clientsFlexTable.setWidget(row, 2, cupArray);

		clientsFlexTable.setWidget(row, 3, removeButton);
	}
}
