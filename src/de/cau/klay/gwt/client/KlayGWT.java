package de.cau.klay.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import de.cau.klay.gwt.client.layout.KlayExampleLayouter;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class KlayGWT implements EntryPoint {
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		final Button layoutButton = new Button("Do Layout");

		// We can add style names to widgets
		layoutButton.addStyleName("sendButton");

		RootPanel.get("sendButtonContainer").add(layoutButton);

		// Create the popup dialog box
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setText("GWT Layout Result");
		dialogBox.setAnimationEnabled(true);
		final Button closeButton = new Button("Close");
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		final HTML layout = new HTML();
		final VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML("<b>Layout is</b>"));
		dialogVPanel.add(layout);
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(closeButton);
		dialogBox.setWidget(dialogVPanel);

		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
			}
		});

		class MyHandler implements ClickHandler {
			public void onClick(ClickEvent event) {
				Window.alert("layout");
				KlayExampleLayouter.layoutExample();
				
			}
		}
		layoutButton.addClickHandler(new MyHandler());
	}
}
