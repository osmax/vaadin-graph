package org.vaadin.cytographer;

import org.vaadin.cytographer.ctrl.CytographerController;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

public class CytographerToolbar extends HorizontalLayout {

	private static final long serialVersionUID = -509091973436800759L;
	private final CytographerController controller;

	public CytographerToolbar(final CytographerController controller) {
		setSpacing(true);
		this.controller = controller;
		addComponent(getNewButton());
		addComponent(getOpenButton());
		addComponent(getSaveButton());
		addComponent(getSaveAsButton());
		Component fitb;
		addComponent(fitb = getFitToViewButton());
		setComponentAlignment(fitb, Alignment.BOTTOM_RIGHT);
	}

	private Component getNewButton() {
		final Button button = new Button("New network");
		button.setImmediate(true);
		button.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -8874905593085298508L;

			@Override
			public void buttonClick(final ClickEvent event) {
				controller.createNewNetworkGraph();
			}
		});
		return button;
	}

	private Component getOpenButton() {
		final Button button = new Button("Open...");
		button.setImmediate(true);
		button.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -8874905593085298508L;

			@Override
			public void buttonClick(final ClickEvent event) {

			}
		});
		button.setEnabled(false);
		return button;
	}

	private Component getSaveButton() {
		final Button saveButton = new Button("Save");
		saveButton.setImmediate(true);
		saveButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -8874905593085298508L;

			@Override
			public void buttonClick(final ClickEvent event) {

			}
		});
		saveButton.setEnabled(false);
		return saveButton;
	}

	private Component getSaveAsButton() {
		final Button saveButton = new Button("Export...");
		saveButton.setImmediate(true);
		saveButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -8874905593085298508L;

			@Override
			public void buttonClick(final ClickEvent event) {
			}
		});
		saveButton.setEnabled(false);
		return saveButton;
	}

	private Component getFitToViewButton() {
		final Button button = new Button("Fit to view");
		button.setImmediate(true);
		button.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -8874905593085298508L;

			@Override
			public void buttonClick(final ClickEvent event) {
				controller.fitToView();
			}
		});
		return button;
	}

}
