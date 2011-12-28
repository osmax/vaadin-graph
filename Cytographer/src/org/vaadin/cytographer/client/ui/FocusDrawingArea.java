package org.vaadin.cytographer.client.ui;

import org.vaadin.gwtgraphics.client.DrawingArea;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public class FocusDrawingArea extends DrawingArea implements HasAllKeyHandlers, HasFocusHandlers {

	public FocusDrawingArea(final int width, final int height) {
		super(width, height);
	}

	public HandlerRegistration addKeyUpHandler(final KeyUpHandler handler) {
		return addDomHandler(handler, KeyUpEvent.getType());
	}

	public HandlerRegistration addKeyDownHandler(final KeyDownHandler handler) {
		return addDomHandler(handler, KeyDownEvent.getType());
	}

	public HandlerRegistration addKeyPressHandler(final KeyPressHandler handler) {
		return addDomHandler(handler, KeyPressEvent.getType());
	}

	public HandlerRegistration addFocusHandler(final FocusHandler handler) {
		return addDomHandler(handler, FocusEvent.getType());
	}

}
