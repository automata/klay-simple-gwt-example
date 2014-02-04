package de.cau.klay.gwt.client;

import com.google.gwt.core.client.EntryPoint;

import de.cau.klay.gwt.client.layout.KlayNoFloLayouter;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class KlayGWT implements EntryPoint {
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		// Export JSNI defined method
		KlayNoFloLayouter.exportKlayLayout();
	}
}
