/* 
 * Copyright 2011 Johannes Tuikkala <johannes@vaadin.com>
 *                           LICENCED UNDER
 *                  GNU LESSER GENERAL PUBLIC LICENSE
 *                     Version 3, 29 June 2007
 */
package org.vaadin.johannes.graph.client.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.Shape;
import org.vaadin.gwtgraphics.client.VectorObject;
import org.vaadin.gwtgraphics.client.shape.Circle;
import org.vaadin.gwtgraphics.client.shape.Rectangle;
import org.vaadin.gwtgraphics.client.shape.Text;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;

/**
 * Client side widget which communicates with the server. Messages from the
 * server are shown as HTML and mouse clicks are sent to the server.
 */
public class VVaadinGraph extends Composite implements Paintable, ClickHandler, DoubleClickHandler, MouseDownHandler, MouseUpHandler,
		MouseMoveHandler, MouseWheelHandler, KeyDownHandler, KeyUpHandler {

	/** Set the CSS class name to allow styling. */
	public static final String CLASSNAME = "v-vaadingraph";

	public static final String CLICK_EVENT_IDENTIFIER = "click";

	/** The client side widget identifier */
	protected String paintableId;

	/** Reference to the server connection object. */
	protected ApplicationConnection client;

	private final FocusDrawingArea canvas;
	private int gwidth = 500;
	private int gheight = 500;
	private final FocusPanel panel;

	private Map<String, VEdge> edges = new HashMap<String, VEdge>();
	private Map<String, VNode> nodes = new HashMap<String, VNode>();
	private Map<VNode, Set<VEdge>> shapeToEdgesMap = new HashMap<VNode, Set<VEdge>>();
	private final Set<VNode> paintedShapes = new HashSet<VNode>();
	private final Set<VNode> selectedShapes = new HashSet<VNode>();
	private final Set<VEdge> selectedEdges = new HashSet<VEdge>();
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
	private final boolean showInfo = true;

	private Set<Integer> currentKeyModifiers;
	private int selectionBoxStartY;
	private int selectionBoxStartX;
	private int selectionBoxStartYold;
	private int selectionBoxStartXold;
	private Rectangle selectionBox;
	private boolean selectionBoxVisible = false;
	private boolean selectionBoxRightHandSide = true;

	private float angle = 0;
	private float zoomFactor = 1;
	private float fps = 1;

	private VectorObject info;

	private final boolean trackMouseMoveEvents = true;

	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */
	public VVaadinGraph() {
		panel = new FocusPanel();
		panel.setSize(gwidth + "px", gheight + "px");
		panel.addKeyDownHandler(this);
		panel.addKeyUpHandler(this);
		canvas = new FocusDrawingArea(gwidth, gheight);
		canvas.addKeyDownHandler(this);
		canvas.addKeyUpHandler(this);
		panel.add(canvas);
		initWidget(panel);
		setStyleName(CLASSNAME);
		DOM.setStyleAttribute(canvas.getElement(), "border", "1px solid black");
	}

	/**
	 * Called whenever an update is received from the server
	 */
	public void updateFromUIDL(final UIDL uidl, final ApplicationConnection client) {
		if (client.updateComponent(this, uidl, true)) {
			return;
		}
		this.client = client;
		paintableId = uidl.getId();
		currentKeyModifiers = new HashSet<Integer>();

		/*
		 * final Timer t = new Timer() {
		 * 
		 * @Override public void run() { trackMouseMoveEvents = true; } };
		 * t.scheduleRepeating(100);
		 */

		parseGeneralStyleAttributes(uidl);
		initializeCanvas();
		parseGraphFromUIDL(uidl);
		paintGraph();
	}

	private void parseGraphFromUIDL(final UIDL uidl) {
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
				node1 = createANode(child, node1name, true);
				nodes.put(node1name, node1);
			}
			if (node2 == null) {
				node2 = createANode(child, node2name, false);
				nodes.put(node2name, node2);
			}
			final VEdge edge = createAnEdge(child, name, node1, node2);
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
	}

	private VEdge createAnEdge(final UIDL child, final String name, final VNode node1, final VNode node2) {
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
			edge.setOriginalStrokeColor(edge.getStrokeColor());
		}
		if (child.hasAttribute("_elw")) {
			edge.setStrokeWidth(child.getIntAttribute("_elw"));
		}
		edge.addClickHandler(this);
		edge.addMouseDownHandler(this);
		edge.addMouseUpHandler(this);
		edge.addMouseMoveHandler(this);
		return edge;
	}

	private VNode createANode(final UIDL child, final String nodeName, final boolean firstNode) {
		Circle shape = null;
		if (firstNode) {
			shape = new Circle(child.getIntAttribute("node1x"), child.getIntAttribute("node1y"), nodeSize);
		} else {
			shape = new Circle(child.getIntAttribute("node2x"), child.getIntAttribute("node2y"), nodeSize);
		}
		shape.setFillColor(nodeFillColor);
		shape.setStrokeColor(nodeBorderColor);
		shape.setStrokeWidth(nodeBorderWidth);

		final VNode node = new VNode(shape, nodeName);
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
			node.setOriginalFillColor(shape.getFillColor());
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
		return node;
	}

	private void initializeCanvas() {
		panel.setSize((50 + 1 + gwidth) + "px", (25 + gheight) + "px");
		canvas.setWidth(gwidth);
		canvas.setHeight(gheight);
		canvas.getElement().getStyle().setPropertyPx("width", gwidth);
		canvas.getElement().getStyle().setPropertyPx("height", gheight);
		canvas.addMouseUpHandler(this);
		canvas.addMouseDownHandler(this);
		canvas.addMouseMoveHandler(this);
		canvas.addClickHandler(this);
		canvas.addMouseWheelHandler(this);
		canvas.addDoubleClickHandler(this);
	}

	private void parseGeneralStyleAttributes(final UIDL uidl) {
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
	}

	private void paintGraph(final Shape... updatedShapes) {
		final long st = System.currentTimeMillis();
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
		if (showInfo) {
			if (info != null) {
				canvas.remove(info);
			}
			canvas.add(info = getInfo());
		}
		fps = 1f / ((System.currentTimeMillis() - st) / 1000f);
	}

	private VectorObject getInfo() {
		final NumberFormat df = NumberFormat.getDecimalFormat();
		final String zoom = df.format(zoomFactor);
		final String angl = df.format(angle);
		final String fpss = df.format(fps);

		final Text info = new Text(canvas.getWidth() - 130, 10, "Zoom: " + zoom + " Rot.: " + angl + " Fps " + fpss);
		info.setStrokeOpacity(0);
		info.setFillColor(edgeLabelColor);
		info.setFontSize(8);
		return info;
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
		if (trackMouseMoveEvents) {
			// trackMouseMoveEvents = false;
			final int currentX = event.getX();
			final int currentY = event.getY();

			if (selectionBoxVisible) {
				drawSelectionBox(currentX, currentY);
			} else if (movedShape != null) {
				movedShape.setX(currentX);
				movedShape.setY(currentY);
				updateEdges(movedShape, true);
				canvas.remove(movedShape);
				canvas.add(movedShape);
			} else if (onMove && event.getSource().equals(canvas)) {
				VConsole.log("onMouseMove");
				moveGraph(startX - currentX, startY - currentY);
				startX = currentX;
				startY = currentY;
			}
		}
	}

	private void drawSelectionBox(final int currentX, final int currentY) {
		if (selectionBox != null) {
			canvas.remove(selectionBox);
		}
		int width = Math.abs(selectionBoxStartX - currentX);
		int height = Math.abs(selectionBoxStartY - currentY);

		if (selectionBoxRightHandSide && (currentX < selectionBoxStartX - 1 || currentY < selectionBoxStartY - 1)) {
			selectionBoxRightHandSide = false;
			selectionBoxStartXold = selectionBoxStartX;
			selectionBoxStartYold = selectionBoxStartY;
		} else if (!selectionBoxRightHandSide && (currentX > selectionBoxStartX + 1 || currentY > selectionBoxStartY + 1)) {
			selectionBoxRightHandSide = true;
			selectionBoxStartX = selectionBoxStartXold;
			selectionBoxStartY = selectionBoxStartYold;
		}
		if (!selectionBoxRightHandSide) {
			width = Math.abs(selectionBoxStartXold - currentX);
			height = Math.abs(selectionBoxStartYold - currentY);
			selectionBoxStartX = currentX;
			selectionBoxStartY = currentY;
		}
		selectionBox = new Rectangle(selectionBoxStartX, selectionBoxStartY, width, height);
		selectionBox.setStrokeWidth(1);
		selectionBox.setStrokeOpacity(0.5);
		selectionBox.setStrokeColor("#ff0000");
		selectionBox.setFillOpacity(0.09);
		canvas.add(selectionBox);
		selectionBoxVisible = true;
	}

	public void onMouseUp(final MouseUpEvent event) {
		extractSelection();
		removeSelectionBox();
		movedShape = null;
		onMove = false;
		startX = 0;
		startY = 0;
	}

	private void extractSelection() {
		final int x1 = selectionBoxStartX;
		final int y1 = selectionBoxStartY;
		if (selectionBox != null && selectionBoxVisible) {
			selectNodesAndEdgesInTheBox(x1, y1, x1 + selectionBox.getWidth(), y1 + selectionBox.getHeight());
		}
	}

	public void onMouseDown(final MouseDownEvent event) {
		final Object sender = event.getSource();
		extractSelection();
		removeSelectionBox();
		if (currentKeyModifiers.contains(KeyCodes.KEY_CTRL)) {
			selectionBoxStartX = event.getX();
			selectionBoxStartY = event.getY();
			selectionBoxVisible = true;
		} else if (sender instanceof VNode) {
			final VNode node = (VNode) sender;
			movedShape = node;
			VConsole.log("mouse down on shape");
		} else if (sender instanceof DrawingArea) {
			onMove = true;
			startX = event.getX();
			startY = event.getY();
		}
	}

	private void selectNodesAndEdgesInTheBox(final int startX, final int startY, final int endX, final int endY) {
		for (final VNode node : selectedShapes) {
			setNodeSelected(node, false);
		}
		for (final VEdge edge : selectedEdges) {
			setEdgeSelected(edge, false);
		}
		for (final VNode node : paintedShapes) {
			if (isInArea(node.getX(), node.getY(), startX, startY, endX, endY)) {
				setNodeSelected(node, true);
			}
		}
		for (final VEdge edge : selectedEdges) {
			if (selectedShapes.contains(edge.getFirstNode()) && selectedShapes.contains(edge.getSecondNode())) {
				setEdgeSelected(edge, true);
			}
		}
		nodeOrEdgeSelectionChanged();
	}

	private boolean isInArea(final int targetX, final int targetY, final int x1, final int y1, final int x2, final int y2) {
		return (targetX >= x1 && targetX <= x2) && (targetY >= y1 && targetY <= y2);
	}

	private void removeSelectionBox() {
		if (selectionBox != null) {
			canvas.remove(selectionBox);
			selectionBox = null;
			selectionBoxVisible = false;
		}
		if (selectionBoxVisible) {
			selectionBoxVisible = false;
		}
		selectionBoxRightHandSide = true;
	}

	public void onClick(final ClickEvent event) {
		extractSelection();
		removeSelectionBox();
		movedShape = null;
		if (skipEvents) {
			return;
		}
		final Object sender = event.getSource();
		if (sender instanceof VNode) {
			setNodeSelected((VNode) sender, !selectedShapes.contains(sender));
			nodeOrEdgeSelectionChanged();
		} else if (sender instanceof VEdge) {
			setEdgeSelected((VEdge) sender, selectedEdges.contains(sender));
			nodeOrEdgeSelectionChanged();
		}
	}

	private void setNodeSelected(final VNode node, final boolean selected) {
		if (selected) {
			node.setFillColor(nodeSelectionColor);
			selectedShapes.add(node);
		} else {
			node.setFillColor(node.getOriginalFillColor());
			selectedShapes.remove(node);
		}
	}

	private void setEdgeSelected(final VEdge edge, final boolean selected) {
		if (selected) {
			edge.setStrokeColor(edgeSelectionColor);
			selectedEdges.add(edge);
		} else {
			edge.setStrokeColor(edge.getOrginalStrokeColor());
			selectedEdges.remove(edge);
		}
	}

	private void nodeOrEdgeSelectionChanged() {
		final String[] edges = new String[selectedEdges.size()];
		final String[] nodes = new String[selectedShapes.size()];
		int i = 0;
		for (final VEdge vedge : selectedEdges) {
			edges[i] = vedge.toString();
			++i;
		}
		i = 0;
		for (final VNode vnode : selectedShapes) {
			nodes[i] = vnode.toString();
			++i;
		}
		client.updateVariable(paintableId, "selectedEdges", edges, false);
		client.updateVariable(paintableId, "selectedNodes", nodes, true);
	}

	public void onMouseWheel(final MouseWheelEvent event) {
		final int delta = event.getDeltaY();
		if (currentKeyModifiers.contains(KeyCodes.KEY_CTRL)) {
			VConsole.log("rotation: " + angle);
			if (delta < 0) {
				rotate(0.025);
			} else if (delta > 0) {
				rotate(-0.025);
			}
		} else {
			VConsole.log("zooming");
			if (delta < 0) {
				zoom(1.025);
			} else if (delta > 0) {
				zoom(0.925);
			}
		}
		paintGraph();
	}

	private void rotate(final double delta) {
		for (final VNode n : paintedShapes) {
			final int newX = ((int) (n.getX() * Math.cos(delta) - n.getY() * Math.sin(delta)));
			final int newY = ((int) (n.getX() * Math.sin(delta) + n.getY() * Math.cos(delta)));
			n.setX(newX);
			n.setY(newY);
			updateEdges(n, false);
		}
		angle += delta;
	}

	private void zoom(final double delta) {
		for (final VNode n : paintedShapes) {
			n.setX((int) (n.getX() * delta));
			n.setY((int) (n.getY() * delta));
			if (n.getView() instanceof Circle) {
				/*
				 * if (delta > 1) { ((Circle) n.getView()).setRadius((((Circle)
				 * n.getView()).getRadius() + 1)); } else { ((Circle)
				 * n.getView()).setRadius((((Circle) n.getView()).getRadius() -
				 * 1)); }
				 */
			}
			updateEdges(n, false);
		}
		zoomFactor += delta;
		client.updateVariable(paintableId, "zoomFactor", zoomFactor, false);
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

	public void onKeyDown(final KeyDownEvent event) {
		currentKeyModifiers.add(event.getNativeKeyCode());
	}

	public void onKeyUp(final KeyUpEvent event) {
		if (currentKeyModifiers.contains(KeyCodes.KEY_CTRL)) {
			removeSelectionBox();
		}
		currentKeyModifiers.remove(event.getNativeKeyCode());
	}

	public void onDoubleClick(final DoubleClickEvent event) {
		// TODO
	}
}
