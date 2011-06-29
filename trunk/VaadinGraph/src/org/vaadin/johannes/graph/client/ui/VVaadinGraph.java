package org.vaadin.johannes.graph.client.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.Shape;
import org.vaadin.gwtgraphics.client.shape.Circle;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;

/**
 * Client side widget which communicates with the server. Messages from the
 * server are shown as HTML and mouse clicks are sent to the server.
 */
public class VVaadinGraph extends Composite implements Paintable, ClickHandler, MouseDownHandler, MouseUpHandler, MouseMoveHandler {

	/** Set the CSS class name to allow styling. */
	public static final String CLASSNAME = "v-vaadingraph";

	public static final String CLICK_EVENT_IDENTIFIER = "click";

	/** The client side widget identifier */
	protected String paintableId;

	/** Reference to the server connection object. */
	protected ApplicationConnection client;

	private final DrawingArea canvas;
	private int gwidth = 500;
	private int gheight = 500;
	private final AbsolutePanel panel;

	private Map<String, VEdge> edges = new HashMap<String, VEdge>();
	private Map<String, Shape> nodes = new HashMap<String, Shape>();
	private Map<Shape, Set<VEdge>> shapeToEdgesMap = new HashMap<Shape, Set<VEdge>>();
	private final Set<Shape> paintedShapes = new HashSet<Shape>();
	private boolean skipEvents;

	private String edgeColor;
	private String nodeBorderColor;
	private String nodeFillColor;
	private int edgeLineWidth;
	private int nodeBorderWidth;
	private int nodeSize;

	private Shape movedShape = null;

	private String bgColor;

	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */
	public VVaadinGraph() {
		panel = new AbsolutePanel();
		panel.setSize(gwidth + "px", gheight + "px");
		canvas = new DrawingArea(gwidth, gheight);
		panel.add(canvas);
		initWidget(panel);
		setStyleName(CLASSNAME);
		DOM.setStyleAttribute(canvas.getElement(), "border", "1px solid black");
	}

	/**
	 * Called whenever an update is received from the server
	 */
	public void updateFromUIDL(final UIDL uidl, final ApplicationConnection client) {
		// This call should be made first.
		// It handles sizes, captions, tooltips, etc. automatically.
		if (client.updateComponent(this, uidl, true)) {
			return;
		}

		// Save reference to server connection object to be able to send
		// user interaction later
		this.client = client;

		// Save the client side identifier (paintable id) for the widget
		paintableId = uidl.getId();

		gwidth = uidl.getIntAttribute("gwidth");
		gheight = uidl.getIntAttribute("gheight");

		bgColor = uidl.getStringAttribute("bc");
		edgeColor = uidl.getStringAttribute("ec");
		nodeBorderColor = uidl.getStringAttribute("nbc");
		nodeFillColor = uidl.getStringAttribute("nfc");
		edgeLineWidth = uidl.getIntAttribute("elw");
		nodeBorderWidth = uidl.getIntAttribute("nbw");
		nodeSize = uidl.getIntAttribute("ns") / 2;

		panel.setSize((50 + 1 + gwidth) + "px", (25 + gheight) + "px");
		canvas.setWidth(gwidth);
		canvas.setHeight(gheight);
		canvas.getElement().getStyle().setPropertyPx("width", gwidth);
		canvas.getElement().getStyle().setPropertyPx("height", gheight);
		canvas.addMouseUpHandler(this);
		canvas.addMouseMoveHandler(this);

		edges = new HashMap<String, VEdge>();
		nodes = new HashMap<String, Shape>();
		shapeToEdgesMap = new HashMap<Shape, Set<VEdge>>();

		for (int i = 0; i < uidl.getChildCount(); i++) {
			final UIDL child = uidl.getChildUIDL(i);
			final String name = child.getStringAttribute("name");
			final String node1name = child.getStringAttribute("node1");
			final String node2name = child.getStringAttribute("node2");

			Shape node1 = nodes.get(node1name);
			Shape node2 = nodes.get(node2name);
			if (node1 == null) {
				final Circle shape = new Circle(child.getIntAttribute("node1x"), child.getIntAttribute("node1y"), nodeSize);
				shape.setFillColor(nodeFillColor);
				shape.setStrokeColor(nodeBorderColor);
				shape.setStrokeWidth(nodeBorderWidth);
				shape.addClickHandler(this);
				shape.addMouseDownHandler(this);
				shape.addMouseUpHandler(this);
				shape.addMouseMoveHandler(this);
				nodes.put(node1name, shape);
				node1 = shape;
			}
			if (node2 == null) {
				final Circle shape = new Circle(child.getIntAttribute("node2x"), child.getIntAttribute("node2y"), nodeSize);
				shape.setFillColor(nodeFillColor);
				shape.setStrokeColor(nodeBorderColor);
				shape.setStrokeWidth(nodeBorderWidth);
				shape.addClickHandler(this);
				shape.addMouseDownHandler(this);
				shape.addMouseUpHandler(this);
				shape.addMouseMoveHandler(this);
				nodes.put(node2name, shape);
				node2 = shape;
			}
			final VEdge edge = new VEdge(node1, node2);
			edge.setStrokeColor(edgeColor);
			edge.setStrokeWidth(edgeLineWidth);
			Set<VEdge> edgs = shapeToEdgesMap.get(node1);
			if (edgs == null) {
				edgs = new HashSet<VEdge>();
				edgs.add(edge);
				shapeToEdgesMap.put(node1, edgs);
			} else {
				edgs.add(edge);
			}
			edgs = shapeToEdgesMap.get(node2);
			if (edgs == null) {
				edgs = new HashSet<VEdge>();
				edgs.add(edge);
				shapeToEdgesMap.put(node2, edgs);
			} else {
				edgs.add(edge);
			}
			edges.put(name, edge);
		}
		paintGraph();
	}

