package org.vaadin.johannes.graph;

import giny.model.Edge;
import giny.model.Node;

import java.awt.Color;
import java.util.Map;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.EdgeAppearance;
import cytoscape.visual.NodeAppearance;
import cytoscape.visual.VisualMappingManager;
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
	private boolean textsVisible = false;
	private boolean styleOptimization;
	private final boolean useFitting = false;

	@Override
	public void paintContent(final PaintTarget target) throws PaintException {
		super.paintContent(target);
		target.addAttribute("title", title);
		target.addAttribute("gwidth", width);
		target.addAttribute("gheight", height);
		target.addAttribute("texts", textsVisible);

		final VisualMappingManager vizmapper = Cytoscape.getVisualMappingManager();
		final VisualStyle vs = vizmapper.getVisualStyle();

		final Color ec = (Color) vs.getEdgeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.EDGE_COLOR);
		final Color nbc = (Color) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.NODE_BORDER_COLOR);
		final Color nfc = (Color) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.NODE_FILL_COLOR);
		final Color nlc = (Color) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.NODE_LABEL_COLOR);
		final Color elc = (Color) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.EDGE_LABEL_COLOR);
		final Number elw = (Number) vs.getEdgeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.EDGE_LINE_WIDTH);
		final Number nbw = (Number) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.NODE_LINE_WIDTH);
		final Number ns = (Number) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.NODE_SIZE);
		final Number efs = (Number) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.EDGE_FONT_SIZE);
		final Number nfs = (Number) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.NODE_FONT_SIZE);

		final Color bc = vs.getGlobalAppearanceCalculator().getDefaultBackgroundColor();
		final Color nsc = vs.getGlobalAppearanceCalculator().getDefaultNodeSelectionColor();
		final Color esc = vs.getGlobalAppearanceCalculator().getDefaultEdgeSelectionColor();

		target.addAttribute("bc", getRGB(bc));
		target.addAttribute("ec", getRGB(ec));
		target.addAttribute("elw", elw.intValue());
		target.addAttribute("nbc", getRGB(nbc));
		target.addAttribute("nfc", getRGB(nfc));
		target.addAttribute("nsc", getRGB(nsc));
		target.addAttribute("esc", getRGB(esc));
		target.addAttribute("nlc", getRGB(nlc));
		target.addAttribute("elc", getRGB(elc));
		target.addAttribute("nbw", nbw.intValue());
		target.addAttribute("ns", ns.intValue());
		target.addAttribute("efs", efs.intValue());
		target.addAttribute("nfs", nfs.intValue());

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

			int x1 = (int) (xx1);
			int y1 = (int) (yy1);
			int x2 = (int) (xx2);
			int y2 = (int) (yy2);

			if (useFitting) {
				x1 = (int) (((xx1 - minX) / cytoscapeViewWidth) * width);
				y1 = (int) (((yy1 - minY) / cytoscapeViewHeight) * height);
				x2 = (int) (((xx2 - minX) / cytoscapeViewWidth) * width);
				y2 = (int) (((yy2 - minY) / cytoscapeViewHeight) * height);
			}

			target.addAttribute("node1x", x1);
			target.addAttribute("node1y", y1);
			target.addAttribute("node2x", x2);
			target.addAttribute("node2y", y2);

			if (!styleOptimization) {
				final EdgeAppearance ea = vs.getEdgeAppearanceCalculator().calculateEdgeAppearance(e, network);
				final NodeAppearance n1a = vs.getNodeAppearanceCalculator().calculateNodeAppearance(node1, network);
				final NodeAppearance n2a = vs.getNodeAppearanceCalculator().calculateNodeAppearance(node2, network);

				target.addAttribute("_ec", getRGB((Color) ea.get(VisualPropertyType.EDGE_COLOR)));
				target.addAttribute("_elw", ((Number) ea.get(VisualPropertyType.EDGE_LINE_WIDTH)).intValue());
				// target.addAttribute("_elc", getRGB(elc));
				// target.addAttribute("_efs", efs.intValue());

				target.addAttribute("_n1bc", getRGB((Color) n1a.get(VisualPropertyType.NODE_BORDER_COLOR)));
				target.addAttribute("_n1fc", getRGB((Color) n1a.get(VisualPropertyType.NODE_FILL_COLOR)));
				target.addAttribute("_n1bw", ((Number) n1a.get(VisualPropertyType.NODE_LINE_WIDTH)).intValue());
				target.addAttribute("_n1s", ((Number) n1a.get(VisualPropertyType.NODE_SIZE)).intValue());

				target.addAttribute("_n2bc", getRGB((Color) n2a.get(VisualPropertyType.NODE_BORDER_COLOR)));
				target.addAttribute("_n2fc", getRGB((Color) n2a.get(VisualPropertyType.NODE_FILL_COLOR)));
				target.addAttribute("_n2bw", ((Number) n2a.get(VisualPropertyType.NODE_LINE_WIDTH)).intValue());
				target.addAttribute("_n2s", ((Number) n2a.get(VisualPropertyType.NODE_SIZE)).intValue());
				// target.addAttribute("_nlc", getRGB(nlc));
				// target.addAttribute("_nfs", nfs.intValue());
			}

			target.endTag("e");
		}
	}

	private String getRGB(final Color bc) {
		return "rgb(" + bc.getRed() + "," + bc.getGreen() + "," + bc.getBlue() + ")";
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

	public void setTextsVisible(final boolean b) {
		textsVisible = b;
		requestRepaint();
	}

	/**
	 * Optimize styles to minimize client-server traffic
	 * 
	 * @param b
	 */
	public void setOptimizedStyles(final boolean b) {
		styleOptimization = b;
		requestRepaint();
	}
}
