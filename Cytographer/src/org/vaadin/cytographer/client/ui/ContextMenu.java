package org.vaadin.cytographer.client.ui;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

public class ContextMenu extends PopupPanel {

	private final ContextListener listener;

	public ContextMenu(final ContextListener listener) {
		super();
		this.listener = listener;
		this.listener.initCommands(this);
		sinkEvents(Event.ONMOUSEUP | Event.ONDBLCLICK | Event.ONCONTEXTMENU | Event.ONCLICK);
		setStyleName("contextmenu");
	}

	public void showMenu(final int x, final int y) {
		final MenuBar contextMenu = new MenuBar(true);
		for (final Command command : listener.getCommands()) {
			final MenuItem commandItem = new MenuItem(listener.getCommandName(command), true, command);
			contextMenu.addItem(commandItem);
		}

		contextMenu.setVisible(true);
		add(contextMenu);
		super.setPopupPosition(x, y);
		super.show();
	}

	public class ContextMenuCommand implements Command {

		@Override
		public void execute() {
			ContextMenu.this.hide();
		}

	}
}
