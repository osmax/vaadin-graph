package org.vaadin.cytographer.client.ui;

import org.vaadin.gwtgraphics.client.Line;
import org.vaadin.gwtgraphics.client.Shape;
import org.vaadin.gwtgraphics.client.shape.Text;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.vaadin.terminal.gwt.client.UIDL;

public class VEdge extends Line implements ClickHandler {

	private final VGraph graph;
	private final VNode node1;
	private final VNode node2;
	private Shape text;
	private final String name;
	private String originalStrokeColor;
	private final VCytographer cytographer;

	public VEdge(final VCytographer cytographer, final VGraph graph, final VNode node1, final VNode node2, final Shape text,
			final String name) {
		super(node1.getX(), node1.getY(), node2.getX(), node2.getY());
		this.cytographer = cytographer;
		this.graph = graph;
		this.node1 = node1;
		this.node2 = node2;
		this.text = text;
		this.name = name;
		addClickHandler(this);
	}

	public static VEdge createAnEdge(final UIDL child, final VCytographer cytographer, final VGraph graph, final String name,
			final VNode node1, final VNode node2, final VVisualStyle style) {
		String str = "";
		if (name.indexOf("(") != -1 && name.indexOf(")") != -1) {
			str = name.substring(name.indexOf("(") + 1, name.indexOf(")"));
		}
		final Text text = new Text((node1.getX() + node2.getX()) / 2, (node1.getY() + node2.getY()) / 2, str);
		text.setFontSize(style.getEdgeFontSize());
		text.setFontFamily(style.getFontFamily());
		text.setStrokeOpacity(0);
		text.setFillOpacity(1);
		text.setFillColor(style.getEdgeLabelColor());
		final VEdge edge = new VEdge(cytographer, graph, node1, node2, text, name);
		edge.setStrokeColor(style.getEdgeColor());
		edge.setStrokeWidth(style.getEdgeLineWidth());

		// edge specific style attributes
		if (child != null && child.hasAttribute("_ec")) {
			edge.setStrokeColor(child.getStringAttribute("_ec"));
			edge.setOriginalStrokeColor(edge.getStrokeColor());
		}
		if (child != null && child.hasAttribute("_elw")) {
			edge.setStrokeWidth(child.getIntAttribute("_elw"));
		}
		return edge;
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

	public void setText(final Shape text) {
		this.text = text;
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

	public String getName() {
		return name;
	}

	@Override
	public void onClick(final ClickEvent event) {
		graph.setEdgeSelected((VEdge) event.getSource(), graph.getSelectedEdges().contains(event.getSource()));
		cytographer.nodeOrEdgeSelectionChanged();
	}
}
