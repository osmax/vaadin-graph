package org.vaadin.cytographer;

import org.vaadin.cytographer.ctrl.CytographerController;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;

import csplugins.layout.algorithms.circularLayout.CircularLayoutAlgorithm;
import csplugins.layout.algorithms.force.ForceDirectedLayout;
import csplugins.layout.algorithms.hierarchicalLayout.HierarchicalLayoutAlgorithm;
import cytoscape.Cytoscape;
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.layout.algorithms.GridNodeLayout;

public class CytographerActionToolbar extends HorizontalLayout {
	private static final long serialVersionUID = -2390577303164805877L;

	private final CytographerController controller;
	private String path = "C:/research/sifs/";

	private NativeSelect networkSelect;
	private NativeSelect layoutSelect;
	private NativeSelect sessionSelect;

	public CytographerActionToolbar(final CytographerController controller, final String path) {
		setSpacing(true);
		this.controller = controller;
		this.path = path;
		addComponent(getNetworkSelect());
		// addComponent(getSessionSelect());
		addComponent(getLayoutSelect());
		addComponent(getNodeSizeSelect());
		addComponent(getStyleSelect());

		final Component cb1 = getTextHideBox();
		addComponent(cb1);
		setComponentAlignment(cb1, Alignment.BOTTOM_LEFT);
	}

	public void setPath(final String path) {
		this.path = path;
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

		networkSelect.setNullSelectionAllowed(true);
		networkSelect.setNullSelectionItemId("[select]");
		networkSelect.setImmediate(true);
		networkSelect.select(null);
		networkSelect.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 8021555546280140242L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				if (networkSelect.getValue() != null) {
					final String fileName = networkSelect.getItem(networkSelect.getValue()).getItemProperty("path").toString();
					controller.loadNetworkGraph(fileName);
					networkSelect.select(null);
				}
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

			@Override
			public void valueChange(final ValueChangeEvent event) {
				if (sessionSelect.getValue() == null) {
					networkSelect.setEnabled(true);
					networkSelect.select(null);
				} else {
					networkSelect.setEnabled(false);
					final String fileName = sessionSelect.getItem(sessionSelect.getValue()).getItemProperty("path").toString();
					controller.loadCytoscapeSession(fileName);
				}
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

		layoutSelect.setNullSelectionAllowed(true);
		layoutSelect.setNullSelectionItemId("[select]");
		layoutSelect.setImmediate(true);
		layoutSelect.select(null);

		layoutSelect.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 3668584778868323776L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				if (layoutSelect.getValue() != null) {
					final CyLayoutAlgorithm loAlgorithm = (CyLayoutAlgorithm) layoutSelect.getItem(layoutSelect.getValue())
							.getItemProperty("alg").getValue();
					controller.applyLayoutAlgorithm(loAlgorithm);
					layoutSelect.select(null);
				}
			}
		});
		return layoutSelect;
	}

	private Component getNodeSizeSelect() {
		final NativeSelect sizeSelect = new NativeSelect();
		sizeSelect.setCaption("Node size");
		sizeSelect.addItem("0");
		sizeSelect.addItem("5");
		sizeSelect.addItem("10");
		sizeSelect.addItem("15");
		sizeSelect.addItem("20");
		sizeSelect.addItem("30");
		sizeSelect.select("10");
		sizeSelect.setNullSelectionAllowed(true);
		sizeSelect.setNullSelectionItemId("[select]");
		sizeSelect.setImmediate(true);
		sizeSelect.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 8021555546280140242L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				if (sizeSelect.getValue() != null) {
					controller.getCurrentGraph().setNodeSize(Double.valueOf(sizeSelect.getValue().toString()), true);
				}
			}
		});
		return sizeSelect;
	}

	private Component getStyleSelect() {
		final NativeSelect styleSelect = new NativeSelect();
		styleSelect.setCaption("Visual Style");
		styleSelect.addItem("default");
		styleSelect.addItem("Nested Network Style");
		styleSelect.addItem("Sample1");
		styleSelect.addItem("Solid");
		styleSelect.addItem("Universe");

		styleSelect.setNullSelectionAllowed(true);
		styleSelect.setNullSelectionItemId("[select]");
		styleSelect.setImmediate(true);
		styleSelect.select(null);
		styleSelect.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 8021555546280140242L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				if (styleSelect.getValue() != null) {
					Cytoscape.getVisualMappingManager().setVisualStyle(styleSelect.getValue().toString());
					controller.repaintGraph();
					styleSelect.select(null);
				}
			}
		});
		return styleSelect;
	}

	private Component getTextHideBox() {
		final Button cb = new Button("Hide/show texts");
		cb.setImmediate(true);
		cb.setValue(true);
		cb.addListener(new CheckBox.ClickListener() {
			private static final long serialVersionUID = 1981652250991931328L;

			@Override
			public void buttonClick(final ClickEvent event) {
				controller.getCurrentGraph().setTextsVisible(!controller.getCurrentGraph().isTextsVisible());
			}
		});
		return cb;
	}

}
