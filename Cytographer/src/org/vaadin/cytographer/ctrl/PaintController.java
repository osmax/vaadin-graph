package org.vaadin.cytographer.ctrl;

import giny.model.Edge;
import giny.model.Node;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import org.vaadin.cytographer.model.GraphProperties;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;

import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.visual.EdgeAppearance;
import cytoscape.visual.LineStyle;
import cytoscape.visual.NodeAppearance;
import cytoscape.visual.NodeShape;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualStyle;

public class PaintController {

	private static final int MARGIN = 20;
	private Set<Integer> paintedNodes = new HashSet<Integer>();

	public void repaintGraph(final PaintTarget target, final GraphProperties gp) throws PaintException {
		paintedNodes = new HashSet<Integer>();
		target.addAttribute("title", gp.getTitle());
		target.addAttribute("gwidth", gp.getWidth());
		target.addAttribute("gheight", gp.getHeight());
		target.addAttribute("texts", gp.isTextsVisible());

		final VisualMappingManager vizmapper = Cytoscape.getVisualMappingManager();
		final VisualStyle vs = vizmapper.getVisualStyle();

		final Color ec = (Color) vs.getEdgeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.EDGE_COLOR);
		final Color nbc = (Color) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.NODE_BORDER_COLOR);
		final Color nfc = (Color) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.NODE_FILL_COLOR);
		final Color nlc = (Color) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.NODE_LABEL_COLOR);
		final Color elc = (Color) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.EDGE_LABEL_COLOR);
		final Number elw = (Number) vs.getEdgeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.EDGE_LINE_WIDTH);
		final Number nbw = (Number) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.NODE_LINE_WIDTH);
		final Number efs = (Number) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.EDGE_FONT_SIZE);
		final Number nfs = (Number) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.NODE_FONT_SIZE);

		final LineStyle ls = (LineStyle) vs.getEdgeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.EDGE_LINE_STYLE);
		final String dashArray = getDashArray(ls);

		final NodeShape shape = (NodeShape) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.NODE_SHAPE);

		Number ns = (Number) vs.getNodeAppearanceCalculator().getDefaultAppearance().get(VisualPropertyType.NODE_SIZE);
		if (gp.getNodeSize() > 0) {
			ns = gp.getNodeSize();
		}

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
		target.addAttribute("eda", dashArray);
		target.addAttribute("shp", shape.name());

		for (final int ei : gp.getEdges()) {
			final Edge e = gp.getNetwork().getEdge(ei);
			final Node node1 = e.getSource();
			final Node node2 = e.getTarget();
			paintedNodes.add(node1.getRootGraphIndex());
			paintedNodes.add(node2.getRootGraphIndex());

			target.startTag("e");
			target.addAttribute("name", e.getIdentifier());
			target.addAttribute("node1", node1.getIdentifier());
			target.addAttribute("node2", node2.getIdentifier());

			final double xx1 = gp.getFinalView().getNodeView(node1).getXPosition();
			final double yy1 = gp.getFinalView().getNodeView(node1).getYPosition();
			final double xx2 = gp.getFinalView().getNodeView(node2).getXPosition();
			final double yy2 = gp.getFinalView().getNodeView(node2).getYPosition();

			int x1 = (int) xx1;
			int y1 = (int) yy1;
			int x2 = (int) xx2;
			int y2 = (int) yy2;

			if (gp.isUseFitting()) {
				x1 = MARGIN + (int) ((xx1 - gp.getMinX()) / gp.getCytoscapeViewWidth() * (gp.getWidth() - 2 * MARGIN));
				y1 = MARGIN + (int) ((yy1 - gp.getMinY()) / gp.getCytoscapeViewHeight() * (gp.getHeight() - 2 * MARGIN));
				x2 = MARGIN + (int) ((xx2 - gp.getMinX()) / gp.getCytoscapeViewWidth() * (gp.getWidth() - 2 * MARGIN));
				y2 = MARGIN + (int) ((yy2 - gp.getMinY()) / gp.getCytoscapeViewHeight() * (gp.getHeight() - 2 * MARGIN));
			}

			target.addAttribute("node1x", x1);
			target.addAttribute("node1y", y1);
			target.addAttribute("node2x", x2);
			target.addAttribute("node2y", y2);

			if (!gp.isStyleOptimization()) {
				final EdgeAppearance ea = vs.getEdgeAppearanceCalculator().calculateEdgeAppearance(e, gp.getNetwork());
				final NodeAppearance n1a = vs.getNodeAppearanceCalculator().calculateNodeAppearance(node1, gp.getNetwork());
				final NodeAppearance n2a = vs.getNodeAppearanceCalculator().calculateNodeAppearance(node2, gp.getNetwork());

				final LineStyle _ls = (LineStyle) ea.get(VisualPropertyType.EDGE_LINE_STYLE);
				final String _dashArray = getDashArray(_ls);

				target.addAttribute("_ec", getRGB((Color) ea.get(VisualPropertyType.EDGE_COLOR)));
				target.addAttribute("_elw", ((Number) ea.get(VisualPropertyType.EDGE_LINE_WIDTH)).intValue());

				target.addAttribute("_n1bc", getRGB((Color) n1a.get(VisualPropertyType.NODE_BORDER_COLOR)));
				target.addAttribute("_n1fc", getRGB((Color) n1a.get(VisualPropertyType.NODE_FILL_COLOR)));
				target.addAttribute("_n1bw", ((Number) n1a.get(VisualPropertyType.NODE_LINE_WIDTH)).intValue());
				target.addAttribute("_n1bs", ((NodeShape) n1a.get(VisualPropertyType.NODE_SHAPE)).name());

				if (gp.getNodeSize() > 0) {
					target.addAttribute("_n1s", ns.intValue());
				} else {
					target.addAttribute("_n1s", ((Number) n1a.get(VisualPropertyType.NODE_SIZE)).intValue());
				}

				target.addAttribute("_n2bc", getRGB((Color) n2a.get(VisualPropertyType.NODE_BORDER_COLOR)));
				target.addAttribute("_n2fc", getRGB((Color) n2a.get(VisualPropertyType.NODE_FILL_COLOR)));
				target.addAttribute("_n2bw", ((Number) n2a.get(VisualPropertyType.NODE_LINE_WIDTH)).intValue());
				target.addAttribute("_n2s", ((Number) n2a.get(VisualPropertyType.NODE_SIZE)).intValue());
				target.addAttribute("_n2bs", ((NodeShape) n1a.get(VisualPropertyType.NODE_SHAPE)).name());
				target.addAttribute("_eda", _dashArray);
			}
			target.endTag("e");
		}
		// paint also single nodes
		for (final int nodeIndex : gp.getNodes()) {
			final Node node1 = gp.getNetwork().getNode(nodeIndex);
			if (!paintedNodes.contains(node1.getRootGraphIndex())) {
				target.startTag("e");
				target.addAttribute("name", "tmp");
				target.addAttribute("node1", node1.getIdentifier());
				final double xx1 = gp.getFinalView().getNodeView(node1).getXPosition();
				final double yy1 = gp.getFinalView().getNodeView(node1).getYPosition();
				final int x1 = (int) xx1;
				final int y1 = (int) yy1;
				target.addAttribute("node1x", x1);
				target.addAttribute("node1y", y1);
				if (!gp.isStyleOptimization()) {
					final NodeAppearance n1a = vs.getNodeAppearanceCalculator().calculateNodeAppearance(node1, gp.getNetwork());

					target.addAttribute("_n1bc", getRGB((Color) n1a.get(VisualPropertyType.NODE_BORDER_COLOR)));
					target.addAttribute("_n1fc", getRGB((Color) n1a.get(VisualPropertyType.NODE_FILL_COLOR)));
					target.addAttribute("_n1bw", ((Number) n1a.get(VisualPropertyType.NODE_LINE_WIDTH)).intValue());
					target.addAttribute("_n1bs", ((NodeShape) n1a.get(VisualPropertyType.NODE_SHAPE)).name());

					if (gp.getNodeSize() > 0) {
						target.addAttribute("_n1s", ns.intValue());
					} else {
						target.addAttribute("_n1s", ((Number) n1a.get(VisualPropertyType.NODE_SIZE)).intValue());
					}
				}
				target.endTag("e");
			}
		}
	}

	private String getDashArray(final LineStyle ls) {
		String dashArray;
		switch (ls) {
		case DASH_DOT:
			dashArray = "4 1";
			break;
		case LONG_DASH:
			dashArray = "6 6";
			break;
		case EQUAL_DASH:
			dashArray = "4 4";
			break;
		case DOT:
			dashArray = "1 1";
			break;
		default:
			dashArray = " ";
		}
		return dashArray;
	}

	public void paintNodeSize(final PaintTarget target, final GraphProperties graphProperties) throws PaintException {
		target.addAttribute("ns", (int) graphProperties.getNodeSize());
	}

	public void paintTextVisibility(final PaintTarget target, final GraphProperties graphProperties) throws PaintException {
		target.addAttribute("texts", graphProperties.isTextsVisible());
	}

	public void updateNode(final PaintTarget target, final GraphProperties graphProperties, final String nodeId) throws PaintException {
		final VisualMappingManager vizmapper = Cytoscape.getVisualMappingManager();
		final VisualStyle vs = vizmapper.getVisualStyle();
		final CyNode node = Cytoscape.getCyNode(nodeId);
		final NodeAppearance n1a = vs.getNodeAppearanceCalculator().calculateNodeAppearance(node, graphProperties.getNetwork());

		target.addAttribute("node", nodeId);
		target.addAttribute("_n1bc", getRGB((Color) n1a.get(VisualPropertyType.NODE_BORDER_COLOR)));
		target.addAttribute("_n1fc", getRGB((Color) n1a.get(VisualPropertyType.NODE_FILL_COLOR)));
		target.addAttribute("_n1bw", ((Number) n1a.get(VisualPropertyType.NODE_LINE_WIDTH)).intValue());
		target.addAttribute("_n1s", ((Number) n1a.get(VisualPropertyType.NODE_SIZE)).intValue());
	}

	private String getRGB(final Color bc) {
		return "rgb(" + bc.getRed() + "," + bc.getGreen() + "," + bc.getBlue() + ")";
	}

	public void setZoom(final PaintTarget target, final GraphProperties graphProperties) throws PaintException {
		target.addAttribute("zoom", graphProperties.getZoomFactor());
	}

}