	private void paintGraph(final Shape... updatedShapes) {
		if (updatedShapes == null || updatedShapes.length == 0) {
			canvas.clear();
			paintedShapes.clear();
			for (final Map.Entry<String, VEdge> entry : edges.entrySet()) {
				final VEdge edge = entry.getValue();
				canvas.add(edge);
			}
			for (final Map.Entry<String, VEdge> entry : edges.entrySet()) {
				final VEdge edge = entry.getValue();
				final Shape n1 = edge.getSecondNode();
				if (!paintedShapes.contains(n1)) {
					canvas.add(n1);
					paintedShapes.add(n1);
				}
				final Shape n2 = edge.getFirstNode();
				if (!paintedShapes.contains(n2)) {
					canvas.add(n2);
					paintedShapes.add(n2);
				}
			}
		} else {
			for (final Shape s : updatedShapes) {
				canvas.remove(s);
				canvas.add(s);
			}
		}
	}

	private void updateEdges(final Shape node) {
		final Set<VEdge> edgs = shapeToEdgesMap.get(node);
		for (final VEdge e : edgs) {
			if (e.getFirstNode().equals(node)) {
				e.setX1(node.getX());
				e.setY1(node.getY());
			} else {
				e.setX2(node.getX());
				e.setY2(node.getY());
			}
			canvas.remove(e);
			canvas.add(e);
			if (e.getFirstNode().equals(node)) {
				canvas.remove(e.getSecondNode());
				canvas.add(e.getSecondNode());
			} else {
				canvas.remove(e.getFirstNode());
				canvas.add(e.getFirstNode());
			}
		}
	}

	public void onMouseMove(final MouseMoveEvent event) {
		if (movedShape != null) {
			final int x = event.getClientX() - 15;
			final int y = event.getClientY() - 60;
			movedShape.setX(x);
			movedShape.setY(y);
			updateEdges(movedShape);
			canvas.remove(movedShape);
			canvas.add(movedShape);
		}
	}

	public void onMouseUp(final MouseUpEvent event) {
		VConsole.log("mouse up");
		movedShape = null;
	}

	public void onMouseDown(final MouseDownEvent event) {
		final Object sender = event.getSource();
		if (sender instanceof Shape) {
			final Shape node = (Shape) sender;
			movedShape = node;
			VConsole.log("mouse down on shape");
		}
	}

	public void onClick(final ClickEvent event) {
		movedShape = null;
		if (skipEvents) {
			return;
		}
		final Object sender = event.getSource();
		if (sender instanceof Shape) {
			final Shape node = (Shape) sender;
			node.setFillColor("yellow");
		}
	}
}
