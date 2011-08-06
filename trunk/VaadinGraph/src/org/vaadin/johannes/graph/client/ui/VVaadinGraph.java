package org.vaadin.johannes.graph.client.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.Shape;
import org.vaadin.gwtgraphics.client.shape.Circle;
import org.vaadin.gwtgraphics.client.shape.Rectangle;
import org.vaadin.gwtgraphics.client.shape.Text;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
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
public class VVaadinGraph extends Composite implements Paintable, ClickHandler, MouseDownHandler, MouseUpHandler, MouseMoveHandler,
		MouseWheelHandler {

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
	private Map<String, VNode> nodes = new HashMap<String, VNode>();
	private Map<VNode, Set<VEdge>> shapeToEdgesMap = new HashMap<VNode, Set<VEdge>>();
	private final Set<VNode> paintedShapes = new HashSet<VNode>();
	private final Set<VNode> selectedShapes = new HashSet<VNode>();
	private boolean skipEvents;

	private String bgColor;
	private String edgeColor;
	private String nodeBorderColor;
	private String nodeFillColor;
	private String nodeSelectionColor;
	private String edgeSelectionColor;
	private String nodeLabelColor;
	private String edgeLabelColor;
	private final String fontFamily = "Times New Roman Regular";

	private int edgeLineWidth;
	private int nodeBorderWidth;
	private int nodeSize;
	private int nodeFontSize;
	private int edgeFontSize;

	private VNode movedShape = null;

	private int startY;
	private int startX;
	private boolean onMove = false;
	private boolean textsVisible = true;

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

		// general style attributes
		gwidth = uidl.getIntAttribute("gwidth");
		gheight = uidl.getIntAttribute("gheight");
		textsVisible = uidl.getBooleanAttribute("texts");
		bgColor = uidl.getStringAttribute("bc");
		edgeColor = uidl.getStringAttribute("ec");
		nodeBorderColor = uidl.getStringAttribute("nbc");
		nodeFillColor = uidl.getStringAttribute("nfc");
		edgeLineWidth = uidl.getIntAttribute("elw");
		nodeBorderWidth = uidl.getIntAttribute("nbw");
		nodeSize = uidl.getIntAttribute("ns") / 2;
		nodeSelectionColor = uidl.getStringAttribute("nsc");
		edgeSelectionColor = uidl.getStringAttribute("esc");
		nodeLabelColor = uidl.getStringAttribute("nlc");
		edgeLabelColor = uidl.getStringAttribute("elc");
		nodeFontSize = uidl.getIntAttribute("nfs");
		edgeFontSize = uidl.getIntAttribute("efs");

		panel.setSize((50 + 1 + gwidth) + "px", (25 + gheight) + "px");
		canvas.setWidth(gwidth);
		canvas.setHeight(gheight);
		canvas.getElement().getStyle().setPropertyPx("width", gwidth);
		canvas.getElement().getStyle().setPropertyPx("height", gheight);
		canvas.addMouseUpHandler(this);
		canvas.addMouseDownHandler(this);
		canvas.addMouseMoveHandler(this);
		canvas.addMouseWheelHandler(this);

		edges = new HashMap<String, VEdge>();
		nodes = new HashMap<String, VNode>();
		shapeToEdgesMap = new HashMap<VNode, Set<VEdge>>();

		for (int i = 0; i < uidl.getChildCount(); i++) {
			final UIDL child = uidl.getChildUIDL(i);
			final String name = child.getStringAttribute("name");
			final String node1name = child.getStringAttribute("node1");
			final String node2name = child.getStringAttribute("node2");

			VNode node1 = nodes.get(node1name);
			VNode node2 = nodes.get(node2name);
			if (node1 == null) {
				final Circle shape = new Circle(child.getIntAttribute("node1x"), child.getIntAttribute("node1y"), nodeSize);
				shape.setFillColor(nodeFillColor);
				shape.setStrokeColor(nodeBorderColor);
				shape.setStrokeWidth(nodeBorderWidth);
				final VNode node = new VNode(shape, node1name);
				node.setLabelColor(nodeLabelColor);
				node.setFontSize(nodeFontSize);
				node.setFontFamily(fontFamily);
				node.setTextVisible(textsVisible);

				// node specific styles
				if (child.hasAttribute("_n1bc")) {
					shape.setStrokeColor(child.getStringAttribute("_n1bc"));
				}
				if (child.hasAttribute("_n1fc")) {
					shape.setFillColor(child.getStringAttribute("_n1fc"));
				}
				if (child.hasAttribute("_n1bw")) {
					shape.setStrokeWidth(child.getIntAttribute("_n1bw"));
				}
				if (child.hasAttribute("_n1s")) {
					shape.setRadius(child.getIntAttribute("_n1s") / 2);
				}

				node.addClickHandler(this);
				node.addMouseDownHandler(this);
				node.addMouseUpHandler(this);
				node.addMouseMoveHandler(this);
				nodes.put(node1name, node);
				node1 = node;
			}
			if (node2 == null) {
				final Circle shape = new Circle(child.getIntAttribute("node2x"), child.getIntAttribute("node2y"), nodeSize);
				shape.setFillColor(nodeFillColor);
				shape.setStrokeColor(nodeBorderColor);
				shape.setStrokeWidth(nodeBorderWidth);
				final VNode node = new VNode(shape, node2name);
				node.setLabelColor(nodeLabelColor);
				node.setFontSize(nodeFontSize);
				node.setFontFamily(fontFamily);
				node.setTextVisible(textsVisible);

				// node specific styles
				if (child.hasAttribute("_n2bc")) {
					shape.setStrokeColor(child.getStringAttribute("_n2bc"));
				}
				if (child.hasAttribute("_n2fc")) {
					shape.setFillColor(child.getStringAttribute("_n2fc"));
				}
				if (child.hasAttribute("_n2bw")) {
					shape.setStrokeWidth(child.getIntAttribute("_n2bw"));
				}
				if (child.hasAttribute("_n2s")) {
					shape.setRadius(child.getIntAttribute("_n2s") / 2);
				}

				node.addClickHandler(this);
				node.addMouseDownHandler(this);
				node.addMouseUpHandler(this);
				node.addMouseMoveHandler(this);
				nodes.put(node2name, node);
				node2 = node;
			}
			String str = "";
			if (name.indexOf("(") != -1 && name.indexOf(")") != -1) {
				str = name.substring(name.indexOf("(") + 1, name.indexOf(")"));
			}
			final Text text = new Text((node1.getX() + node2.getX()) / 2, (node1.getY() + node2.getY()) / 2, str);
			text.setFontSize(edgeFontSize);
			text.setFontFamily(fontFamily);
			text.setStrokeOpacity(0);
			text.setFillOpacity(1);
			text.setFillColor(edgeLabelColor);
			final VEdge edge = new VEdge(node1, node2, text);
			edge.setStrokeColor(edgeColor);
			edge.setStrokeWidth(edgeLineWidth);
			// edge specific style attributes
			if (child.hasAttribute("_ec")) {
				edge.setStrokeColor(child.getStringAttribute("_ec"));
			}
			if (child.hasAttribute("_elw")) {
				edge.setStrokeWidth(child.getIntAttribute("_elw"));
			}

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
			final Rectangle bg = new Rectangle(0, 0, gwidth, gheight);
			bg.setFillColor(bgColor);
			bg.setStrokeColor(bgColor);
			canvas.add(bg);
			for (final Map.Entry<String, VEdge> entry : edges.entrySet()) {
				final VEdge edge = entry.getValue();
				canvas.add(edge);
				if (textsVisible) {
					canvas.add(edge.getText());
				}
			}
			for (final Map.Entry<String, VEdge> entry : edges.entrySet()) {
				final VEdge edge = entry.getValue();
				final VNode n1 = edge.getSecondNode();
				if (!paintedShapes.contains(n1) && isInPaintedArea(n1)) {
					canvas.add(n1);
					paintedShapes.add(n1);
				}
				final VNode n2 = edge.getFirstNode();
				if (!paintedShapes.contains(n2) && isInPaintedArea(n1)) {
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

	private boolean isInPaintedArea(final VNode n1) {
		// TODO
		return true;
	}

	private void updateEdges(final VNode node, final boolean repaint) {
		final Set<VEdge> edgs = shapeToEdgesMap.get(node);
		for (final VEdge e : edgs) {
			if (e.getFirstNode().equals(node)) {
				e.setX1(node.getX());
				e.setY1(node.getY());
				e.getText().setX((node.getX() + e.getSecondNode().getX()) / 2);
				e.getText().setY((node.getY() + e.getSecondNode().getY()) / 2);
			} else {
				e.setX2(node.getX());
				e.setY2(node.getY());
				e.getText().setX((e.getFirstNode().getX() + node.getX()) / 2);
				e.getText().setY((e.getFirstNode().getY() + node.getY()) / 2);
			}
			if (repaint) {
				canvas.remove(e);
				if (textsVisible) {
					canvas.remove(e.getText());
				}
				canvas.add(e);
				if (textsVisible) {
					canvas.add(e.getText());
				}
				if (e.getFirstNode().equals(node)) {
					canvas.remove(e.getSecondNode());
					canvas.add(e.getSecondNode());
				} else {
					canvas.remove(e.getFirstNode());
					canvas.add(e.getFirstNode());
				}
			}
		}
	}

	public void onMouseMove(final MouseMoveEvent event) {
		if (movedShape != null) {
			final int x = event.getClientX() - 15;
			final int y = event.getClientY() - 60;
			movedShape.setX(x);
			movedShape.setY(y);
			updateEdges(movedShape, true);
			canvas.remove(movedShape);
			canvas.add(movedShape);
		} else if (onMove && event.getSource().equals(canvas)) {
			VConsole.log("onMouseMove");
			moveGraph(startX - event.getX(), startY - event.getY());
			startX = event.getX();
			startY = event.getY();
		}
	}

	public void onMouseUp(final MouseUpEvent event) {
		VConsole.log("mouse up");
		movedShape = null;
		onMove = false;
		startX = 0;
		startY = 0;
	}

	public void onMouseDown(final MouseDownEvent event) {
		final Object sender = event.getSource();
		if (sender instanceof VNode) {
			final VNode node = (VNode) sender;
			movedShape = node;
			VConsole.log("mouse down on shape");
		} else if (sender instanceof DrawingArea) {
			onMove = true;
			startX = event.getX();
			startY = event.getY();
		}
	}

	public void onClick(final ClickEvent event) {
		movedShape = null;
		if (skipEvents) {
			return;
		}
		final Object sender = event.getSource();
		if (sender instanceof VNode) {
			if (selectedShapes.contains(sender)) {
				final VNode node = (VNode) sender;
				node.setFillColor(nodeFillColor);
				selectedShapes.remove(node);
			} else {
				final VNode node = (VNode) sender;
				node.setFillColor(nodeSelectionColor);
				selectedShapes.add(node);
			}
		}
	}

	public void onMouseWheel(final MouseWheelEvent event) {
		final int delta = event.getDeltaY();
		if (delta < 0) {
			zoomIn(event.getX(), event.getY());
		} else if (delta > 0) {
			zoomOut(event.getX(), event.getY());
		}
		paintGraph();
	}

	private void zoomIn(final int x, final int y) {
		for (final VNode n : paintedShapes) {
			n.setX((int) (n.getX() * 1.1));
			n.setY((int) (n.getY() * 1.1));
			if (n.getView() instanceof Circle) {
				((Circle) n.getView()).setRadius((((Circle) n.getView()).getRadius() + 1));
			}
			updateEdges(n, false);
		}

	}

	private void zoomOut(final int x, final int y) {
		for (final VNode n : paintedShapes) {
			n.setX((int) (n.getX() * 0.9));
			n.setY((int) (n.getY() * 0.9));
			if (n.getView() instanceof Circle) {
				((Circle) n.getView()).setRadius((((Circle) n.getView()).getRadius() - 1));
			}
			updateEdges(n, false);
		}
	}

	private void moveGraph(final int x, final int y) {
		for (final VNode shape : paintedShapes) {
			shape.setX(shape.getX() - x);
			shape.setY(shape.getY() - y);
			updateEdges(shape, false);
		}
		paintGraph();
	}

	private void moveNode(final VNode node, final double x, final double y) {
		node.setX(node.getX() - (int) x);
		node.setY(node.getY() - (int) y);
	}

}
