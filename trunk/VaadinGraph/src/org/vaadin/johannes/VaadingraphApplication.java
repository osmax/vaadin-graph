package org.vaadin.johannes;

import org.vaadin.johannes.graph.VaadinGraph;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import csplugins.layout.algorithms.circularLayout.CircularLayoutAlgorithm;
import csplugins.layout.algorithms.force.ForceDirectedLayout;
import csplugins.layout.algorithms.hierarchicalLayout.HierarchicalLayoutAlgorithm;
import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.data.readers.CytoscapeSessionReader;
import cytoscape.ding.DingNetworkView;
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.layout.algorithms.GridNodeLayout;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualPropertyType;

public class VaadingraphApplication extends Application {

	private static final long serialVersionUID = 8397288032426120704L;
	private static final int HEIGHT = 600;
	private static final int WIDTH = 800;

	private NativeSelect networkSelect;
	private Window mainWindow;
	private VaadinGraph graph;
	private double nodeSize = 10;
	private NativeSelect layoutSelect;
	private String path = "C:/research/sifs/";
	private String fileName;
	private boolean reLayout = true;
	private final VerticalLayout mainLayout = new VerticalLayout();

	transient private DingNetworkView view;
	transient private CyLayoutAlgorithm loAlgorithm = new ForceDirectedLayout();
	transient private CyNetwork net;
	private NativeSelect sessionSelect;
	private HorizontalLayout hl, hl2;

	static {
		final CytoscapeInit init = new CytoscapeInit();
		init.init(new CytographerInit());
	}

	@Override
	public void init() {
		path = getProperty("sifpath");
		System.out.println(path);
		fileName = path + "sample.sif";

		mainWindow = new Window("Vaadingraph Application");
		mainWindow.setContent(mainLayout);
		mainLayout.setMargin(true);
		mainLayout.addComponent(hl = new HorizontalLayout());

		hl.setSpacing(true);
		hl.addComponent(getNetworkSelect());
		hl.addComponent(getSessionSelect());
		hl.addComponent(getLayoutSelect());
		hl.addComponent(getNodeSizeSelect());
		hl.addComponent(getStyleSelect());

		final Component cb1 = getTextHideBox();
		final Component cb2 = getStyleOptimizedBox();
		hl.addComponent(cb1);
		hl.addComponent(cb2);
		hl.setComponentAlignment(cb1, Alignment.BOTTOM_LEFT);
		hl.setComponentAlignment(cb2, Alignment.BOTTOM_LEFT);

		graph = getNetworkGraph(WIDTH, HEIGHT);
		mainLayout.addComponent(hl2 = new HorizontalLayout());
		hl2.addComponent(graph);
		hl2.addComponent(getInfoLabel());
		setMainWindow(mainWindow);
	}

	private Component getInfoLabel() {
		return new Label(
				"<h2><h3 style=\"color:red;\">Cytographer main features</h3><ul><li>No Flash or browser plugins needed!</li><li>Drag and drop move nodes</li><li>Drag and drop move whole graph</li><li>Mouse wheel zoom</li><li>Node selection and deselection by mouse click</li><li>Generic or node specific styles (see the difference e.g. in the &quot;galFiltered&quot; session file)</li></ul></h2>",
				Label.CONTENT_XHTML);
	}

	private Component getStyleOptimizedBox() {
		final CheckBox cb = new CheckBox("Node specific styles enabled");
		cb.setImmediate(true);
		cb.setValue(true);
		cb.addListener(new CheckBox.ClickListener() {
			private static final long serialVersionUID = 4837240993197391750L;

			public void buttonClick(final ClickEvent event) {
				graph.setOptimizedStyles(!(Boolean) cb.getValue());
			}
		});
		return cb;
	}

	private Component getTextHideBox() {
		final CheckBox cb = new CheckBox("Hide texts");
		cb.setImmediate(true);
		cb.setValue(true);
		cb.addListener(new CheckBox.ClickListener() {
			private static final long serialVersionUID = 1981652250991931328L;

			public void buttonClick(final ClickEvent event) {
				graph.setTextsVisible(!(Boolean) cb.getValue());
			}
		});
		return cb;
	}

