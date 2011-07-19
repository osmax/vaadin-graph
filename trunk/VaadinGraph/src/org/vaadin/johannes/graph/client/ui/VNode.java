package org.vaadin.johannes.graph.client.ui;

import org.vaadin.gwtgraphics.client.Group;
import org.vaadin.gwtgraphics.client.Positionable;
import org.vaadin.gwtgraphics.client.Shape;
import org.vaadin.gwtgraphics.client.VectorObject;
import org.vaadin.gwtgraphics.client.shape.Text;

public class VNode extends Group implements Positionable {

	private Shape view;
	private final Text text;
	private final String name;
	private boolean textsVisible = true;

	public VNode(final Shape view, final String name) {
		super();
		this.view = view;
		this.name = name;
		text = new Text(view.getX(), view.getY(), name);
		text.setStrokeOpacity(0);
		add(view);
		add(text);
	}

	@Override
	protected Class<? extends VectorObject> getType() {
		return Group.class;
	}

	public void setView(final Shape view) {
		this.view = view;
	}

	public Shape getView() {
		return view;
	}

	public String getName() {
		return name;
	}

	public int getX() {
		return view.getX();
	}

	public int getY() {
		return view.getY();
	}

	public void setFillColor(final String color) {
		view.setFillColor(color);
	}

	public void setLabelColor(final String color) {
		text.setFillColor(color);
		text.setFillOpacity(1);
	}

	public void setX(final int i) {
		view.setX(i);
		text.setX(i);
	}

	public void setY(final int i) {
		view.setY(i);
		text.setY(i);
	}

	public void setFontSize(final int nodeFontSize) {
		text.setFontSize(nodeFontSize);
	}

	public void setFontFamily(final String family) {
		text.setFontFamily(family);
	}

	public void setTextVisible(final boolean visible) {
		if (!visible && textsVisible) {
			remove(text);
		} else if (!textsVisible) {
			add(text);
		}
		textsVisible = visible;
	}
}
