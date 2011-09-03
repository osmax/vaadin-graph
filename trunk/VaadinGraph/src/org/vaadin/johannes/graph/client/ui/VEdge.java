package org.vaadin.johannes.graph.client.ui;

import org.vaadin.gwtgraphics.client.Line;
import org.vaadin.gwtgraphics.client.Shape;

public class VEdge extends Line {

	private final VNode node1;
	private final VNode node2;
	private final Shape text;
	private String originalStrokeColor;

	public VEdge(final VNode node1, final VNode node2, final Shape text) {
		super(node1.getX(), node1.getY(), node2.getX(), node2.getY());
		this.node1 = node1;
		this.node2 = node2;
		this.text = text;
	}

	public VNode getFirstNode() {
		return node1;
	}

	public VNode getSecondNode() {
		return node2;
	}

	public Shape getText() {
		return text;
	}

	public String getOrginalStrokeColor() {
		return getOriginalStrokeColor();
	}

	public void setOriginalStrokeColor(final String originalStrokeColor) {
		this.originalStrokeColor = originalStrokeColor;
	}

	public String getOriginalStrokeColor() {
		return originalStrokeColor;
	}

	@Override
	public String toString() {
		return text.getTitle();
	}
}
