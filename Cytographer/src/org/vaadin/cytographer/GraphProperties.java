package org.vaadin.cytographer;

import giny.model.Edge;
import giny.model.Node;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.Semantics;
import cytoscape.view.CyNetworkView;

public class GraphProperties {

	private final String title;

	private CyNetwork network;
	private CyNetworkView finalView;

	private final int[] edges;
	private final int[] nodes;

	private final Set<String> selectedNodes = new HashSet<String>();
	private final Set<String> selectedEdges = new HashSet<String>();

	private int width;
	private int height;
	private int cytoscapeViewWidth;
	private int cytoscapeViewHeight;
	private float zoomFactor = 1;
	private double nodeSize;

	private int maxX = Integer.MIN_VALUE;
	private int minX = Integer.MAX_VALUE;
	private int minY = Integer.MAX_VALUE;
	private int maxY = Integer.MIN_VALUE;

	private boolean useFitting = false;
	private boolean textsVisible = false;
	private boolean styleOptimization = false;

	public GraphProperties(final CyNetwork network, final CyNetworkView finalView, final String title) {
		this.network = network;
		this.finalView = finalView;
		this.title = title;
		edges = network.getEdgeIndicesArray();
		nodes = network.getNodeIndicesArray();
		measureDimensions(finalView);
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(final int height) {
		this.height = height;
	}

	public double getNodeSize() {
		return nodeSize;
	}

	public void setNodeSize(final double nodeSize) {
		this.nodeSize = nodeSize;
	}

	public boolean isStyleOptimization() {
		return styleOptimization;
	}

	public void setStyleOptimization(final boolean styleOptimization) {
		this.styleOptimization = styleOptimization;
	}

	public String getTitle() {
		return title;
	}

	public CyNetwork getNetwork() {
		return network;
	}

	public int[] getEdges() {
		return edges;
	}

	public int[] getNodes() {
		return nodes;
	}

	public int getMaxX() {
		return maxX;
	}

	public int getMinX() {
		return minX;
	}

	public int getMinY() {
		return minY;
	}

	public int getMaxY() {
		return maxY;
	}

	public int getCytoscapeViewWidth() {
		return cytoscapeViewWidth;
	}

	public void setCytoscapeViewWidth(final int cytoscapeViewWidth) {
		this.cytoscapeViewWidth = cytoscapeViewWidth;
	}

	public int getCytoscapeViewHeight() {
		return cytoscapeViewHeight;
	}

	public void setCytoscapeViewHeight(final int cytoscapeViewHeight) {
		this.cytoscapeViewHeight = cytoscapeViewHeight;
	}

	public boolean isUseFitting() {
		return useFitting;
	}

	public void setFitting(final boolean b) {
		useFitting = b;
	}

	public boolean isTextsVisible() {
		return textsVisible;
	}

	public void setTextVisible(final boolean b) {
		textsVisible = b;
	}

	public void setNetwork(final CyNetwork network) {
		this.network = network;
	}

	public void setFinalView(final CyNetworkView finalView) {
		this.finalView = finalView;
	}

	private void measureDimensions(final CyNetworkView finalView2) {
		for (final int ei : edges) {
			final Edge e = network.getEdge(ei);
			final Node node1 = e.getSource();
			final Node node2 = e.getTarget();
			final int x1 = (int) finalView.getNodeView(node1).getXPosition();
			final int y1 = (int) finalView.getNodeView(node1).getYPosition();
			final int x2 = (int) finalView.getNodeView(node2).getXPosition();
			final int y2 = (int) finalView.getNodeView(node2).getYPosition();
			if (x1 > maxX) {
				maxX = x1;
			}
			if (x1 < minX) {
				minX = x1;
			}
			if (y1 > maxY) {
				maxY = y1;
			}
			if (y1 < minY) {
				minY = y1;
			}
			if (x2 > maxX) {
				maxX = x2;
			}
			if (x2 < minX) {
				minX = x2;
			}
			if (y2 > maxY) {
				maxY = y2;
			}
			if (y2 < minY) {
				minY = y2;
			}
		}
		cytoscapeViewWidth = maxX - minX;
		cytoscapeViewHeight = maxY - minY;
	}

	public float getZoomFactor() {
		return zoomFactor;
	}

	public void setZoomFactor(final float zoomFactor) {
		this.zoomFactor = zoomFactor;
	}

	public CyNetworkView getFinalView() {
		return finalView;
	}

	public Set<String> getSelectedNodes() {
		return selectedNodes;
	}

	public Set<String> getSelectedEdges() {
		return selectedEdges;
	}

	public void addSelectedNode(final String n) {
		selectedNodes.add(n);
	}

	public void addSelectedEdge(final String e) {
		selectedEdges.add(e);
	}

	public void clearSelectedNodes() {
		selectedNodes.clear();
	}

	public void clearSelectedEdges() {
		selectedEdges.clear();
	}

	public Container getNodeAttributeContainerForSelectedNodes() {
		final IndexedContainer container = new IndexedContainer();
		container.addContainerProperty("index", Integer.class, null);
		container.addContainerProperty("identifier", String.class, null);

		for (final Integer nodeIndex : nodes) {
			final Node n = network.getNode(nodeIndex);
			for (final String str : selectedNodes) {
				if (str.equals(n.getIdentifier())) {
					final Item i = container.addItem(n);
					i.getItemProperty("index").setValue(nodeIndex);
					i.getItemProperty("identifier").setValue(str);
					break;
				}
			}
		}
		return container;
	}

	public void addANewNode(final String id, final int i, final int j) {
		final CyNode node = Cytoscape.getCyNode(id, true);
		network.addNode(node);
	}

	public void removeNode(final String id) {
		final CyNode node = Cytoscape.getCyNode(id, false);
		if (node != null) {
			network.removeNode(node.getRootGraphIndex(), true);
			network.removeEdge(node.getRootGraphIndex(), true);
		} else {
			throw new IllegalStateException("Node not found " + id);
		}
	}

	public void createAnEdge(final String[] ids) {
		final CyNode node1 = Cytoscape.getCyNode(ids[0], false);
		final CyNode node2 = Cytoscape.getCyNode(ids[1], false);
		if (node1 != null && node2 != null) {
			final CyEdge edge = Cytoscape.getCyEdge(node1, node2, Semantics.INTERACTION, ids[2], true);
			network.addEdge(edge);
		} else {
			throw new IllegalStateException("Edge creation failed since node not found " + Arrays.toString(ids));
		}
	}
}
