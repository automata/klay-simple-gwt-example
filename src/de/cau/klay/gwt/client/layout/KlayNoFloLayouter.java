/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 *
 * Copyright 2009 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 *
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.klay.gwt.client.layout;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;


import java.util.Set;
import java.util.HashMap;

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.alg.BasicProgressMonitor;
import de.cau.cs.kieler.core.kgraph.KEdge;
import de.cau.cs.kieler.core.kgraph.KLabel;
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.core.kgraph.KPort;
import de.cau.cs.kieler.kiml.AbstractLayoutProvider;
import de.cau.cs.kieler.kiml.klayoutdata.KEdgeLayout;
import de.cau.cs.kieler.kiml.klayoutdata.KShapeLayout;
import de.cau.cs.kieler.kiml.options.Direction;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortConstraints;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.kiml.util.KimlUtil;
import de.cau.cs.kieler.klay.layered.LayeredLayoutProvider;
import de.cau.cs.kieler.klay.layered.p4nodes.NodePlacementStrategy;
import de.cau.cs.kieler.klay.layered.properties.PortType;
import de.cau.cs.kieler.klay.layered.properties.Properties;

/**
 * A layouter for NoFlo JSON graphs.
 * 
 * @author vilson
 */
public final class KlayNoFloLayouter {

	/**
	 * Hidden default constructor.
	 */
	private KlayNoFloLayouter() {
	}

	public static String klayLayout(String s) {
		JSONValue jsonValue = JSONParser.parseStrict(s);
		JSONObject jsonObj = jsonValue.isObject();
		
		// *** Encode noflo JSON to KGraph ***
		
		// Create and configure the root node
		KNode rootNode = KimlUtil.createInitializedNode();
		KShapeLayout rootLayout = rootNode.getData(KShapeLayout.class);
		// Set layout direction to horizontal
		rootLayout.setProperty(LayoutOptions.DIRECTION, Direction.RIGHT);
		// Set overall element spacing
		rootLayout.setProperty(LayoutOptions.SPACING, 25f);
		rootLayout.setProperty(Properties.NODE_PLACER, NodePlacementStrategy.LINEAR_SEGMENTS);
		
		// Use an auxiliary hashmap to store nodes/processes id's
		HashMap<String, KNode> auxNodes = new HashMap<String, KNode>();
		HashMap<KNode, String> auxProcesses = new HashMap<KNode, String>();
		
		// TODO: Encode groups
		
		// Encode nodes (processes on noflo)
		if (jsonObj.containsKey("processes")) {
			JSONObject processes = jsonObj.get("processes").isObject();
			Set<String> keys = processes.keySet();		
			
			for (String key:keys) {
				// Get process information from noflo JSON
				JSONObject process = processes.get(key).isObject();
				
				// Create a child node for the root node
				KNode childNode = KimlUtil.createInitializedNode();
				// This automatically adds the child to the list of its parent's
				// children
				childNode.setParent(rootNode);
				// Configure the child node
				KLabel nodeLabel = KimlUtil.createInitializedLabel(childNode);
				nodeLabel.setText(process.get("metadata").isObject().get("label").isString().stringValue());
				
				KShapeLayout childLayout = childNode.getData(KShapeLayout.class);
				// Set width and height for the node
				childLayout.setWidth(92.0f);
				childLayout.setHeight(72.0f);
				// set port constraints to fixed port positions
				childLayout.setProperty(LayoutOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);
				childLayout.setProperty(Properties.NODE_PLACER, NodePlacementStrategy.LINEAR_SEGMENTS);
				
				// Store the nodes's reference to make possible to access by it's process id in the future
				auxNodes.put(key, childNode);
				// TODO: Maybe we can do better than this...
				auxProcesses.put(childNode, key);
				
				// TODO: Encode ports for the child node
			}
		}
		
		// Encode edges (connections on noflo)
		if (jsonObj.containsKey("connections")) {
			JSONArray connections = jsonObj.get("connections").isArray();
			if (connections != null) {
				for (int i=0; i<connections.size(); i++) {
					// Get connection information from noflo JSON
					JSONObject connection = connections.get(i).isObject();
					if (connection.containsKey("data")) {
						continue;
					}
					
					JSONObject processSrc = connection.get("src").isObject();
					String srcProcId = processSrc.get("process").isString().stringValue();
					String srcPortId = processSrc.get("port").isString().stringValue();
					
					JSONObject processTgt = connection.get("tgt").isObject();
					String tgtProcId = processTgt.get("process").isString().stringValue();
					String tgtPortId = processTgt.get("port").isString().stringValue();
					
					// Find the source and target nodes which references are stored in our auxiliary dict/map
					KNode srcNode = auxNodes.get(srcProcId);
					// TODO: Encode port! KPort srcPort = srcNode.getPorts();
					KNode tgtNode = auxNodes.get(tgtProcId);
					// TODO: Encode port! KPort tgtPort = tgtNode.getPorts();
					
					// Create edge
					KEdge edge = KimlUtil.createInitializedEdge();
					// This automatically adds the edge to the node's list of outgoing
					// edges
					edge.setSource(srcNode);
					// this automatically adds the edge to the node's list of incoming
					// edges.
					edge.setTarget(tgtNode);
					// this automatically adds the edge to the port's list of edges
					// TODO: Encode port! edge.setSourcePort(srcPort);
					// TODO: Encode port! edge.setTargetPort(tgtPort);
									
				}
				
			}
		}
		
		// *** Apply the layouter ***
		
		// Create a progress monitor
		IKielerProgressMonitor progressMonitor = new BasicProgressMonitor();
		
		// Create the layout provider
		AbstractLayoutProvider layoutProvider = new LayeredLayoutProvider();
		
		// Perform layout on the created graph
		layoutProvider.doLayout(rootNode, progressMonitor);
		
		// *** Decode back KGraph to noflo JSON ***
		
		// Loop through all root nodes' children and update the respective processes nodes
		for (KNode childNode : rootNode.getChildren()) {
			KShapeLayout childLayout = childNode.getData(KShapeLayout.class);
			
			// Find the process in the noflo JSON
			String processId = auxProcesses.get(childNode);
			
			JSONObject processes = jsonObj.get("processes").isObject();
			JSONObject process = processes.get(processId).isObject();
			
			// Updates process metadata in the noflo JSON
			// TODO: How to convert a String to JSONValue without using the deprecated parse method?
			process.get("metadata").isObject().put("x", JSONParser.parse(Float.toString(childLayout.getXpos())));
			process.get("metadata").isObject().put("y", JSONParser.parse(Float.toString(childLayout.getYpos())));
		}
			
		// Convert the noflo JSON to a serialized String and return
		return jsonObj.toString();
	}
	
	public static native void exportKlayLayout() /*-{
		$wnd.klayLayout = $entry(@de.cau.klay.gwt.client.layout.KlayNoFloLayouter::klayLayout(Ljava/lang/String;));
	}-*/;
}
