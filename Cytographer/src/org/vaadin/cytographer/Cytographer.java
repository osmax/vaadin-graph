package org.vaadin.cytographer;

import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

import cytoscape.CyNetwork;
import cytoscape.view.CyNetworkView;

/**
 * Server side component for the VCytographer widget.
 */
@com.vaadin.ui.ClientWidget(org.vaadin.cytographer.client.ui.VCytographer.class)
public class Cytographer extends AbstractComponent {
	private static final long serialVersionUID = 8483008141219579936L;

	public enum GraphOperation {
		REPAINT, SET_NODE_SIZE, SET_VISUAL_STYLE, SET_TEXT_VISIBILITY, SET_OPTIMIZED_STYLES, UPDATE_NODE
	}

	private GraphOperation currentOperation = GraphOperation.REPAINT;

	private final GraphProperties graphProperties;
	private final PaintController ctrl = new PaintController();

	private String updatedNode;

	@Override
	public void paintContent(final PaintTarget target) throws PaintException {
		super.paintContent(target);
		target.addAttribute("operation", currentOperation.toString());

		switch (currentOperation) {
		case REPAINT:
			ctrl.repaintGraph(target, graphProperties);
			break;
		case SET_NODE_SIZE:
			ctrl.paintNodeSize(target, graphProperties);
			break;
		case SET_VISUAL_STYLE:
			ctrl.paintVisualStyle(target, graphProperties);
			break;
		case SET_TEXT_VISIBILITY:
			ctrl.paintTextVisibility(target, graphProperties);
			break;
		case SET_OPTIMIZED_STYLES:
			ctrl.paintOptimizedStyles(target, graphProperties);
			break;
		case UPDATE_NODE:
			ctrl.updateNode(target, graphProperties, updatedNode);
		default:
			;
		}
		currentOperation = GraphOperation.REPAINT;
	}

	/**
	 * Receive and handle events and other variable changes from the client.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void changeVariables(final Object source, final Map<String, Object> variables) {
		super.changeVariables(source, variables);
		if (variables.containsKey("selectedEdges")) {
			graphProperties.clearSelectedEdges();
			final String[] strs = (String[]) variables.get("selectedEdges");
			for (final String str : strs) {
				graphProperties.addSelectedEdge(str);
			}
			System.out.printf("Selected %d edges\n", graphProperties.getSelectedEdges().size());
		}
		if (variables.containsKey("selectedNodes")) {
			graphProperties.clearSelectedNodes();
			final String[] strs = (String[]) variables.get("selectedNodes");
			for (final String str : strs) {
				graphProperties.addSelectedNode(str);
			}
			System.out.printf("Selected %d nodes\n", graphProperties.getSelectedNodes().size());
		}
		if (variables.containsKey("zoomFactor")) {
			graphProperties.setZoomFactor((Float) variables.get("zoomFactor"));
		}
		if (variables.containsKey("createdANode")) {
			final Object[] nodeData = (Object[]) variables.get("createdANode");
			graphProperties.addANewNode((String) nodeData[0], (Integer) nodeData[1], (Integer) nodeData[2]);
		}
		if (variables.containsKey("removedNode")) {
			graphProperties.removeNode((String) variables.get("removedNode"));
		}
		if (variables.containsKey("edgeCreated")) {
			graphProperties.createAnEdge((String[]) variables.get("edgeCreated"));
		}
	}

	public Cytographer(final CyNetwork network, final CyNetworkView finalView, final String title, final int width, final int height) {
		graphProperties = new GraphProperties(network, finalView, title);
		graphProperties.setWidth(width);
		graphProperties.setHeight(height);
	}

	public void setWidthAndHeight(final int width, final int height) {
		graphProperties.setWidth(width);
		graphProperties.setHeight(height);
		requestRepaint();
	}

	/**
	 * Change texts visibilities
	 * 
	 * @param b
	 */
	public void setTextVisible(final boolean b) {
		currentOperation = GraphOperation.SET_TEXT_VISIBILITY;
		graphProperties.setTextVisible(b);
		requestRepaint();
	}

	/**
	 * Optimize styles to minimize client-server traffic
	 * 
	 * @param b
	 */
	public void setStyleOptimization(final boolean b) {
		currentOperation = GraphOperation.SET_OPTIMIZED_STYLES;
		graphProperties.setStyleOptimization(b);
		requestRepaint();
	}

	/**
	 * Change node size
	 * 
	 * @param nodeSize
	 * @param repaint
	 */
	public void setNodeSize(final double nodeSize, final boolean repaint) {
		graphProperties.setNodeSize(nodeSize);
		if (repaint) {
			currentOperation = GraphOperation.SET_NODE_SIZE;
			requestRepaint();
		}
	}

	public void repaintGraph() {
		currentOperation = GraphOperation.REPAINT;
		requestRepaint();
	}

	public void setTextsVisible(final boolean b) {
		currentOperation = GraphOperation.SET_TEXT_VISIBILITY;
		graphProperties.setTextVisible(b);
		requestRepaint();
	}

	public void setOptimizedStyles(final boolean b) {
		graphProperties.setStyleOptimization(b);
	}

	public Container getNodeAttributeContainerForSelectedNodes() {
		return graphProperties.getNodeAttributeContainerForSelectedNodes();
	}
}
