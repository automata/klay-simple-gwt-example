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
import com.google.gwt.user.client.Window;

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
 * Test class for the layout algorithm releases.
 * 
 * @author msp
 */
public final class KlayExampleLayouter {

	// CHECKSTYLEOFF MagicNumber

	/**
	 * Hidden default constructor.
	 */
	private KlayExampleLayouter() {
	}
	
	/**
	 * The main method.
	 * 
	 * @param args
	 *            command-line arguments
	 */
	public static void layoutExample() {
		// create a KGraph for layout
		KNode parentNode = createGraph();

		// create a progress monitor
		IKielerProgressMonitor progressMonitor = new BasicProgressMonitor();

		// create the layout provider
		AbstractLayoutProvider layoutProvider = new LayeredLayoutProvider();

		// perform layout on the created graph
		layoutProvider.doLayout(parentNode, progressMonitor);

		// output layout information
		printLayoutInfo(parentNode, progressMonitor);
	}

	/**
	 * Creates a KGraph, represented by a parent node that contains the actual
	 * nodes and edges of the graph.
	 * 
	 * @return a parent node that contains graph elements
	 */
	private static KNode createGraph() {
		// create parent node
		KNode parentNode = KimlUtil.createInitializedNode();
		configureParentNodeLayout(parentNode);

		// create child nodes
		KNode childNode1 = KimlUtil.createInitializedNode();
		// This automatically adds the child to the list of its parent's
		// children.
		childNode1.setParent(parentNode);
		KLabel nodeLabel1 = KimlUtil.createInitializedLabel(childNode1);
		nodeLabel1.setText("node1");
		configureNodeLayout(childNode1);

		KNode childNode2 = KimlUtil.createInitializedNode();
		childNode2.setParent(parentNode);
		KLabel nodeLabel2 = KimlUtil.createInitializedLabel(childNode2);
		nodeLabel2.setText("node2");
		configureNodeLayout(childNode2);

		// create ports (optional)
		KPort port1 = KimlUtil.createInitializedPort();
		// this automatically adds the port to the node's list of ports.
		port1.setNode(childNode1);
		confiugurePortLayout(port1, PortType.OUTPUT);

		KPort port2 = KimlUtil.createInitializedPort();
		port2.setNode(childNode2);
		confiugurePortLayout(port2, PortType.INPUT);

		// create edges
		KEdge edge1 = KimlUtil.createInitializedEdge();
		// this automatically adds the edge to the node's list of outgoing
		// edges.
		edge1.setSource(childNode1);
		// this automatically adds the edge to the node's list of incoming
		// edges.
		edge1.setTarget(childNode2);
		// this automatically adds the edge to the port's list of edges
		edge1.setSourcePort(port1);
		edge1.setTargetPort(port2);

		return parentNode;
	}

	/**
	 * Configure the layout of the parent node.
	 * 
	 * @param parentNode
	 *            parent node representing a graph
	 */
	private static void configureParentNodeLayout(final KNode parentNode) {
		// add options for the parent node
		KShapeLayout parentLayout = parentNode.getData(KShapeLayout.class);
		// set layout direction to horizontal
		parentLayout.setProperty(LayoutOptions.DIRECTION, Direction.RIGHT);
		// set overall element spacing
		parentLayout.setProperty(LayoutOptions.SPACING, 25f);
	}

	/**
	 * Configure the layout of the given node.
	 * 
	 * @param node
	 *            a node
	 */
	private static void configureNodeLayout(final KNode node) {
		KShapeLayout childLayout = node.getData(KShapeLayout.class);
		// set width and height for the node
		childLayout.setWidth(30.0f);
		childLayout.setHeight(30.0f);
		// set port constraints to fixed port positions
		childLayout.setProperty(LayoutOptions.PORT_CONSTRAINTS,
				PortConstraints.FIXED_POS);
	}

	/**
	 * Configure the layout of the given port.
	 * 
	 * @param port
	 *            a port
	 * @param type
	 *            the type of port (input or output)
	 */
	private static void confiugurePortLayout(final KPort port,
			final PortType type) {
		KShapeLayout portLayout = port.getData(KShapeLayout.class);
		// set position and side depending on the port type
		portLayout.setYpos(15.0f);
		switch (type) {
		case OUTPUT:
			portLayout.setXpos(30.0f);
			portLayout.setProperty(LayoutOptions.PORT_SIDE, PortSide.EAST);
			break;
		case INPUT:
			portLayout.setXpos(0.0f);
			portLayout.setProperty(LayoutOptions.PORT_SIDE, PortSide.WEST);
			break;
		default:
			break;
		}
	}

	/**
	 * Output layout information on the console.
	 * 
	 * @param parentNode
	 *            parent node representing a graph
	 * @param progressMonitor
	 *            progress monitor for the layout run
	 */
	private static void printLayoutInfo(final KNode parentNode,
			final IKielerProgressMonitor progressMonitor) {
		for (KNode childNode : parentNode.getChildren()) {
			KShapeLayout childLayout = childNode.getData(KShapeLayout.class);
			Window.alert(childNode.getLabels().get(0).getText()
					+ ": x = " + childLayout.getXpos() + ", y = "
					+ childLayout.getYpos());

			for (KEdge edge : childNode.getOutgoingEdges()) {
				KEdgeLayout edgeLayout = edge.getData(KEdgeLayout.class);
				Window.alert("  -edge: " + edgeLayout.createVectorChain());
			}
		}
	}

}
