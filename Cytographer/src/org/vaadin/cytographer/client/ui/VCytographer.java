package org.vaadin.cytographer.client.ui;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.Line;
import org.vaadin.gwtgraphics.client.VectorObject;
import org.vaadin.gwtgraphics.client.shape.Circle;
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
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;

public class VCytographer extends Composite implements Paintable, ClickHandler, DoubleClickHandler, MouseDownHandler, MouseUpHandler,
		MouseMoveHandler, MouseWheelHandler, KeyDownHandler, KeyUpHandler {
	private static final long serialVersionUID = 5554800884802605342L;
	public static final String CLASSNAME = "v-vaadingraph";

	protected String paintableId;
	protected ApplicationConnection client;

	private int graphWidth = 500;
	private int graphHeight = 500;

	private final VGraph graph;
	private final FocusPanel panel;
	private final FocusDrawingArea canvas;
	private final VVisualStyle style = new VVisualStyle();
	private final VSelectionBox selectionBox = new VSelectionBox();

	private Line linkLine;
	private VNode linkNode;
	private VectorObject info;
	private ContextMenu currentMenu;

	private int startY;
	private int startX;
	private float fps = 1;
	private long paintStartTime;
	private float angle = 0;
	private float zoomFactor = 1;
	private boolean onMove = false;
	private boolean onLink = false;
	private final boolean showInfo = true;

	private Set<Integer> currentKeyModifiers;

	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */
	public VCytographer() {
		panel = new FocusPanel();
		panel.setSize(graphWidth + "px", graphHeight + "px");
		panel.addKeyDownHandler(this);
		panel.addKeyUpHandler(this);
		canvas = new FocusDrawingArea(graphWidth, graphHeight);
		canvas.addKeyDownHandler(this);
		canvas.addKeyUpHandler(this);
		graph = new VGraph(this, style, canvas, graphWidth, graphHeight);
		panel.add(canvas);
		canvas.add(graph);
		initWidget(panel);
		setStyleName(CLASSNAME);
		DOM.setStyleAttribute(canvas.getElement(), "border", "1px solid black");
		disableContextMenu(canvas.getElement());
	}

	// should prevent browser's default right click action
	public static native void disableContextMenu(final Element elem) /*-{
																		elem.oncontextmenu=function() {return false};
																		}-*/;

	/**
	 * Called whenever an update is received from the server
	 */
	@Override
	public void updateFromUIDL(final UIDL uidl, final ApplicationConnection client) {
		if (client.updateComponent(this, uidl, true)) {
			return;
		}
		this.client = client;
		paintableId = uidl.getId();
		currentKeyModifiers = new HashSet<Integer>();
		selectionBox.setSelectionBoxVisible(false);

		graphWidth = uidl.getIntAttribute("gwidth");
		graphHeight = uidl.getIntAttribute("gheight");
		style.parseGeneralStyleAttributesFromUidl(uidl);
		initializeCanvas();

		final String operation = uidl.getStringAttribute("operation");
		if ("REPAINT".equals(operation)) {
			graph.repaintGraph(uidl);
		} else if ("SET_NODE_SIZE".equals(operation)) {
			style.setNodeSize(uidl.getIntAttribute("ns") / 2);
			graph.updateGraphProperties(style);
			paintGraph();
		} else if ("SET_VISUAL_STYLE".equals(operation)) {
			graph.updateGraphProperties(style);
			paintGraph();
		} else if ("SET_TEXT_VISIBILITY".equals(operation)) {
			style.setTextsVisible(uidl.getBooleanAttribute("texts"));
			graph.updateGraphProperties(style);
			paintGraph();
		} else if ("SET_OPTIMIZED_STYLES".equals(operation)) {
			graph.paintGraph();
		} else if ("UPDATE_NODE".equals(operation)) {
			graph.updateNode(uidl, uidl.getStringAttribute("node"));
		} else {
			graph.repaintGraph(uidl);
		}
	}

	private void initializeCanvas() {
		panel.setSize(50 + 1 + graphWidth + "px", 25 + graphHeight + "px");
		canvas.setWidth(graphWidth);
		canvas.setHeight(graphHeight);
		canvas.getElement().getStyle().setPropertyPx("width", graphWidth);
		canvas.getElement().getStyle().setPropertyPx("height", graphHeight);
		canvas.addMouseUpHandler(this);
		canvas.addMouseDownHandler(this);
		canvas.addMouseMoveHandler(this);
		canvas.addClickHandler(this);
		canvas.addMouseWheelHandler(this);
		canvas.addDoubleClickHandler(this);
	}

	private void paintGraph() {
		selectionBox.setSelectionBoxVisible(false);
		fps = 1f / ((System.currentTimeMillis() - paintStartTime) / 1000f);
		paintStartTime = System.currentTimeMillis();
		if (showInfo) {
			if (info != null) {
				canvas.remove(info);
			}
			canvas.add(info = getInfo());
		}
	}

	private VectorObject getInfo() {
		final NumberFormat df = NumberFormat.getDecimalFormat();
		final String zoom = df.format(zoomFactor);
		final String angl = df.format(angle);
		final String fpss = df.format(fps);

		final Text info = new Text(canvas.getWidth() - 130, 10, "Zoom: " + zoom + " Rot.: " + angl + " Fps " + fpss);
		info.setStrokeOpacity(0);
		info.setFillColor(style.getEdgeColor());
		info.setFontSize(8);
		return info;
	}

	@Override
	public void onMouseDown(final MouseDownEvent event) {
		VConsole.log("onMouseDown");
		extractSelection();
		removeSelectionBox();

		if (currentKeyModifiers.contains(KeyCodes.KEY_CTRL)) {
			selectionBox.setSelectionBoxStartX(event.getX());
			selectionBox.setSelectionBoxStartY(event.getY());
			selectionBox.setSelectionBoxVisible(true);
			VConsole.log("onMouseDown - selection started:" + selectionBox.getSelectionBoxStartX() + ","
					+ selectionBox.getSelectionBoxStartY());
		} else if (event.getSource() instanceof VNode) {
			onMove = false;
		} else if (event.getSource() instanceof DrawingArea) {
			onMove = true;
			startX = event.getX();
			startY = event.getY();
			VConsole.log("onMouseDown - moving");
		} else {
			VConsole.error("onMouseDown - UNKNOWN STATE");
		}
	}

	@Override
	public void onMouseMove(final MouseMoveEvent event) {
		final int currentX = event.getX();
		final int currentY = event.getY();

		if (selectionBox.isSelectionBoxVisible()) {
			selectionBox.drawSelectionBox(canvas, currentX, currentY);
		} else if (graph.getMovedShape() != null) {
			final VNode moved = graph.getMovedShape();
			moved.moveNode(currentX, currentY);
		} else if (onLink) {
			if (linkLine != null) {
				canvas.remove(linkLine);
			}
			linkLine = getLinkLine(currentX, currentY);
			canvas.add(linkLine);
		} else if (onMove && event.getSource().equals(canvas)) {
			graph.moveGraph(startX - currentX, startY - currentY);
			startX = currentX;
			startY = currentY;
		}

	}

	@Override
	public void onMouseUp(final MouseUpEvent event) {
		VConsole.log("onMouseUp");
		extractSelection();
		removeSelectionBox();
		graph.setMovedShape(null);
		onMove = false;
		startX = 0;
		startY = 0;
	}

	private void extractSelection() {
		final int x1 = selectionBox.getSelectionBoxStartX();
		final int y1 = selectionBox.getSelectionBoxStartY();
		if (selectionBox.isSelectionBoxVisible()) {
			final int x2 = x1 + selectionBox.getWidth();
			final int y2 = y1 + selectionBox.getHeight();
			VConsole.log("selectNodesAndEdgesInTheBox: " + x1 + "," + y1 + " " + x2 + "," + y2);
			selectNodesAndEdgesInTheBox(x1, y1, x2, y2);
		}
	}

	private void selectNodesAndEdgesInTheBox(final int startX, final int startY, final int endX, final int endY) {
		for (final VNode node : graph.getSelectedShapes()) {
			graph.setNodeSelected(node, false);
		}
		for (final VEdge edge : graph.getSelectedEdges()) {
			graph.setEdgeSelected(edge, false);
		}
		for (final VNode node : graph.getPaintedShapes()) {
			if (isInArea(node.getX(), node.getY(), startX, startY, endX, endY)) {
				graph.setNodeSelected(node, true);
			}
		}
		for (final VEdge edge : graph.getSelectedEdges()) {
			if (graph.getSelectedShapes().contains(edge.getFirstNode()) && graph.getSelectedShapes().contains(edge.getSecondNode())) {
				graph.setEdgeSelected(edge, true);
			}
		}
		nodeOrEdgeSelectionChanged();
	}

	private boolean isInArea(final int targetX, final int targetY, final int x1, final int y1, final int x2, final int y2) {
		return targetX >= x1 && targetX <= x2 && targetY >= y1 && targetY <= y2;
	}

	private void removeSelectionBox() {
		canvas.remove(selectionBox);
		selectionBox.setSelectionBoxVisible(false);
		VConsole.log("selection box removed from canvas");
		selectionBox.setSelectionBoxRightHandSide(true);
	}

	private Line getLinkLine(final int x, final int y) {
		final Line linkLine = new Line(startX, startY, x, y);
		linkLine.setStrokeColor(style.getEdgeColor());
		linkLine.setStrokeOpacity(0.55);
		return linkLine;
	}

	private void removeLinkLine() {
		if (linkLine != null) {
			canvas.remove(linkLine);
			linkLine = null;
		}
		onLink = false;
	}

	@Override
	public void onClick(final ClickEvent event) {
		VConsole.log("onClick");
		if (onLink) {
			if (event.getSource() instanceof VNode) {
				final VNode node2 = (VNode) event.getSource();
				client.updateVariable(paintableId, "edgeCreated", new String[] { linkNode.getName(), node2.getName() }, true);
				final String name = linkNode.getName() + "_to_" + node2.getName() + "_" + new Random().nextInt(1000);
				final VEdge edge = VEdge.createAnEdge(null, this, graph, name, linkNode, node2, style);
				graph.addEdge(edge);
			}
		}
		removeLinkLine();
		onMove = false;
		removeMenu();
		extractSelection();
		removeSelectionBox();
	}

	public void nodeOrEdgeSelectionChanged() {
		final String[] edges = new String[graph.getSelectedEdges().size()];
		final String[] nodes = new String[graph.getSelectedShapes().size()];
		int i = 0;
		for (final VEdge vedge : graph.getSelectedEdges()) {
			edges[i] = vedge.toString();
			++i;
		}
		i = 0;
		for (final VNode vnode : graph.getSelectedShapes()) {
			nodes[i] = vnode.toString();
			++i;
		}
		client.updateVariable(paintableId, "selectedEdges", edges, false);
		client.updateVariable(paintableId, "selectedNodes", nodes, true);
	}

	@Override
	public void onMouseWheel(final MouseWheelEvent event) {
		final int delta = event.getDeltaY();
		if (currentKeyModifiers.contains(KeyCodes.KEY_CTRL)) {
			if (delta < 0) {
				rotate(0.035);
			} else if (delta > 0) {
				rotate(-0.035);
			}
		} else {
			if (delta < 0) {
				zoom(1.05);
			} else if (delta > 0) {
				zoom(0.95);
			}
		}
		paintGraph();
	}

	private void rotate(final double delta) {
		for (final VNode n : graph.getPaintedShapes()) {
			final int newX = (int) (n.getX() * Math.cos(delta) - n.getY() * Math.sin(delta));
			final int newY = (int) (n.getX() * Math.sin(delta) + n.getY() * Math.cos(delta));
			n.setX(newX);
			n.setY(newY);
			graph.updateEdges(n, false);
		}
		angle += delta;
	}

	private void zoom(final double delta) {
		for (final VNode n : graph.getPaintedShapes()) {
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
			graph.updateEdges(n, false);
		}
		zoomFactor += delta;
		client.updateVariable(paintableId, "zoomFactor", zoomFactor, false);
	}

	@Override
	public void onKeyDown(final KeyDownEvent event) {
		currentKeyModifiers.add(event.getNativeKeyCode());
	}

	@Override
	public void onKeyUp(final KeyUpEvent event) {
		VConsole.log("KeyUpEvent");
		if (currentKeyModifiers.contains(KeyCodes.KEY_CTRL)) {
			removeSelectionBox();
		}
		currentKeyModifiers.clear();
	}

	@Override
	public void onDoubleClick(final DoubleClickEvent event) {
		final int x = event.getX();
		final int y = event.getY();
		final VNode node = VNode.createANode(x, y, this, graph, style);
		client.updateVariable(paintableId, "createdANode", new Object[] { node.getName(), x, y }, false);
	}

	public void removeMenu() {
		if (currentMenu != null) {
			currentMenu.hide();
			currentMenu = null;
		}
	}

	public void setCurrentMenu(final ContextMenu currentMenu) {
		this.currentMenu = currentMenu;
	}

	public ContextMenu getCurrentMenu() {
		return currentMenu;
	}

	public void editNode(final VNode node) {
		// TODO Auto-generated method stub

	}

	public void linkNode(final VNode node) {
		VConsole.log("onlink");
		onLink = true;
		linkNode = node;
		startX = node.getX();
		startY = node.getY();
	}

	public void deleteNode(final VNode node) {
		client.updateVariable(paintableId, "removedNode", node.getName(), true);
		graph.removeNode(node);
	}
}