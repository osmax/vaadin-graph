/* 
 * Copyright 2011 Johannes Tuikkala <johannes@vaadin.com>
 *                           LICENCED UNDER
 *                  GNU LESSER GENERAL PUBLIC LICENSE
 *                     Version 3, 29 June 2007
 */
package org.vaadin.cytographer;

import org.vaadin.cytographer.ctrl.CytographerController;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class CytographerApplication extends Application {

	private static final long serialVersionUID = 8397288032426120704L;
	private static final int HEIGHT = 450;
	private static final int WIDTH = 800;

	private Window mainWindow;
	private final VerticalLayout mainLayout = new VerticalLayout();
	private final HorizontalLayout hl2 = new HorizontalLayout();

	private final CytographerController controller;

	public CytographerApplication() {
		controller = new CytographerController(this, WIDTH, HEIGHT);
	}

	@Override
	public void init() {
		mainWindow = new Window("Vaadingraph Application");
		mainWindow.setContent(mainLayout);
		mainLayout.setMargin(true);
		mainLayout.addComponent(new CytographerToolbar(controller));
		mainLayout.addComponent(new CytographerActionToolbar(controller, getProperty("sifpath")));
		mainLayout.addComponent(hl2);

		hl2.addComponent(getInfoLabel());

		controller.createNewNetworkGraph();

		setMainWindow(mainWindow);
		setTheme("cytographertheme");
	}

	public void paintGraph(final Cytographer graph) {
		hl2.removeAllComponents();
		hl2.addComponent(graph);
		hl2.addComponent(getInfoLabel());
	}

	private Component getInfoLabel() {
		final VerticalLayout vlo = new VerticalLayout();
		final Label l1 = new Label(
				"<h2><h3 style=\"color:red;\">Cytographer main features</h3><ul><li>No Flash or browser plugins needed!</li><li>Drag and drop move nodes</li><li>Drag and drop move whole graph</li><li>Mouse wheel zoom</li><li>Node selection and deselection by mouse click</li><li>Generic or node specific styles</li><li>Ctrl-click and drag selection box</li><li>Right click context menu for node linking and deletion</li><li>Double click node creation</li></ul></h2>",
				Label.CONTENT_XHTML);

		vlo.addComponent(l1);
		return vlo;
	}

}
