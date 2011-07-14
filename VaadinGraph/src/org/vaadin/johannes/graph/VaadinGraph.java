package org.vaadin.johannes.graph;

import giny.model.Edge;
import giny.model.Node;

import java.awt.Color;
import java.util.Map;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

import cytoscape.CyNetwork;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualStyle;

/**
 * Server side component for the VMyComponent widget.
 */
@com.vaadin.ui.ClientWidget(org.vaadin.johannes.graph.client.ui.VVaadinGraph.class)
public class VaadinGraph extends AbstractComponent {
	private static final long serialVersionUID = 8483008141219579936L;
	private final String title;
	private final CyNetwork network;
	private final int[] edges;
	private final CyNetworkView finalView;
	private int width;
	private int height;

	private int maxX = Integer.MIN_VALUE;
	private int minX = Integer.MAX_VALUE;
	private int minY = Integer.MAX_VALUE;
	private int maxY = Integer.MIN_VALUE;

	private double cytoscapeViewWidth = 0;
	private double cytoscapeViewHeight = 0;

	@Override
	public void paintContent(final PaintTarget target) throws PaintException {
		super.paintContent(target);
		target.addAttribute("title", title);
		target.addAttribute("gwidth", width);
		target.addAttribute("gheight", height);

		final VisualStyle vs = finalView.getVisualStyle();
		final Color bc = vs.getGlobalAppearanceCalculator().getDefaultBackgroundColor();
		final Color ec = (Color) vs.getEdgeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.EDGE_COLOR);
		final Float elw = (Float) vs.getEdgeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.EDGE_LINE_WIDTH);
		final Color nbc = (Color) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.NODE_BORDER_COLOR);
		final Color nfc = (Color) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.NODE_FILL_COLOR);
		final Float nbw = (Float) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.NODE_LINE_WIDTH);
		final Double ns = (Double) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.NODE_SIZE);
		final Color nsc = vs.getGlobalAppearanceCalculator().getDefaultNodeSelectionColor();
		final Color esc = vs.getGlobalAppearanceCalculator().getDefaultEdgeSelectionColor();

		target.addAttribute("bc", "rgb(" + bc.getRed() + "," + bc.getGreen() + "," + bc.getBlue() + ")");
		target.addAttribute("ec", "rgb(" + ec.getRed() + "," + ec.getGreen() + "," + ec.getBlue() + ")");
		target.addAttribute("elw", elw.intValue());
		target.addAttribute("nbc", "rgb(" + nbc.getRed() + "," + nbc.getGreen() + "," + nbc.getBlue() + ")");
		target.addAttribute("nfc", "rgb(" + nfc.getRed() + "," + nfc.getGreen() + "," + nfc.getBlue() + ")");
		target.addAttribute("nsc", "rgb(" + nsc.getRed() + "," + nsc.getGreen() + "," + nsc.getBlue() + ")");
		target.addAttribute("esc", "rgb(" + esc.getRed() + "," + esc.getGreen() + "," + esc.getBlue() + ")");
		target.addAttribute("nbw", nbw.intValue());
		target.addAttribute("ns", ns.intValue());

		for (final int ei : edges) {
			final Edge e = network.getEdge(ei);
			final Node node1 = e.getSource();
			final Node node2 = e.getTarget();
			target.startTag("e");
			target.addAttribute("name", e.getIdentifier());
			target.addAttribute("node1", node1.getIdentifier());
			target.addAttribute("node2", node2.getIdentifier());

			final double xx1 = finalView.getNodeView(node1).getXPosition();
			final double yy1 = finalView.getNodeView(node1).getYPosition();
			final double xx2 = finalView.getNodeView(node2).getXPosition();
			final double yy2 = finalView.getNodeView(node2).getYPosition();

			final int x1 = (int) (((xx1 - minX) / cytoscapeViewWidth) * width);
			final int y1 = (int) (((yy1 - minY) / cytoscapeViewHeight) * height);
			final int x2 = (int) (((xx2 - minX) / cytoscapeViewWidth) * width);
			final int y2 = (int) (((yy2 - minY) / cytoscapeViewHeight) * height);

			target.addAttribute("node1x", x1);
			target.addAttribute("node1y", y1);
			target.addAttribute("node2x", x2);
			target.addAttribute("node2y", y2);

			target.endTag("e");
		}
	}

	/**
	 * Receive and handle events and other variable changes from the client.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void changeVariables(final Object source, final Map<String, Object> variables) {
		super.changeVariables(source, variables);
	}

	public VaadinGraph(final CyNetwork network, final CyNetworkView finalView, final String title, final int width, final int height) {
		this.title = title;
		this.network = network;
		this.finalView = finalView;
		this.width = width;
		this.height = height;
		edges = network.getEdgeIndicesArray();
		measureDimensions(finalView);
	}

	private void measureDimensions(final CyNetworkView finalView2) {
		for (final int ei : edges) {
			final Edge e = network.getEdge(ei);
			final Node node1 = e.getSource();
			final Node node2 = e.getTarget();
			final int x1 = (int) (finalView.getNodeView(node1).getXPosition());
			final int y1 = (int) (finalView.getNodeView(node1).getYPosition());
			final int x2 = (int) (finalView.getNodeView(node2).getXPosition());
			final int y2 = (int) (finalView.getNodeView(node2).getYPosition());
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

	public void setWidthAndHeight(final int width, final int height) {
		this.width = width;
		this.height = height;
		requestRepaint();
	}
}
