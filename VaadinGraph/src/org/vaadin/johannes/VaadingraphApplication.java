package org.vaadin.johannes;

import org.vaadin.johannes.graph.VaadinGraph;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import csplugins.layout.algorithms.circularLayout.CircularLayoutAlgorithm;
import csplugins.layout.algorithms.force.ForceDirectedLayout;
import csplugins.layout.algorithms.hierarchicalLayout.HierarchicalLayoutAlgorithm;
import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.ding.DingNetworkView;
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.layout.algorithms.GridNodeLayout;
import cytoscape.visual.VisualPropertyType;

public class VaadingraphApplication extends Application {
	private static final long serialVersionUID = 8397288032426120704L;
	private static final int HEIGHT = 600;
	private static final int WIDTH = 800;
	private NativeSelect networkSelect;
	private Window mainWindow;
	private VaadinGraph graph;
	private double nodeSize = 15;
	private NativeSelect layoutSelect;
	private String path = "C:/research/sifs/";
	private String fileName;
	private boolean reLayout = true;
	private final VerticalLayout mainLayout = new VerticalLayout();

	transient private DingNetworkView view;
	transient private CyLayoutAlgorithm loAlgorithm = new ForceDirectedLayout();
	transient private CyNetwork net;

	@Override
	public void init() {
		path = getProperty("sifpath");
		System.out.println(path);
		fileName = path + "sample.sif";
		mainWindow = new Window("Vaadingraph Application");
		mainWindow.setContent(mainLayout);
		mainLayout.setMargin(true);
		final HorizontalLayout hl = new HorizontalLayout();
		mainLayout.addComponent(hl);
		hl.setSpacing(true);
		hl.addComponent(getNetworkSelect());
		hl.addComponent(getLayoutSelect());
		hl.addComponent(getNodeSizeSelect());
		hl.addComponent(getStyleSelect());
		graph = getNetworkGraph(WIDTH, HEIGHT);
		mainLayout.addComponent(graph);
		setMainWindow(mainWindow);
	}

	private Component getStyleSelect() {
		final NativeSelect styleSelect = new NativeSelect();
		styleSelect.setCaption("Visual Style");
		styleSelect.addItem("default");
		styleSelect.addItem("Nested Network Style");
		styleSelect.addItem("Sample1");
		styleSelect.addItem("Solid");

		styleSelect.setNullSelectionAllowed(false);
		styleSelect.setImmediate(true);
		styleSelect.select("default");
		styleSelect.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 8021555546280140242L;

			public void valueChange(final ValueChangeEvent event) {
				reLayout = true;
				Cytoscape.getVisualMappingManager().setVisualStyle(styleSelect.getValue().toString());
				final VaadinGraph g = getNetworkGraph(WIDTH, HEIGHT);
				mainWindow.replaceComponent(graph, g);
				graph = g;
			}
		});
		return styleSelect;
	}

	private Component getNodeSizeSelect() {
		final NativeSelect sizeSelect = new NativeSelect();
		sizeSelect.setCaption("Node size");
		sizeSelect.addItem("5");
		sizeSelect.addItem("10");
		sizeSelect.addItem("15");
		sizeSelect.addItem("20");
		sizeSelect.addItem("30");
		sizeSelect.setNullSelectionAllowed(false);
		sizeSelect.setImmediate(true);
		sizeSelect.select("15");
		sizeSelect.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 8021555546280140242L;

			public void valueChange(final ValueChangeEvent event) {
				nodeSize = Double.valueOf(sizeSelect.getValue().toString());
				final VaadinGraph g = getNetworkGraph(WIDTH, HEIGHT);
				mainWindow.replaceComponent(graph, g);
				graph = g;
			}
		});
		return sizeSelect;
	}

	private Component getNetworkSelect() {
		networkSelect = new NativeSelect();
		networkSelect.setCaption("Network file");
		networkSelect.addContainerProperty("path", String.class, null);
		Item i = networkSelect.addItem("sample");
		i.getItemProperty("path").setValue(path + "sample.sif");
		i = networkSelect.addItem("multiWordProteins");
		i.getItemProperty("path").setValue(path + "multiWordProteins.sif");
		i = networkSelect.addItem("galFiltered");
		i.getItemProperty("path").setValue(path + "galFiltered.sif");
		i = networkSelect.addItem("ptackek");
		i.getItemProperty("path").setValue(path + "ptackek.sif");

		networkSelect.setNullSelectionAllowed(false);
		networkSelect.setImmediate(true);
		networkSelect.select("sample");
		networkSelect.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 8021555546280140242L;

			public void valueChange(final ValueChangeEvent event) {
				reLayout = true;
				fileName = networkSelect.getItem(networkSelect.getValue()).getItemProperty("path").toString();
				final VaadinGraph g = getNetworkGraph(WIDTH, HEIGHT);
				mainWindow.replaceComponent(graph, g);
				graph = g;
			}
		});

		return networkSelect;
	}

	private Component getLayoutSelect() {
		layoutSelect = new NativeSelect();
		layoutSelect.setCaption("Layout algorithm");
		layoutSelect.addContainerProperty("alg", CyLayoutAlgorithm.class, null);

		Item i = layoutSelect.addItem("Force Directed");
		i.getItemProperty("alg").setValue(new ForceDirectedLayout());
		i = layoutSelect.addItem("Hierarchical");
		i.getItemProperty("alg").setValue(new HierarchicalLayoutAlgorithm());
		i = layoutSelect.addItem("Grid");
		i.getItemProperty("alg").setValue(new GridNodeLayout());
		i = layoutSelect.addItem("Circular");
		i.getItemProperty("alg").setValue(new CircularLayoutAlgorithm());

		layoutSelect.setNullSelectionAllowed(false);
		layoutSelect.setImmediate(true);
		layoutSelect.select("Force Directed");

		layoutSelect.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 3668584778868323776L;

			public void valueChange(final ValueChangeEvent event) {
				reLayout = true;
				loAlgorithm = (CyLayoutAlgorithm) layoutSelect.getItem(layoutSelect.getValue()).getItemProperty("alg").getValue();
				final VaadinGraph g = getNetworkGraph(WIDTH, HEIGHT);
				mainWindow.replaceComponent(graph, g);
				graph = g;
			}
		});
		return layoutSelect;
	}

	private VaadinGraph getNetworkGraph(final int width, final int height) {
		if (reLayout) {
			Cytoscape.createNewSession();
			net = Cytoscape.createNetworkFromFile(fileName);
			view = (DingNetworkView) Cytoscape.createNetworkView(net);
			view.applyLayout(loAlgorithm);
		}
		view.getVisualStyle().getNodeAppearanceCalculator().getDefaultAppearance().set(VisualPropertyType.NODE_SIZE, nodeSize);
		final VaadinGraph graph = new VaadinGraph(net, view, "test", width, height);
		graph.setImmediate(true);
		graph.setWidth(WIDTH + "px");
		graph.setHeight(HEIGHT + "px");
		reLayout = false;
		return graph;
	}
}
