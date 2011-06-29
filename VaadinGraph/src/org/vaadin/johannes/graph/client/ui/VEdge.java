package org.vaadin.johannes.graph.client.ui;

import org.vaadin.gwtgraphics.client.Line;
import org.vaadin.gwtgraphics.client.Shape;

public class VEdge extends Line {

	private final Shape node1;
	private final Shape node2;

	public VEdge(final Shape node1, final Shape node2) {
		super(node1.getX(), node1.getY(), node2.getX(), node2.getY());
		this.node1 = node1;
		this.node2 = node2;
	}

	public Shape getFirstNode() {
		return node1;
	}

	public Shape getSecondNode() {
		return node2;
	}

}