	private Component getStyleSelect() {
		final NativeSelect styleSelect = new NativeSelect();
		styleSelect.setCaption("Visual Style");
		styleSelect.addItem("default");
		styleSelect.addItem("Nested Network Style");
		styleSelect.addItem("Sample1");
		styleSelect.addItem("Solid");
		styleSelect.addItem("Universe");

		styleSelect.setNullSelectionAllowed(false);
		styleSelect.setImmediate(true);
		styleSelect.select("default");
		styleSelect.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 8021555546280140242L;

			public void valueChange(final ValueChangeEvent event) {
				reLayout = true;
				Cytoscape.getVisualMappingManager().setVisualStyle(styleSelect.getValue().toString());
				final VaadinGraph g = getNetworkGraph(WIDTH, HEIGHT);
				hl2.replaceComponent(graph, g);
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
		sizeSelect.select("10");
		sizeSelect.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 8021555546280140242L;

			public void valueChange(final ValueChangeEvent event) {
				if (sizeSelect.getValue() == null) {
					nodeSize = -1;
				} else {
					nodeSize = Double.valueOf(sizeSelect.getValue().toString());
				}
				final VaadinGraph g = getNetworkGraph(WIDTH, HEIGHT);
				hl2.replaceComponent(graph, g);
				graph = g;
			}
		});
		return sizeSelect;
	}

	private Component getNetworkSelect() {
		networkSelect = new NativeSelect();
		networkSelect.setCaption("Network sif file");
		networkSelect.addContainerProperty("path", String.class, null);
		Item i = networkSelect.addItem("sample");
		i.getItemProperty("path").setValue(path + "sample.sif");
		i = networkSelect.addItem("multiWordProteins");
		i.getItemProperty("path").setValue(path + "multiWordProteins.sif");
		i = networkSelect.addItem("galFiltered");
		i.getItemProperty("path").setValue(path + "galFiltered.sif");

		networkSelect.setNullSelectionAllowed(false);
		networkSelect.setImmediate(true);
		networkSelect.select("sample");
		networkSelect.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 8021555546280140242L;

			public void valueChange(final ValueChangeEvent event) {
				reLayout = true;
				fileName = networkSelect.getItem(networkSelect.getValue()).getItemProperty("path").toString();
				final VaadinGraph g = getNetworkGraph(WIDTH, HEIGHT);
				hl2.replaceComponent(graph, g);
				graph = g;
			}
		});

		return networkSelect;
	}

	private Component getSessionSelect() {
		sessionSelect = new NativeSelect();
		sessionSelect.setImmediate(true);
		sessionSelect.setNullSelectionAllowed(true);
		sessionSelect.setNullSelectionItemId("-no selection-");
		sessionSelect.setCaption("Cytoscape session file");
		sessionSelect.addContainerProperty("path", String.class, null);

		Item i = sessionSelect.addItem("example");
		i.getItemProperty("path").setValue(path + "example.cys");

		i = sessionSelect.addItem("example2");
		i.getItemProperty("path").setValue(path + "example2.cys");

		i = sessionSelect.addItem("galFiltered");
		i.getItemProperty("path").setValue(path + "galFiltered.cys");

		sessionSelect.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 6954002045682969159L;

			public void valueChange(final ValueChangeEvent event) {
				reLayout = true;
				if (sessionSelect.getValue() == null) {
					networkSelect.setEnabled(true);
					fileName = networkSelect.getItem(networkSelect.getValue()).getItemProperty("path").toString();
				} else {
					networkSelect.setEnabled(false);
					fileName = sessionSelect.getItem(sessionSelect.getValue()).getItemProperty("path").toString();
				}
				final VaadinGraph g = getNetworkGraph(WIDTH, HEIGHT);
				hl2.replaceComponent(graph, g);
				graph = g;
			}
		});
		return sessionSelect;
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
				hl2.replaceComponent(graph, g);
				graph = g;
			}
		});
		return layoutSelect;
	}

	private VaadinGraph getNetworkGraph(final int width, final int height) {
		if (reLayout) {
			if (!fileName.endsWith(".cys")) {
				Cytoscape.createNewSession();
				if (fileName.endsWith(".sif")) {
					net = Cytoscape.createNetworkFromFile(fileName);
					view = (DingNetworkView) Cytoscape.createNetworkView(net);
					view.applyLayout(loAlgorithm);
				} else if (fileName.endsWith(".gml")) {
					net = Cytoscape.createNetworkFromFile(fileName);
					view = (DingNetworkView) Cytoscape.createNetworkView(net);
					// final GMLReader reader = new GMLReader(fileName);
					// net = Cytoscape.createNetwork(reader, true, null);
					// reader.get
				}
			} else {
				Cytoscape.createNewSession();
				Cytoscape.setSessionState(Cytoscape.SESSION_OPENED);
				Cytoscape.createNewSession();
				Cytoscape.setSessionState(Cytoscape.SESSION_NEW);
				CytoscapeSessionReader sr;
				try {
					sr = new CytoscapeSessionReader(fileName, null);
					sr.read();
				} catch (final Exception e) {
					e.printStackTrace();
				} finally {
					sr = null;
				}
				Cytoscape.setCurrentSessionFileName(fileName);
			}
		}
		final VisualMappingManager vizmapper = Cytoscape.getVisualMappingManager();
		vizmapper.getVisualStyle().getNodeAppearanceCalculator().getDefaultAppearance().set(VisualPropertyType.NODE_SIZE, nodeSize);
		final VaadinGraph graph = new VaadinGraph(net, view, "test", width, height);
		graph.setImmediate(true);
		graph.setWidth(WIDTH + "px");
		graph.setHeight(HEIGHT + "px");
		reLayout = false;
		return graph;
	}
}
